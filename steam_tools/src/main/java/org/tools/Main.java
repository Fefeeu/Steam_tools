package org.tools;

import org.tools.steam.api.GameAPI;
import org.tools.steam.api.UserAPI;

public class Main {
    static void main() {
        System.out.println(GameAPI.getGame("250900").toString()
        );
    }
}
