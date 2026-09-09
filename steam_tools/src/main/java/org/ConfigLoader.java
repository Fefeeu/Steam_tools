package org;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream input = new FileInputStream("config.proconfigperties")) {
            props.load(input);
        } catch (IOException e) {
            System.out.println("\n\n\n\nAviso: config.properties não encontrado. Crie um a partir do config.properties.example");
        }
    }

    public static String get(String chave) {
        return props.getProperty(chave);
    }
}
