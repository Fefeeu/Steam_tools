package org.steam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.steam.Produto;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * VERSÃO SEM SEMÁFORO: leitura e escrita do JSON sem exclusão mútua.
 * Com várias threads gravando ao mesmo tempo, o arquivo pode perder jogos ou corromper.
 */
public class JsonExporter {

    private static final String CAMINHO_COMPLETO = "src/main/resources/biblioteca_jogos.json";

    public static void salvarListaProdutosComoJson(Produto[] listaJogos) {
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

    public static void atualizarJogo(Produto novoJogo) {
        if (novoJogo == null || novoJogo.getAppid() == null) return;

        Map<String, Produto> mapa = new LinkedHashMap<>();
        for (Produto p : lerListaProdutosDoJson()) {
            mapa.put(p.getAppid(), p);
        }
        mapa.put(novoJogo.getAppid(), novoJogo);

        salvarListaProdutosComoJson(mapa.values().toArray(new Produto[0]));
    }

    public static Produto[] lerListaProdutosDoJson() {
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
