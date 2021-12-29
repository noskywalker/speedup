package com.alex.speedup.base;

import com.alex.speedup.SpeedupApplication;
import com.alex.speedup.core.speed.Remote;
import com.alex.speedup.core.speed.junit.DelegateTestRunner;
import com.alex.speedup.mock.MockitoDIListener;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(DelegateTestRunner.class)
@Remote(testRunner = SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { SpeedupApplication.class })
@TestExecutionListeners({MockitoDIListener.class })
@Lazy
public class BaseTest {
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
