package com.alex.speedup.junit;

import com.alex.speedup.junit.impl.Behavior;
import com.alex.speedup.junit.impl.DefaultBehavior;
import com.alex.speedup.junit.impl.ExistingCacheBehavior;
import com.alex.speedup.junit.impl.NopBehavior;
import com.alex.speedup.junit.impl.PandoraBootStarter;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;

import java.lang.reflect.Constructor;
import java.util.Map;

public class PandoraBootRunner extends Runner implements Filterable {
    public static final String[] SELF_LOAD_PACKAGES = new String[]{"org.junit.", "org.hamcrest.", "com.taobao.pandora.boot.test.junit4.", "com.taobao.pandora.boot.test.junit5.GroupSpringExtension"};
    public static final String[] SELF_LOAD_PACKAGES_EXCLUDES = new String[]{"org.junit.platform.launcher.core.DefaultLauncher"};
    public static final String PANDORA_SKIP = "pandora.skip";
    private Runner runner;
    private Behavior behavior;
    private Class<?> testClass;
    ClassLoader pandoraClassLoader = null;

    public PandoraBootRunner(Class<?> testClass) throws Exception {
        LogOutputCapature.changeLogOutputTarget(testClass);
        if (System.getProperty("pandora.skip") != null && !"false".equalsIgnoreCase(System.getProperty("pandora.skip"))) {
            this.behavior = new NopBehavior(testClass);
        } else {
            this.behavior = this.prepareClassLoader(testClass);
        }

        if (this.pandoraClassLoader != null) {
            ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(this.pandoraClassLoader);
                this.runner = this.prepareDelegateRunner(testClass, this.behavior);
            } finally {
                Thread.currentThread().setContextClassLoader(oldTccl);
            }
        } else {
            this.runner = this.prepareDelegateRunner(testClass, this.behavior);
        }

        this.testClass = testClass;
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        filter.apply(this.runner);
    }

    public Description getDescription() {
        return this.runner.getDescription();
    }

    public void run(RunNotifier notifier) {
        try {
            this.behavior.before();
            this.runner.run(notifier);
        } finally {
            this.behavior.after();
            LogOutputCapature.resetLogOutput(this.testClass);
        }

    }

    private Runner prepareDelegateRunner(Class<?> clazz, Behavior classLoader) throws Exception {
        Class<?> runnerClass = classLoader.loadClass(JUnit4.class.getName());
        DelegateTo delegateTo = (DelegateTo)clazz.getAnnotation(DelegateTo.class);
        if (delegateTo != null) {
            runnerClass = classLoader.loadClass(delegateTo.value().getName());
        }

        Class<?> testClassToInvoke = classLoader.loadClass(clazz.getName());
        Constructor<?> constructor = runnerClass.getConstructor(Class.class);
        constructor.setAccessible(true);
        return (Runner)constructor.newInstance(testClassToInvoke);
    }

    private Behavior prepareClassLoader(Class<?> clazz) throws Exception {
        Map<String, Class<?>> cache = findClassCache();
        boolean share = true;
        Sar sarInfo = (Sar)clazz.getAnnotation(Sar.class);
        if (sarInfo != null) {
            share = sarInfo.share();
        }

        Behavior behavior = null;
        DefaultBehavior defaultBehavior;
        if (cache == null) {
            defaultBehavior = new DefaultBehavior(clazz);
            this.pandoraClassLoader = defaultBehavior.getPandoraClassLoader();
            behavior = defaultBehavior;
        } else if (share) {
            behavior = new ExistingCacheBehavior(clazz);
        } else {
            defaultBehavior = new DefaultBehavior(clazz);
            this.pandoraClassLoader = defaultBehavior.getPandoraClassLoader();
            behavior = defaultBehavior;
        }

        return (Behavior)behavior;
    }

    private static Map<String, Class<?>> findClassCache() throws NoSuchFieldException, IllegalAccessException {
        return PandoraBootStarter.getExportedClassMap();
    }
}