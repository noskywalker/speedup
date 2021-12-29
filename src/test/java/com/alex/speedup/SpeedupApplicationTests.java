package com.alex.speedup;

import com.alex.speedup.base.BaseTest;
import com.alex.speedup.biz.TestInvoke;
import com.alex.speedup.biz.TestService;
import com.alex.speedup.core.speed.Remote;
import com.alex.speedup.core.speed.junit.DelegateTestRunner;
import com.alex.speedup.mock.MockitoDIListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.when;

@RunWith(DelegateTestRunner.class)
@Remote(testRunner = SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { SpeedupApplication.class })
@TestExecutionListeners({MockitoDIListener.class })
@Lazy
public class SpeedupApplicationTests extends BaseTest {


    @Mock
    TestInvoke testInvoke;

    @Autowired
    @InjectMocks
    TestService testService;
    @Before
    public void init(){
        when(testInvoke.testInvoke()).thenReturn("alll");
    }
    @Test
    public void testUnit(){
        testService.test();
        testService.test2();
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");

    }
    @Test
    public void testUni2t(){
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxx");
    }

}
