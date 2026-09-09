package org.steam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.steam.Produto;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

        Produto[] jogosAtuais = lerListaProdutosDoJson();

        Map<String, Produto> mapaJogos = new HashMap<>();
        for (Produto p : jogosAtuais) {
            mapaJogos.put(p.getAppid(), p);
        }

        mapaJogos.put(novoJogo.getAppid(), novoJogo);

        Produto[] listaAtualizada = mapaJogos.values().toArray(new Produto[0]);
        salvarListaProdutosComoJson(listaAtualizada);
    }

    // Lê os dados salvos do JSON. Se não existir, retorna um array vazio.
    public static Produto[] lerListaProdutosDoJson() {
        File arquivo = new File(CAMINHO_COMPLETO);
        if (!arquivo.exists()) {
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
