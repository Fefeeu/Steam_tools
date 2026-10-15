package org.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servidor da demonstração ao vivo.
 *
 * Serve a página demo/index.html e roda cada forma de execução (Sequencial, Paralelo,
 * FCFS e Round Robin) como um processo Java separado, para que o botão "Parar" possa
 * encerrar a execução a qualquer momento. A saída de cada processo fica guardada em
 * memória e a página busca as linhas novas de tempos em tempos.
 *
 * Deve ser executado a partir da pasta steam_tools (o DLC lê src/main/resources/biblioteca_jogos.json).
 */
public class ServidorDemo {

    private static final int PORTA_PADRAO = 8090;
    private static final int MAX_LINHAS = 50_000;

    private enum Modo {
        SEQUENCIAL("sequencial", "Sequencial", "org.tools.Sequencial"),
        PARALELO("paralelo", "Paralelo", "org.tools.Paralelo"),
        FCFS("fcfs", "FCFS", "org.escalonamento.SimuladorFCFS"),
        RR("rr", "Round Robin", "org.tools.RoundRobin");

        final String id;
        final String nome;
        final String classe;

        Modo(String id, String nome, String classe) {
            this.id = id;
            this.nome = nome;
            this.classe = classe;
        }

        static Modo porId(String id) {
            for (Modo m : values()) {
                if (m.id.equals(id)) return m;
            }
            return null;
        }
    }

    /** Uma execução de um modo: o processo, as linhas que ele imprimiu e quando cada uma chegou. */
    private static class Execucao {
        final Modo modo;
        final String parametro;
        final Process processo;
        final long inicio = System.currentTimeMillis();
        final List<String> linhas = new ArrayList<>();
        final List<Long> tempos = new ArrayList<>();
        volatile long fim = -1;
        volatile boolean parada = false;
        volatile Integer codigo = null;

        Execucao(Modo modo, String parametro, Process processo) {
            this.modo = modo;
            this.parametro = parametro;
            this.processo = processo;
        }

        synchronized void adicionar(String linha) {
            if (linhas.size() < MAX_LINHAS) {
                linhas.add(linha);
                tempos.add(System.currentTimeMillis() - inicio);
            }
        }

        String status() {
            if (fim < 0) return "rodando";
            if (parada) return "parado";
            return codigo != null && codigo == 0 ? "concluido" : "erro";
        }

        long decorrido() {
            return (fim < 0 ? System.currentTimeMillis() : fim) - inicio;
        }
    }

    private static final Map<Modo, Execucao> execucoes = new HashMap<>();
    private static final List<JSONObject> historico = new ArrayList<>();
    private static String classpath;

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : PORTA_PADRAO;
        classpath = montarClasspath();

        if (!new File("src/main/resources/biblioteca_jogos.json").exists()) {
            System.out.println("Aviso: rode o servidor a partir da pasta steam_tools, senão o DLC não acha o JSON local.");
        }

        HttpServer servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        servidor.setExecutor(Executors.newCachedThreadPool());
        servidor.createContext("/", ServidorDemo::pagina);
        servidor.createContext("/api/rodar", ServidorDemo::rodar);
        servidor.createContext("/api/parar", ServidorDemo::parar);
        servidor.createContext("/api/log", ServidorDemo::log);
        servidor.createContext("/api/estado", ServidorDemo::estado);
        servidor.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (execucoes) {
                for (Execucao e : execucoes.values()) encerrar(e.processo);
            }
        }));

        System.out.println("Demonstração em http://localhost:" + porta);
    }

    // ---------------------------------------------------------------- rotas

    private static void pagina(HttpExchange ex) throws IOException {
        if (!ex.getRequestURI().getPath().equals("/")) {
            responder(ex, 404, "text/plain", "não encontrado");
            return;
        }
        try (InputStream in = ServidorDemo.class.getResourceAsStream("/demo/index.html")) {
            if (in == null) {
                responder(ex, 500, "text/plain", "demo/index.html não está no classpath");
                return;
            }
            responder(ex, 200, "text/html; charset=utf-8", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private static void rodar(HttpExchange ex) throws IOException {
        if (!exigirPost(ex)) return;
        Map<String, String> q = parametros(ex);
        Modo modo = Modo.porId(q.get("modo"));
        if (modo == null) {
            erro(ex, 400, "modo inválido");
            return;
        }

        List<String> argumentos = new ArrayList<>();
        String parametro = null;
        if (modo == Modo.RR) {
            String quantum = q.getOrDefault("quantum", "1000");
            if (!quantum.matches("\\d{1,6}") || Long.parseLong(quantum) < 1) {
                erro(ex, 400, "quantum inválido");
                return;
            }
            argumentos.add(quantum);
            parametro = "q=" + quantum + "ms";
        }

        // semaforo=false roda o modo sem os semáforos (LimitadorApi e MUTEX do JsonExporter)
        boolean semSemaforo = "false".equals(q.get("semaforo"));
        if (semSemaforo) {
            parametro = parametro == null ? "sem semáforo" : parametro + ", sem semáforo";
        }

        synchronized (execucoes) {
            Execucao atual = execucoes.get(modo);
            if (atual != null && atual.fim < 0) {
                erro(ex, 409, modo.nome + " já está rodando");
                return;
            }

            List<String> comando = new ArrayList<>(List.of(
                    Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8",
                    "-Dsemaforo=" + !semSemaforo,
                    "-cp", classpath, modo.classe));
            comando.addAll(argumentos);

            Process processo = new ProcessBuilder(comando).redirectErrorStream(true).start();
            Execucao execucao = new Execucao(modo, parametro, processo);
            execucoes.put(modo, execucao);
            Thread leitor = new Thread(() -> acompanhar(execucao), "demo-" + modo.id);
            leitor.setDaemon(true);
            leitor.start();
        }
        responder(ex, 200, "application/json", new JSONObject().put("ok", true).toString());
    }

    private static void parar(HttpExchange ex) throws IOException {
        if (!exigirPost(ex)) return;
        Modo modo = Modo.porId(parametros(ex).get("modo"));
        Execucao execucao;
        synchronized (execucoes) {
            execucao = modo == null ? null : execucoes.get(modo);
        }
        if (execucao == null || execucao.fim >= 0) {
            erro(ex, 409, "nada rodando");
            return;
        }
        execucao.parada = true;
        encerrar(execucao.processo);
        responder(ex, 200, "application/json", new JSONObject().put("ok", true).toString());
    }

    private static void log(HttpExchange ex) throws IOException {
        Map<String, String> q = parametros(ex);
        Modo modo = Modo.porId(q.get("modo"));
        Execucao execucao;
        synchronized (execucoes) {
            execucao = modo == null ? null : execucoes.get(modo);
        }
        if (execucao == null) {
            responder(ex, 200, "application/json", new JSONObject().put("status", "parado").put("total", 0).toString());
            return;
        }

        int desde = 0;
        try {
            desde = Math.max(0, Integer.parseInt(q.getOrDefault("desde", "0")));
        } catch (NumberFormatException ignorado) {
        }

        JSONObject resposta = resumo(execucao);
        JSONArray linhas = new JSONArray();
        synchronized (execucao) {
            for (int i = desde; i < execucao.linhas.size(); i++) {
                linhas.put(new JSONObject().put("t", execucao.tempos.get(i)).put("s", execucao.linhas.get(i)));
            }
            resposta.put("total", execucao.linhas.size());
        }
        resposta.put("linhas", linhas);
        responder(ex, 200, "application/json", resposta.toString());
    }

    private static void estado(HttpExchange ex) throws IOException {
        JSONObject modos = new JSONObject();
        synchronized (execucoes) {
            for (Modo m : Modo.values()) {
                Execucao e = execucoes.get(m);
                modos.put(m.id, e == null ? new JSONObject().put("status", "parado") : resumo(e));
            }
        }
        JSONArray hist;
        synchronized (historico) {
            hist = new JSONArray(historico);
        }
        responder(ex, 200, "application/json", new JSONObject().put("modos", modos).put("historico", hist).toString());
    }

    // ---------------------------------------------------------------- processos

    private static void acompanhar(Execucao execucao) {
        try (BufferedReader leitor = new BufferedReader(
                new InputStreamReader(execucao.processo.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = leitor.readLine()) != null) {
                execucao.adicionar(linha);
            }
        } catch (IOException ignorado) {
            // o processo foi encerrado
        }
        try {
            execucao.codigo = execucao.processo.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        execucao.fim = System.currentTimeMillis();

        JSONObject registro = resumo(execucao);
        synchronized (historico) {
            historico.add(registro);
        }
    }

    private static void encerrar(Process processo) {
        processo.descendants().forEach(ProcessHandle::destroy);
        processo.destroy();
        try {
            if (!processo.waitFor(3, TimeUnit.SECONDS)) {
                processo.descendants().forEach(ProcessHandle::destroyForcibly);
                processo.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Métricas impressas no final de cada modo
    private static final Pattern TEMPO_TOTAL = Pattern.compile("Tempo total[^:]*:\\s*(\\d+)\\s*ms");
    private static final Pattern ESPERA = Pattern.compile("Tempo médio de espera:\\s*([\\d.,]+)");
    private static final Pattern TURNAROUND = Pattern.compile("Turnaround médio:\\s*([\\d.,]+)");
    private static final Pattern TROCAS = Pattern.compile("Trocas de contexto:\\s*(\\d+)");
    private static final Pattern DURACAO_FCFS = Pattern.compile("^\\[\\s*(\\d+) ms\\] \\[FCFS\\]");

    private static JSONObject resumo(Execucao e) {
        JSONObject r = new JSONObject()
                .put("modo", e.modo.id)
                .put("nome", e.modo.nome)
                .put("status", e.status())
                .put("decorrido", e.decorrido())
                .put("inicio", e.inicio);
        if (e.parametro != null) r.put("parametro", e.parametro);

        synchronized (e) {
            Long fcfs = null;
            for (String linha : e.linhas) {
                procurar(TEMPO_TOTAL, linha, r, "tempoTotal");
                procurar(ESPERA, linha, r, "esperaMedia");
                procurar(TURNAROUND, linha, r, "turnaroundMedio");
                procurar(TROCAS, linha, r, "trocas");
                Matcher m = DURACAO_FCFS.matcher(linha);
                if (m.find()) fcfs = Long.parseLong(m.group(1));
            }
            // O FCFS não imprime "Tempo total": usa o instante do último evento do escalonador.
            if (!r.has("tempoTotal") && e.modo == Modo.FCFS && fcfs != null && e.fim >= 0 && !e.parada) {
                r.put("tempoTotal", fcfs);
            }
        }
        return r;
    }

    private static void procurar(Pattern p, String linha, JSONObject destino, String chave) {
        Matcher m = p.matcher(linha);
        if (m.find()) destino.put(chave, m.group(1).replace(",", "."));
    }

    /**
     * Classpath para os processos filhos: o do próprio servidor mais os jars das bibliotecas
     * de JSON. Assim funciona tanto pelo IntelliJ quanto pelo "mvn exec:java".
     */
    private static String montarClasspath() {
        Set<String> entradas = new LinkedHashSet<>();
        String[] classes = {
                "org.demo.ServidorDemo",
                "org.json.JSONObject",
                "com.google.gson.Gson",
                "com.fasterxml.jackson.databind.ObjectMapper",
                "com.fasterxml.jackson.core.JsonFactory",
                "com.fasterxml.jackson.annotation.JsonProperty",
                "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule"
        };
        for (String nome : classes) {
            try {
                Class<?> c = Class.forName(nome);
                entradas.add(Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
            } catch (Exception ignorado) {
                // biblioteca não usada nesta instalação
            }
        }
        for (String parte : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!parte.isBlank()) entradas.add(parte);
        }
        return String.join(File.pathSeparator, entradas);
    }

    // ---------------------------------------------------------------- http

    private static boolean exigirPost(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            erro(ex, 405, "use POST");
            return false;
        }
        return true;
    }

    private static Map<String, String> parametros(HttpExchange ex) {
        Map<String, String> mapa = new LinkedHashMap<>();
        String consulta = ex.getRequestURI().getRawQuery();
        if (consulta == null) return mapa;
        for (String par : consulta.split("&")) {
            int i = par.indexOf('=');
            if (i > 0) {
                mapa.put(URLDecoder.decode(par.substring(0, i), StandardCharsets.UTF_8),
                        URLDecoder.decode(par.substring(i + 1), StandardCharsets.UTF_8));
            }
        }
        return mapa;
    }

    private static void erro(HttpExchange ex, int codigo, String mensagem) throws IOException {
        responder(ex, codigo, "application/json", new JSONObject().put("erro", mensagem).toString());
    }

    private static void responder(HttpExchange ex, int codigo, String tipo, String corpo) throws IOException {
        byte[] bytes = corpo.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo.contains("charset") ? tipo : tipo + "; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }
}
