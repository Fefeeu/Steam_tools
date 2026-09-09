package org;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();
    private static final String NOME_ARQUIVO = "config.properties";

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream(NOME_ARQUIVO)) {
            if (input == null) {
                System.out.println("Aviso: " + NOME_ARQUIVO
                        + " não encontrado em src/main/resources. Crie um a partir do config.properties.example");
            } else {
                props.load(input);
                System.out.println("Config carregada com sucesso.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler " + NOME_ARQUIVO + ": " + e.getMessage());
        }
    }

    public static String get(String chave) {
        return props.getProperty(chave);
    }
}