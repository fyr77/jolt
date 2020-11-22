package com.notjakob.jolt;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Cleanup {
    public static String errorOut = "";

    /**
     * Delete temp and exit.
     * @param exitCode Application exit code
     */
    public static void deleteDirectory(int exitCode) {
        String currentDirectory = System.getProperty("user.dir");
        try {
            File dir = new File(Main.tempPath);
            FileUtils.deleteDirectory(dir);

        } catch (IOException ex) {
            errorOut += "Unable to delete temporary directory...";
            ex.printStackTrace();
        }
        if (errorOut != "") {
            File errorFile = new File(currentDirectory + "/errors.txt");
            try {
                Filesystem.writeStringToFile(errorOut, errorFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File workingFile = new File(currentDirectory + "/working please wait.txt");
        if (workingFile.isFile()) {
            File doneFile = new File(currentDirectory + "/done.txt");
            try {
                doneFile.createNewFile();
            } catch (IOException ignored) {}
            workingFile.delete();
        }
        System.exit(exitCode);
    }
}
