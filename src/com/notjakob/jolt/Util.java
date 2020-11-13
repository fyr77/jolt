package com.notjakob.jolt;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class Util {
    public static boolean checkInternet() {
        boolean connected = false;
        try {
            URL url = new URL("https://github.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            connected = true;
        } catch (IOException e) {
            connected = false;
        }
        return connected;
    }
    public static boolean checkForge(String mcVersion, String forgeVersion) {
        boolean valid = false;
        String forgeUrl = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + mcVersion + "-" + forgeVersion + "/forge-" + mcVersion + "-" + forgeVersion + "-installer.jar";
        try {
            URL url = new URL(forgeUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            if (response == 200 || response == 301 || response == 302 || response == 307 || response == 308) {
                valid = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occurred in application. Please report this on GitHub: https://github.com/fyr77/jolt");
            System.exit(255);
        }
        return valid;
    }
    public void buildManifest(String mcVersion, String forgeVersion) {
        //TODO
    }
}
