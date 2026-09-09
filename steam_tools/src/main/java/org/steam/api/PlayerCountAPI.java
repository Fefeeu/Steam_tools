package org.steam.api;

import org.json.JSONObject;

public class PlayerCountAPI {

    public static int getCurrentPlayers(String appid) {
        String request = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?appid=" + appid;
        JSONObject obj = ApiConnection.makeRequestBody(request);

        if (!obj.has("response")) {
            return -1;
        }
        return obj.getJSONObject("response").getInt("player_count");
    }
}
