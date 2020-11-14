package com.notjakob.jolt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

public class Util {
    public static boolean checkInternet() {
        boolean connected = false;
        try {
            URL url = new URL("https://github.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            connected = true;
        } catch (IOException ignored) { }
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

    private static String downloadWeb(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "jolt (java)");
        InputStream in = con.getInputStream();
        String encoding = "UTF-8"; //force UTF-8, no need to check (I hope).
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192]; //This might overflow in the future, if a mod has too many files.
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), encoding);
    }

    private static String formatURL(String urlString) {
        String cleanURL = urlString.replaceAll("/files/\\d+","");
        if (cleanURL.endsWith("/")) {
            cleanURL = cleanURL.substring(0,cleanURL.length() - 1);
        }
        cleanURL = cleanURL.replaceFirst("[w.]*curseforge\\.com","api.cfwidget.com");
        return cleanURL;
    }

    public static int getProjectID(String urlString) throws IOException {
        String jsonUrl = formatURL(urlString);
        String jsonString = downloadWeb(jsonUrl);
        JSONObject obj = new JSONObject(jsonString);
        return obj.getInt("id");
    }

    public static int getFileID(String urlString, String mcVersion) throws IOException {
        //TODO warn user if file version != mc version
        int fID = 0;

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(urlString);
        while(m.find()) {
            fID = Integer.parseInt(m.group());
        }
        if (fID == 0) {
            String jsonUrl = formatURL(urlString) + "?version=" + mcVersion;
            String jsonString = downloadWeb(jsonUrl);
            JSONObject obj = new JSONObject(jsonString);
            return obj.getJSONObject("download").getInt("id");
        }
        else {
            return fID;
        }
    }

    public static void buildManifest(String mcVersion, String forgeVersion) {
        JSONObject jo = new JSONObject();
        jo.put("minecraft", new JSONObject().put("version", mcVersion));
        jo.getJSONObject("minecraft").put("modLoaders",new JSONObject().put("id",forgeVersion));
        jo.getJSONObject("minecraft").getJSONObject("modLoaders").put("primary", true);
        System.out.println(jo.toString());
        //TODO build whole file
    }

    public static void buildModlist() {
        //TODO
    }

    public static void createZip() {
        //TODO
    }
}
