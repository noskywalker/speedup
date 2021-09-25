package com.alex.speedup.core.speed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ReadFile {
    private static final Logger log = LoggerFactory.getLogger(ReadFile.class);

    public ReadFile() {
    }

    public static String getClassPath() {
        String absolutePath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        String packageName = RemoteServer.class.getPackage().getName();
        packageName = packageName.replace(".", File.separator);
        String classpath = absolutePath.replace(packageName, "");
        log.info(classpath);
        return classpath;
    }

    public static void startReplaceDic(String dicFileName) throws IOException {
        String classPath = getClassPath();
        String absolutepath = classPath + dicFileName;
        replaceTxtByStr(absolutepath, "paoding.dic.home=../", "paoding.dic.home=");
    }

    public static void recoverReplaceDic(String dicFileName) throws IOException {
        String classPath = getClassPath();
        String absolutepath = classPath + dicFileName;
        replaceTxtByStr(absolutepath, "paoding.dic.home=", "paoding.dic.home=../");
    }

    public static void startReplaceFile(String oldFileName) throws IOException {
        String classPath = getClassPath();
        String absolutepath = classPath + oldFileName;
        readFile(absolutepath, oldFileName, 1, new File(absolutepath), false);
    }

    public static void recoverFile(String oldFileName) throws IOException {
        String classPath = getClassPath();
        String absolutepath = classPath + oldFileName;
        readFile(absolutepath, oldFileName, 1, new File(absolutepath), true);
    }

    public static void main(String[] args) throws IOException {
        String dicName = "paoding-dic-home.properties";
        startReplaceDic("paoding-dic-home.properties");
        recoverReplaceDic("paoding-dic-home.properties");
    }

    public static void readFile(String absolutepath, String filename, int index, File outPath, boolean isRecover) {
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(absolutepath), "GBK"));
            StringBuffer strBuffer = new StringBuffer();
            String empty = "";

            for(String temp = null; (temp = bufReader.readLine()) != null; temp = null) {
                if (!isRecover) {
                    if (temp.contains("<import resource") && temp.contains("file:../")) {
                        temp = temp.replace("../", empty);
                    }
                } else if (temp.contains("<import resource") && temp.contains("file:itemcenter")) {
                    temp = temp.replace("file:", "file:../");
                }

                strBuffer.append(temp);
                strBuffer.append(System.getProperty("line.separator"));
            }

            bufReader.close();
            if (!outPath.exists()) {
                outPath.createNewFile();
                log.info("已成功创建输出文件夹：" + outPath);
            }

            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outPath.getPath()), "GB2312"));
            printWriter.write(strBuffer.toString().toCharArray());
            printWriter.flush();
            printWriter.close();
            log.info("第 " + (index + 1) + " 个文件   " + absolutepath + "  已成功输出到    " + outPath.getName());
        } catch (Exception var9) {
            var9.printStackTrace();
        }

    }

    public static void replaceTxtByStr(String path, String oldStr, String replaceStr) {
        try {
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "GBK"));
            StringBuffer strBuffer = new StringBuffer();
            String empty = "";

            for(String temp = null; (temp = bufReader.readLine()) != null; temp = null) {
                if (temp.contains(oldStr)) {
                    temp = temp.replace(oldStr, replaceStr);
                }

                strBuffer.append(temp);
                strBuffer.append(System.getProperty("line.separator"));
            }

            bufReader.close();
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream((new File(path)).getPath()), "GB2312"));
            printWriter.write(strBuffer.toString().toCharArray());
            printWriter.flush();
            printWriter.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }
}
