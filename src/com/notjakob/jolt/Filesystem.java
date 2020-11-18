package com.notjakob.jolt;

import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.ZipFile;

public class Filesystem {
    /**
     * Writes a string to a file
     * @param data String to write
     * @param output File to write to
     * @throws IOException Thrown if file not writable
     */
    public static void writeStringToFile(String data, File output) throws IOException {
        String encoding = "UTF-8";
        FileUtils.writeStringToFile(output, data, encoding);
    }

    /**
     * Creates final zip from files in temp
     * @param zipPath Path to save the generated zip file at
     * @param zipName Name of the zip file
     * @param tempPath Application temporary path
     * @throws ZipException Thrown when generation fails
     */
    public static void createZip(String zipPath, String zipName, String tempPath) throws ZipException {
        ZipFile zip = new ZipFile(zipPath + "/" + zipName + ".zip");
        zip.addFile(tempPath + "/manifest.json");
        zip.addFile(tempPath + "/modlist.html");
        zip.addFolder(new File(tempPath + "/overrides"));
    }
}
