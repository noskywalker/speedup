package com.alex.speedup.junit.impl;

public class NopBehavior extends AbstractBehavior {
    public NopBehavior(Class<?> clazz) {
        this.processSystemProperties(clazz);
    }

    public NopBehavior() {
    }

    public void before() {
        super.before();
    }

    public void after() {
        super.after();
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return NopBehavior.class.getClassLoader().loadClass(name);
    }
}
