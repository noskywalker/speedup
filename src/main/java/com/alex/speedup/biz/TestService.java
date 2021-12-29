package com.alex.speedup.biz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:05 下午
 */
@Service
public class TestService {

    @Autowired(required = false)
    TestInvoke testInvoke;

    public void test(){
        System.out.println(testInvoke.testInvoke());
        System.out.println("TestService invoke");
    }
    public void test2(){
        System.out.println(testInvoke.testInvoke());
        System.out.println("TestService invoke22222");
    }
}
