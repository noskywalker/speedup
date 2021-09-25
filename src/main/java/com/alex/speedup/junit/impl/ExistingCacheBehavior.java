package com.alex.speedup.junit.impl;

public class ExistingCacheBehavior extends AbstractBehavior {
    private ClassLoader classLoader;
    private ClassLoader oldClassLoader;

    public ExistingCacheBehavior(Class<?> clazz) {
        this.processSystemProperties(clazz);
        System.out.println("\n[PandoraBootRunner] Pandora is already setup, reuse it.");
        this.classLoader = PandoraBootStarter.getClassLoader();
    }

    public ExistingCacheBehavior() {
        this.classLoader = PandoraBootStarter.getClassLoader();
    }

    public void before() {
        this.oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.classLoader);
        super.before();
    }

    public void after() {
        if (this.oldClassLoader != null) {
            Thread.currentThread().setContextClassLoader(this.oldClassLoader);
        }

        super.after();
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }
}