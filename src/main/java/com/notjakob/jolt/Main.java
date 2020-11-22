package com.notjakob.jolt;

import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) {
        final String currentDirectory = System.getProperty("user.dir");
        final String envPath = currentDirectory + "/env.txt";
        final String modListPath = currentDirectory + "/mods.txt";
        final String overridesPath = currentDirectory + "/overrides";
        String tempPath = "";
        try {
            tempPath = Files.createTempDirectory("jolt-").toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to create temporary working directory. Is there enough space left on your disk?");
            System.exit(2);
        }
        String mcVersion = null;
        String forgeVersion = null;
        String packName = null;
        String packVersion = null;
        String packAuthor = null;

        //Util.buildManifest("1.16.3","34.1.4", "test", "1.0", "fyr77", modListPath, tempPath);

        if (!Util.checkInternet()) {
            System.out.println("Failed to connect to the internet.\nA working internet connection is required for this application to work.");
        }

        File envFile = new File(envPath);
        if (envFile.isFile()) {
            try {
                mcVersion = Files.readAllLines(Paths.get(envPath)).get(0);
                forgeVersion = Files.readAllLines(Paths.get(envPath)).get(1);
                packName = Files.readAllLines(Paths.get(envPath)).get(2);
                packVersion = Files.readAllLines(Paths.get(envPath)).get(3);
                packAuthor = Files.readAllLines(Paths.get(envPath)).get(4);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed to read all information. There might be lines of modpack details missing.");
                System.exit(255);
            }
            if (!Util.checkForge(mcVersion, forgeVersion)){
                System.out.println("Invalid Minecraft/Forge version combination.\nCheck your env.txt for typos.");
                System.exit(1);
            }
        }
        else {
            System.out.println("No environment details found.");
            System.out.println("Please create a file named \"env.txt\" and enter the following data:\nLine 1: Minecraft version (e.g. 1.16.3)\nLine 2: Forge version (e.g. 34.1.42)");
            System.exit(1);
        }

        File modlistFile = new File(modListPath);
        if (modlistFile.isFile()) {
            ConcurrentHashMap<Integer, Integer> mods = Util.getMods(mcVersion, modListPath);
            //call buildManifest for creating manifest.json
            //error handling is done in the functions themselves, because of the complexity and different errors requiring different messages.
            //TODO maybe it's possible to handle exceptions here with different exception contents
            Util.buildManifest(mcVersion, forgeVersion, packName, packVersion, packAuthor, tempPath, mods);

            //call buildHTML for creating modlist.html
            try {
                Util.buildHTML(mods, tempPath);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to create modlist in temporary directory. Please report this on GitHub: https://github.com/fyr77/jolt");
                System.exit(2);
            }

            //copy overrides to temp
            File overridesWorkDir = new File(overridesPath);
            File overridesTempDir = new File(tempPath + "/overrides");
            if (overridesWorkDir.isDirectory()) {
                try {
                    FileUtils.copyDirectory(overridesWorkDir, overridesTempDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to copy overrides to temporary directory. Please report this on GitHub: https://github.com/fyr77/jolt");
                    System.exit(2);
                }
            }
            else {
                overridesTempDir.mkdirs();
            }

            //finalize
            try {
                Filesystem.createZip(currentDirectory, packName, tempPath);
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("No modlist found.");
            System.out.println("Please create a file named \"mods.txt\" paste the links to your CurseForge mods in it.");
            System.exit(1);
        }
        Cleanup.deleteDirectory(tempPath);
    }
}
