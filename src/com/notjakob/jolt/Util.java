package com.notjakob.jolt;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.*;

public class Util {
    /**
     * Checks for internet connection by trying to connect to GitHub
     * @return True, if connection successful
     */
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

    /**
     * Checks if a Minecraft/Forge combination exists using the given parameters
     * @param mcVersion Minecraft version number, e.g. 1.16.3
     * @param forgeVersion Forge version , e.g. 34.1.4
     * @return True, if combination is valid
     */
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
            System.out.println("Error occurred in network connection. Please report this on GitHub: https://github.com/fyr77/jolt");
            System.exit(255);
        }
        return valid;
    }

    /**
     * Downloads a json into a string.
     * @param urlString URL to download from
     * @return the JSON, saved to a string
     * @throws IOException If internet connection fails
     */
    private static String downloadWeb(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "jolt (java)");
        InputStream in = con.getInputStream();
        String encoding = "UTF-8"; //force UTF-8, no need to check (I hope).
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192]; //This might overflow in the future, if a mod has too many files.
        int len;
        while ((len = in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), encoding);
    }

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
    private static int getProjectID(String urlString) throws IOException {
        String jsonUrl = formatURL(urlString);
        String jsonString = downloadWeb(jsonUrl);
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
    private static int getFileID(String urlString, String mcVersion) throws IOException {
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

    /**
     * Gets file ID from URL using the cfwidget API
     * @param projectID Project ID to get file ID for
     * @param mcVersion Minecraft version to get the latest file for
     * @return File ID
     * @throws IOException If internet connection fails
     */
    private static int getFileID(int projectID, String mcVersion) throws IOException {
        //TODO warn user if file version != mc version
        String jsonUrl = "https://api.cfwidget.com/minecraft/mc-mods/" + projectID + "?version=" + mcVersion;
        String jsonString = downloadWeb(jsonUrl);
        JSONObject obj = new JSONObject(jsonString);
        return obj.getJSONObject("download").getInt("id");
    }

    /**
     * Builds the manifest.json file for later inclusion in the final modpack zip
     * @param mcVersion Minecraft version of the modpack
     * @param forgeVersion Forge version of the modpack
     * @param packName Modpack name
     * @param packVersion Modpack version
     * @param packAuthor Modpack author
     * @param modListPath Path to the mods.txt file
     */
    public static void buildManifest(String mcVersion, String forgeVersion, String packName, String packVersion, String packAuthor, String modListPath) {
        JSONObject jo = new JSONObject();
        ConcurrentHashMap<Integer, Integer> mods = getMods(mcVersion, modListPath);
        jo.put("minecraft", new JSONObject().put("version", mcVersion));
        jo.getJSONObject("minecraft").put("modLoaders",new JSONObject().put("id",forgeVersion));
        jo.getJSONObject("minecraft").getJSONObject("modLoaders").put("primary", true);
        jo.put("manifestType", "minecraftModpack");
        jo.put("manifestVersion", 1);
        jo.put("name", packName);
        jo.put("version", packVersion);
        jo.put("author", packAuthor);
        jo.put("files", buildModlist(mods));
        jo.put("overrides", "overrides");

        System.out.println(jo.toString());
        //TODO save to file for ZIP
    }

    public static void buildHTML() {

    }

    /**
     * Builds the "files" part of the manifest.json
     * @param mods All mods, including dependencies, as a ConcurrentHashMap
     * @return JSONArray ready to be inserted into the manifest.json
     */
    private static JSONArray buildModlist(ConcurrentHashMap<Integer, Integer> mods) {
        JSONArray ja = new JSONArray();
        for (Map.Entry<Integer, Integer> entry : mods.entrySet()) {
            JSONObject jo = new JSONObject();
            jo.put("projectID",entry.getKey());
            jo.put("fileID",entry.getValue());
            jo.put("required", true);

            ja.put(jo);
        }

        return ja;
    }

    /**
     * Gets mods from file and their dependencies and sorts them into a ConcurrentHashMap
     * @param mcVersion Minecraft version
     * @param modListPath Path to the mods.txt file
     * @return Modlist as a ConcurrentHashMap
     */
    private static ConcurrentHashMap<Integer, Integer> getMods(String mcVersion, String modListPath) {
        ConcurrentHashMap<Integer, Integer> hm = new ConcurrentHashMap<>();
        try {
            File f = new File(modListPath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine;
            while ((readLine = b.readLine()) != null) {
                int pID = getProjectID(readLine);
                if (hm.containsKey(pID)) {
                    System.out.println("Possible duplicate mod! URL: " + readLine + " - adding it anyway.");
                }
                hm.put(pID, getFileID(readLine, mcVersion));
            }
            for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
                for(int pID : getDependencies(entry.getKey(), entry.getValue()))
                {
                    if (!hm.containsKey(pID)) {
                        hm.put(pID,getFileID(pID,mcVersion));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("mods.txt is unreadable! How did this happen? Please report this on GitHub: https://github.com/fyr77/jolt");
            System.exit(254);
        }
        return hm;
    }

    /**
     * Resolves dependencies of the mods safely
     * @param projectID Project ID
     * @param fileID File ID to resolve dependencies for
     * @return List of mod dependencies
     */
    private static List<Integer> getDependencies(int projectID, int fileID) {
        //TODO dependency is not available for mcVersion - rare case probably.
        List<Integer> pIDs = new ArrayList<>();

        String jsonString = null;
        try {
            jsonString = downloadWeb("https://addons-ecs.forgesvc.net/api/v2/addon/" + projectID + "/file/" + fileID);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Network connection failed. Please check your internet connection.");
            System.exit(255);
        }
        JSONArray depJO = new JSONObject(jsonString).getJSONArray("dependencies");
        for (int i=0; i < depJO.length(); i++) {
            JSONObject obj = depJO.getJSONObject(i);
            if (obj.getInt("type") == 3) {
                //Type 3: Required dependency
                //Type 4: "Tool" - optional
                //TODO Find out about other types
                pIDs.add(obj.getInt("addonId"));
            }
        }
        return pIDs;
    }

    public static void createZip() {
        //TODO
    }
}
