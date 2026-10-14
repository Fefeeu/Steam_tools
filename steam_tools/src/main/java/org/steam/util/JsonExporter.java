package org.steam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.steam.Produto;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class JsonExporter {

    private static final String CAMINHO_COMPLETO = "src/main/resources/biblioteca_jogos.json";

    // Semáforo binário: só uma thread por vez mexe no arquivo.
    // Semaphore NÃO é reentrante, então os métodos públicos pegam a permissão
    // e chamam os métodos privados "SemLock", que nunca pegam de novo.
    private static final Semaphore MUTEX = new Semaphore(1, true);

    // ---------- públicos (com semáforo) ----------

    public static void salvarListaProdutosComoJson(Produto[] listaJogos) {
        MUTEX.acquireUninterruptibly();
        try {
            salvarSemLock(listaJogos);
        } finally {
            MUTEX.release();
        }
    }

    public static Produto[] lerListaProdutosDoJson() {
        MUTEX.acquireUninterruptibly();
        try {
            return lerSemLock();
        } finally {
            MUTEX.release();
        }
    }

    // ler + alterar + gravar = uma única região crítica
    public static void atualizarJogo(Produto novoJogo) {
        if (novoJogo == null || novoJogo.getAppid() == null) return;

        MUTEX.acquireUninterruptibly();
        try {
            Map<String, Produto> mapa = new LinkedHashMap<>();
            for (Produto p : lerSemLock()) {
                mapa.put(p.getAppid(), p);
            }
            mapa.put(novoJogo.getAppid(), novoJogo);
            salvarSemLock(mapa.values().toArray(new Produto[0]));
        } finally {
            MUTEX.release();
        }
    }

    // ---------- privados (sem semáforo) ----------

    private static void salvarSemLock(Produto[] listaJogos) {
        File arquivo = new File(CAMINHO_COMPLETO);
        try {
            if (arquivo.getParentFile() != null) {
                arquivo.getParentFile().mkdirs();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            mapper.writeValue(arquivo, listaJogos);
        } catch (IOException e) {
            System.err.println("Erro ao exportar lista para JSON: " + e.getMessage());
        }
    }

    private static Produto[] lerSemLock() {
        File arquivo = new File(CAMINHO_COMPLETO);
        if (!arquivo.exists()) {
            System.err.println("Arquivo não encontrado: " + arquivo.getAbsolutePath());
            return new Produto[0];
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            return mapper.readValue(arquivo, Produto[].class);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo JSON: " + e.getMessage());
            return new Produto[0];
        }
    }
}