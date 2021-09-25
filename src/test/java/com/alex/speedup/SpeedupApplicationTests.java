package com.alex.speedup;

import com.alex.speedup.core.speed.Remote;
import com.alex.speedup.core.speed.junit.DelegateTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(DelegateTestRunner.class)
@Remote(testRunner = SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { SpeedupApplication.class })
@Lazy
public class SpeedupApplicationTests {
    @Test
    public void testUnit(){
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("=================================");
    }

}
