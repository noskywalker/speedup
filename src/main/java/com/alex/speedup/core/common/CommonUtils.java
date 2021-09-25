package com.alex.speedup.core.common;

import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

public class CommonUtils {
    private static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

    public CommonUtils() {
    }

    public static void filter(Runner runner, Filter filter) throws NoTestsRemainException {
        try {
            runner.getClass().getMethod("filter", Filter.class).invoke(runner, filter);
        } catch (InvocationTargetException var3) {
            if (var3.getCause() instanceof NoTestsRemainException) {
                throw (NoTestsRemainException)var3.getCause();
            } else {
                throw new RuntimeException(var3.getTargetException());
            }
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    public static void sort(Runner runner, Sorter sorter) {
        try {
            runner.getClass().getMethod("sort", Sorter.class).invoke(runner, sorter);
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String toString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[16384];

        int read;
        do {
            read = in.read(b);
            if (read > 0) {
                out.write(b, 0, read);
            }
        } while(read != -1);

        return out.toString();
    }

    public static Runner createRunner(String runnerClassName, Class<?> testClass) {
        try {
            return createRunner((Class<? extends Runner>) Class.forName(runnerClassName), testClass);
        } catch (ClassNotFoundException var3) {
            throw new RuntimeException("Unable to create instance of " + runnerClassName, var3);
        }
    }

    public static Runner createRunner(Class<? extends Runner> runnerClass, Class<?> testClass) {
        try {
            Constructor<? extends Runner> c = runnerClass.getDeclaredConstructor(Class.class);
            return (Runner)c.newInstance(testClass);
        } catch (NoSuchMethodException var5) {
            try {
                return (Runner)runnerClass.newInstance();
            } catch (Exception var4) {
                throw new RuntimeException("Unable to create instanceof " + runnerClass, var5);
            }
        } catch (Exception var6) {
            throw new RuntimeException("Unable to create instanceof " + runnerClass, var6);
        }
    }

    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        } else {
            Class[] var3 = clazz.getInterfaces();
            int var4 = var3.length;

            int var5;
            Class superClass;
            for(var5 = 0; var5 < var4; ++var5) {
                superClass = var3[var5];
                annotation = findAnnotation(superClass, annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }

            if (!Annotation.class.isAssignableFrom(clazz)) {
                Annotation[] var7 = clazz.getAnnotations();
                var4 = var7.length;

                for(var5 = 0; var5 < var4; ++var5) {
                    Annotation ann = var7[var5];
                    annotation = findAnnotation(ann.annotationType(), annotationType);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }

            superClass = clazz.getSuperclass();
            return superClass != null && !superClass.equals(Object.class) ? findAnnotation(superClass, annotationType) : null;
        }
    }

    public static boolean isUrl(String path) throws Exception {
        log.info("check file path " + path);

        try {
            new URL(path);
            return true;
        } catch (MalformedURLException var3) {
            File local = new File(path);
            if (local.exists() && local.isFile()) {
                return false;
            } else {
                throw new Exception("invalid path :" + path);
            }
        }
    }
}