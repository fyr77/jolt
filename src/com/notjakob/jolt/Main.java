package com.notjakob.jolt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        String currentDirectory = System.getProperty("user.dir");
        String envPath = currentDirectory + "\\env.txt";
        String modListPath = currentDirectory + "\\mods.txt";
        String mcVersion = null;
        String forgeVersion = null;

        System.out.println("The current working directory is " + currentDirectory + ".");
        Util.buildManifest("1.16.3","34.1.4", "test", "1.0", "fyr77", modListPath);

        if (!Util.checkInternet()) {
            System.out.println("Failed to connect to the internet.\nA working internet connection is required for this application to work.");
        }

        File envFile = new File(envPath);
        if (envFile.isFile()) {
            try {
                mcVersion = Files.readAllLines(Paths.get(envPath)).get(0);
                forgeVersion = Files.readAllLines(Paths.get(envPath)).get(1);
            } catch (IOException e) {
                e.printStackTrace();
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
            //TODO get mods and dependencies

        }
        else {
            System.out.println("No modlist found.");
            System.out.println("Please create a file named \"mods.txt\" paste the links to your CurseForge mods in it.");
            System.exit(1);
        }
    }
}
