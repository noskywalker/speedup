package com.alex.speedup.core.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public FileUtils() {
    }

    public static String getPathSeparator() {
        return (String)System.getProperties().get("file.separator");
    }

    public static void writeStrToFile(String data, String filePath, String fileName) throws IOException {
        File file = new File(filePath + getPathSeparator() + fileName);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        try {
            bw.write(data);
        } catch (IOException var6) {
            log.error("write data to " + fileName + "field, " + var6.getMessage());
        }

        bw.flush();
        bw.close();
    }

    public String readDataFromFile() {
        return null;
    }

    public static void main(String[] args) {
        System.out.println("分隔符： " + getPathSeparator());
    }
}