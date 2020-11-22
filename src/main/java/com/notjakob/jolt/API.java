package com.notjakob.jolt;

import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class API {
    /**
     * Converts the user-supplied URLs to cfwidget API URLs
     * @param urlString URL to convert
     * @return cfwidget API URL
     */
    private static String formatURL(String urlString) {
        String cleanURL = urlString.replaceAll("/files/\\d+","");
        if (cleanURL.endsWith("/")) {
            cleanURL = cleanURL.substring(0,cleanURL.length() - 1);
        }
        cleanURL = cleanURL.replaceFirst("[w.]*curseforge\\.com","api.cfwidget.com");
        return cleanURL;
    }

    /**
     * Gets project ID from URL using the cfwidget API
     * @param urlString URL to get project ID from
     * @return Project ID
     * @throws IOException If internet connection fails
     */
    public static int getProjectID(String urlString) throws IOException {
        String jsonUrl = formatURL(urlString);
        String jsonString = Util.downloadWeb(jsonUrl);
        JSONObject obj = new JSONObject(jsonString);
        return obj.getInt("id");
    }

    /**
     * Gets file ID from URL using the cfwidget API
     * @param urlString URL to get file ID from. If file URL is supplied, exact file will be used.
     * @param mcVersion Minecraft version to get the latest file for
     * @return File ID
     * @throws IOException If internet connection fails
     */
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
            String jsonString = Util.downloadWeb(jsonUrl);
            JSONObject obj = new JSONObject(jsonString);
            return obj.getJSONObject("download").getInt("id");
        }
        else {
            return fID;
        }
    }

    /**
     * Gets file ID from URL using the cfwidget API
     * @param projectID Project ID to get file ID for
     * @param mcVersion Minecraft version to get the latest file for
     * @return File ID
     * @throws IOException If internet connection fails
     */
    public static int getFileID(int projectID, String mcVersion) throws IOException {
        //TODO warn user if file version != mc version
        String jsonUrl = "https://api.cfwidget.com/minecraft/mc-mods/" + projectID + "?version=" + mcVersion;
        String jsonString = Util.downloadWeb(jsonUrl);
        JSONObject obj = new JSONObject(jsonString);
        return obj.getJSONObject("download").getInt("id");
    }

    public static String getModName(int projectID) throws IOException {
        String jsonUrl = "https://api.cfwidget.com/minecraft/mc-mods/" + projectID;
        String jsonString = Util.downloadWeb(jsonUrl);
        JSONObject obj = new JSONObject(jsonString);
        String name = obj.getString("title");
        String author = obj.getJSONArray("members").getJSONObject(0).getString("username");

        return name + "(by " + author + ")";
    }
}
