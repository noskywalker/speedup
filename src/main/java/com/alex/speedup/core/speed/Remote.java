package com.alex.speedup.core.speed;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Remote {
    String endpoint() default "http://localhost:7890/";

    Class<? extends Runner> testRunner() default BlockJUnit4ClassRunner.class;
}
