package com.alex.speedup.junit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogOutputCapature {
    public static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    public static final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut;
    private static final PrintStream originalErr;
    private static Map<String, Short> logCapatureTests;

    public LogOutputCapature() {
    }

    public static void changeLogOutputTarget(Class<?> clazz) {
        LogCapature logCapature = (LogCapature)clazz.getAnnotation(LogCapature.class);
        if (logCapature != null) {
            logCapatureTests.put(clazz.getName(), Short.valueOf((short)0));
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }
    }

    public static void resetLogOutput(Class<?> clazz) {
        if (logCapatureTests.size() == 1 && logCapatureTests.containsKey(clazz.getName())) {
            logCapatureTests.remove(clazz.getName());
            System.setOut(originalOut);
            System.setErr(originalErr);
        } else {
            logCapatureTests.remove(clazz.getName());
        }

    }

    static {
        originalOut = System.out;
        originalErr = System.err;
        logCapatureTests = new ConcurrentHashMap();
    }
}