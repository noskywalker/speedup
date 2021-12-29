package com.alex.speedup.core.speed.testing;

import com.alex.speedup.core.common.CommonUtils;
import com.alex.speedup.core.common.ConnectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.web.ServletTestExecutionListener;
import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * xianshuangzhang@gmail.com
 */
@TestExecutionListeners({ServletTestExecutionListener.class, DirtiesContextBeforeModesTestExecutionListener.class, DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public class DelegateTests implements IHookable, ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(DelegateTests.class);
    public ApplicationContext applicationContext;
    public final TestContextManager testContextManager = new TestContextManager(this.getClass());
    private Throwable testException;
    private boolean isRemote = false;
    Map<String, Boolean> methodHasRun = new ConcurrentHashMap();
    MutiThreadTaskQueue queue = new MutiThreadTaskQueue();
    AtomicInteger successCounter = new AtomicInteger(0);
    AtomicInteger faileCounter = new AtomicInteger(0);

    public DelegateTests() {
        Class remoteClass = this.getClass();
        log.debug("Trying remote server {} with runner {}", "http://localhost:7890/", remoteClass.getName());
        log.info("DelegateTests Thread.currentThread().getName() : {}", Thread.currentThread().getName());
        if (ConnectUtils.shouldCallRemote("http://localhost:7890/")) {
            this.setRemote(true);
        } else {
            this.setRemote(false);
        }

    }

    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @BeforeClass(
        alwaysRun = true
    )
    protected void springTestContextBeforeTestClass() throws Exception {
        if (!this.isRemote()) {
            this.testContextManager.beforeTestClass();
        }

    }

    @BeforeClass(
        alwaysRun = true,
        dependsOnMethods = {"springTestContextBeforeTestClass"}
    )
    protected void springTestContextPrepareTestInstance() throws Exception {
        if (!this.isRemote()) {
            this.testContextManager.prepareTestInstance(this);
        }

    }

    @BeforeMethod(
        alwaysRun = true
    )
    protected void springTestContextBeforeTestMethod(Method testMethod) throws Exception {
        if (!this.isRemote()) {
            this.testContextManager.beforeTestMethod(this, testMethod);
        }

    }

    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (!this.isRemote()) {
            callBack.runTestMethod(testResult);
            Throwable testResultException = testResult.getThrowable();
            if (testResultException instanceof InvocationTargetException) {
                testResultException = ((InvocationTargetException)testResultException).getCause();
            }

            this.testException = testResultException;
        } else {
            synchronized(this) {
                if (this.methodHasRun.containsKey(testResult.getName())) {
                    this.handleTaskResult();
                    return;
                }

                this.methodHasRun.put(testResult.getName(), true);
            }

            HttpURLConnection connection = null;
            InputStream is = null;

            try {
                String methodName = testResult.getMethod().getMethodName();
                connection = this.getUrl(methodName, "POST");
                this.handleError(connection);
                String enc = connection.getContentEncoding();
                if (enc == null) {
                    enc = "UTF-8";
                }

                is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName(enc)));
                String line = null;

                while((line = reader.readLine()) != null) {
                    if (line.startsWith("E")) {
                        log.error(line.substring(1).trim());
                    } else if (line.startsWith("O")) {
                        log.info(line.substring(1).trim());
                    } else if (line.startsWith("RSUCCESS")) {
                        this.successCounter.incrementAndGet();
                        log.info("执行数量, 成功 : {}, 失败 : {}, 总共 : {}", new Object[]{this.successCounter.get(), this.faileCounter.get(), this.successCounter.get() + this.faileCounter.get()});
                        this.queue.add(new MutiThreadTaskQueue.TaskResult(callBack, testResult, true, ""));
                        log.info(line.substring(8).trim());
                    } else if (line.startsWith("RERROR")) {
                        this.faileCounter.incrementAndGet();
                        log.info("执行数量, 成功 : {}, 失败 : {}, 总共 : {}", new Object[]{this.successCounter.get(), this.faileCounter.get(), this.successCounter.get() + this.faileCounter.get()});
                        this.queue.add(new MutiThreadTaskQueue.TaskResult(callBack, testResult, false, line.substring(6).replace("||", "\n").trim()));
                    } else {
                        log.info(line);
                    }
                }

                this.handleTaskResult();
            } catch (IOException var18) {
                Assert.fail(ExceptionUtils.getStackTrace(var18));
            } finally {
                try {
                    is.close();
                } catch (IOException var17) {
                    System.err.println(var17);
                }

                connection.disconnect();
            }

            log.info("总共执行数量, 成功 : {}, 失败 : {}, 总共 : {}", new Object[]{this.successCounter.get(), this.faileCounter.get(), this.successCounter.get() + this.faileCounter.get()});
        }

    }

    @AfterMethod(
        alwaysRun = true
    )
    protected void springTestContextAfterTestMethod(Method testMethod) throws Exception {
        if (!this.isRemote()) {
            try {
                this.testContextManager.afterTestMethod(this, testMethod, this.testException);
            } finally {
                this.testException = null;
            }
        }

    }

    @AfterClass(
        alwaysRun = true
    )
    protected void springTestContextAfterTestClass() throws Exception {
        if (!this.isRemote()) {
            this.testContextManager.afterTestClass();
        }

    }

    private HttpURLConnection getUrl(String methodName, String httpMethod) {
        HttpURLConnection result = null;

        try {
            result = (HttpURLConnection)(new URL("http://localhost:7890/" + this.getClass().getName() + "?method=" + URLEncoder.encode(methodName, "UTF-8") + "&runner=" + URLEncoder.encode(this.getClass().getName(), "UTF-8") + "&type=" + URLEncoder.encode("testng", "UTF-8"))).openConnection();
            result.setReadTimeout(3600000);
            result.setAllowUserInteraction(false);
            result.setUseCaches(false);
            result.setRequestMethod(httpMethod);
            result.setRequestProperty("Connection", "close");
            result.connect();
        } catch (MalformedURLException var5) {
            throw new RuntimeException("Unable to create remote url", var5);
        } catch (ConnectException var6) {
            log.warn("Skipping host http://localhost:7890/", var6);
        } catch (IOException var7) {
            throw new RuntimeException("Unable to connect", var7);
        }

        return result;
    }

    private void handleError(HttpURLConnection connection) throws IOException {
        if (connection.getResponseCode() != 200) {
            String error = null;
            InputStream err = connection.getErrorStream();
            if (err != null) {
                error = CommonUtils.toString(err);
            }

            if (error == null) {
                error = connection.getResponseMessage();
            }

            throw new RuntimeException("Unable to send request to " + connection.getURL() + ": " + error);
        }
    }

    private void handleTaskResult() {
        MutiThreadTaskQueue.TaskResult taskResult = this.queue.poll();
        if (!taskResult.isSuccess()) {
            Assert.fail(taskResult.getMessage());
        } else {
            Test test = (Test)taskResult.getTestResult().getMethod().getConstructorOrMethod().getMethod().getDeclaredAnnotation(Test.class);
            Class[] expectedExceptions = test.expectedExceptions();
            String expectedExceptionsMessageRegExp = test.expectedExceptionsMessageRegExp();
            if (null != expectedExceptions && 0 < expectedExceptions.length) {
                try {
                    RuntimeException c = (RuntimeException)expectedExceptions[0].newInstance();
                    Field message = this.getMessageField(expectedExceptions[0]);
                    message.setAccessible(true);
                    message.set(c, expectedExceptionsMessageRegExp);
                    throw c;
                } catch (InstantiationException var7) {
                    var7.printStackTrace();
                } catch (IllegalAccessException var8) {
                    var8.printStackTrace();
                }
            }
        }

    }

    public void setRemote(boolean remote) {
        this.isRemote = remote;
    }

    public boolean isRemote() {
        return this.isRemote;
    }

    private Field getMessageField(Class clazz) {
        Field result = null;

        for(Class tempClass = clazz; tempClass != null; tempClass = tempClass.getSuperclass()) {
            Field[] var4 = tempClass.getDeclaredFields();
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Field field = var4[var6];
                if ("detailMessage".equals(field.getName())) {
                    result = field;
                    break;
                }
            }

            if (null != result) {
                break;
            }
        }

        return result;
    }
}