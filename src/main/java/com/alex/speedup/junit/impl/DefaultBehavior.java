package com.alex.speedup.junit.impl;

public class DefaultBehavior extends AbstractBehavior {
    private ClassLoader pandoraClassLoader;
    private ClassLoader oldClassLoader;

    public DefaultBehavior(Class<?> clazz) {
        this.processSystemProperties(clazz);
//        PandoraLazyExportUtils.tryEnablePandoraLazyExport();
        this.processSar(clazz);
    }

    public DefaultBehavior(ClassLoader classLoader) {
//        PandoraLazyExportUtils.tryEnablePandoraLazyExport();
        this.processSar(classLoader);
    }

    public ClassLoader getPandoraClassLoader() {
        return this.pandoraClassLoader;
    }

    public void before() {
        if (this.pandoraClassLoader != null) {
            this.oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.pandoraClassLoader);
        }

        super.before();
    }

    public void after() {
        if (this.oldClassLoader != null) {
            Thread.currentThread().setContextClassLoader(this.oldClassLoader);
        }

        super.after();
    }

    private void processSar(Class<?> clazz) {
        this.processSar(clazz.getClassLoader());
    }

    private void processSar(ClassLoader originalClassLoader) {
//        URL[] urls = ClassLoaderUtils.getUrls(originalClassLoader);
//        urls = AutoConfigWrapper.autoConfig(urls);
//
//        try {
//            ReLaunchURLClassLoader classLoader = new ReLaunchURLClassLoader(urls, PandoraBootRunner.SELF_LOAD_PACKAGES, PandoraBootRunner.SELF_LOAD_PACKAGES_EXCLUDES, ClassLoader.getSystemClassLoader().getParent());
//            Archive sar = SarLoaderUtils.findExternalSar();
//            if (sar == null) {
//                sar = SarLoaderUtils.findFromClassPath(urls);
//            }
//
//            if (sar == null) {
//                throw new RuntimeException("can not load taobao-hsf.sar.jar from classpath, please check your pom config!");
//            }
//
//            Map<String, Class<?>> cache = SarLoaderUtils.getClassCache(sar, classLoader);
//            classLoader.setClassCache(cache);
//            PandoraBootStarter.setExportedClassMap(cache);
//            PandoraBootStarter.setClassLoader(classLoader);
//            SarLoaderUtils.markSarLoaderUtils(classLoader, "sarLoaded", true);
//            this.pandoraClassLoader = classLoader;
//        } catch (Throwable var6) {
//            AnsiLog.error("[PandoraBootRunner] failed to start pandora");
//            var6.printStackTrace();
//        }

    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.pandoraClassLoader != null ? this.pandoraClassLoader.loadClass(name) : DefaultBehavior.class.getClassLoader().loadClass(name);
    }
}