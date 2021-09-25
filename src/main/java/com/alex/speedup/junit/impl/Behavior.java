package com.alex.speedup.junit.impl;

public interface Behavior {
    Class<?> loadClass(String var1) throws ClassNotFoundException;

    void before();

    void after();
}
