package com.notjakob.jolt;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Cleanup {
    /**
     * Recursively deletes directory
     * @param deleteDir Directory to delete
     */
    public static void deleteDirectory(String deleteDir) {
        try {
            File dir = new File(deleteDir);
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            System.out.println("Unable to delete temporary directory...");
            ex.printStackTrace();
        }
    }
}
