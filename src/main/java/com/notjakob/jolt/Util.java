package com.notjakob.jolt;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
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
            Cleanup.errorOut += "Error occurred in network connection. Please report this on GitHub: https://github.com/fyr77/jolt\n";
            Cleanup.deleteDirectory(255);
        }
        return valid;
    }

    /**
     * Downloads a json into a string.
     * @param urlString URL to download from
     * @return the JSON, saved to a string
     * @throws IOException If internet connection fails
     */
    public static String downloadWeb(String urlString) throws IOException {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.setRequestProperty("User-Agent", "jolt (java)");
        InputStream in = con.getInputStream();
        String encoding = "UTF-8"; //force UTF-8, no need to check (I hope).
        return IOUtils.toString(in, encoding);
    }

    /**
     * Builds the manifest.json file for later inclusion in the final modpack zip
     * @param mcVersion Minecraft version of the modpack
     * @param forgeVersion Forge version of the modpack
     * @param packName Modpack name
     * @param packVersion Modpack version
     * @param packAuthor Modpack author
     * @param tempPath Path to the generated temp directory
     * @param mods Output of Util.getMods
     */
    public static void buildManifest(String mcVersion, String forgeVersion, String packName, String packVersion, String packAuthor, String tempPath, ConcurrentHashMap<Integer, Integer> mods) {
        JSONObject jo = new JSONObject();
        jo.put("minecraft", new JSONObject().put("version", mcVersion));
        JSONObject modLoaderForge = new JSONObject();
        modLoaderForge.put("id", "forge-" + forgeVersion);
        modLoaderForge.put("primary", true);
        JSONArray modLoaders = new JSONArray();
        modLoaders.put(modLoaderForge);
        jo.getJSONObject("minecraft").put("modLoaders", modLoaders);
        jo.put("manifestType", "minecraftModpack");
        jo.put("manifestVersion", 1);
        jo.put("name", packName);
        jo.put("version", packVersion);
        jo.put("author", packAuthor);
        jo.put("files", buildModlist(mods));
        jo.put("overrides", "overrides");

        //create manifest.json file in temp
        File manifest = new File(tempPath + "/manifest.json");
        try {
            if (manifest.createNewFile()) {
                Filesystem.writeStringToFile(jo.toString(),manifest);
            }
            else {
                throw new IOException("File already exists?");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Cleanup.errorOut += "Unable to create manifest in temporary directory. Please report this on GitHub: https://github.com/fyr77/jolt\n";
            Cleanup.deleteDirectory(2);
        }
    }

    /**
     * Creates modlist.html, which seems to be necessary for CurseForge.
     * @param mods Output of Util.getMods
     * @param tempPath Temporary working directory
     * @throws IOException Thrown if file could not be written
     */
    public static void buildHTML(ConcurrentHashMap<Integer, Integer> mods, String tempPath) throws IOException {
        StringBuilder htmlBuilder = new StringBuilder("<ul>\n");
        for (Map.Entry<Integer, Integer> entry : mods.entrySet()) {
            try {
                htmlBuilder.append("<li><a href=\"https://minecraft.curseforge.com/mc-mods/").append(entry.getKey()).append("\">").append(API.getModName(entry.getKey())).append("</a></li>\n");
            } catch (IOException e) {
                Cleanup.errorOut += "Unable to get info for project ID " + entry + "\n";
                e.printStackTrace();
            }
        }
        String html = htmlBuilder.toString();

        html += "</ul>";
        File modlist = new File(tempPath + "/modlist.html");
        Filesystem.writeStringToFile(html,modlist);
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
    public static ConcurrentHashMap<Integer, Integer> getMods(String mcVersion, String modListPath) {
        ConcurrentHashMap<Integer, Integer> hm = new ConcurrentHashMap<>();
        try {
            File f = new File(modListPath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String readLine;
            while ((readLine = b.readLine()) != null) {
                int pID = API.getProjectID(readLine);
                if (hm.containsKey(pID))
                    Cleanup.errorOut += "Duplicate mod! URL: " + readLine + " - not adding it to the modpack.\n";
                else
                    hm.put(pID, API.getFileID(readLine, mcVersion));
            }
            for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
                for(int pID : getDependencies(entry.getKey(), entry.getValue()))
                {
                    if (!hm.containsKey(pID)) {
                        hm.put(pID,API.getFileID(pID,mcVersion));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Cleanup.errorOut += "mods.txt is unreadable! How did this happen? Please report this on GitHub: https://github.com/fyr77/jolt\n";
            Cleanup.deleteDirectory(254);
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
            Cleanup.errorOut += "Network connection failed. Please check your internet connection.\n";
            Cleanup.deleteDirectory(255);
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
}
