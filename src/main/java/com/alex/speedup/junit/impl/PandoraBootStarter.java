package com.alex.speedup.junit.impl;

import java.util.Map;

public class PandoraBootStarter {
    private static Map<String, Class<?>> exportedClassMap;
    private static ClassLoader classLoader;

    public PandoraBootStarter() {
    }

    public static Map<String, Class<?>> getExportedClassMap() {
        return exportedClassMap;
    }

    public static void setExportedClassMap(Map<String, Class<?>> map) {
        exportedClassMap = map;
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public static void setClassLoader(ClassLoader classLoader) {
        PandoraBootStarter.classLoader = classLoader;
    }
}
