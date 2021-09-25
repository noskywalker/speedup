package com.alex.speedup.core.speed;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Remote {
    String endpoint() default "http://localhost:7890/";

    Class<? extends Runner> testRunner() default BlockJUnit4ClassRunner.class;
}
