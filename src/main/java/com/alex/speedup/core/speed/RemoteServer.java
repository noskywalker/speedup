package com.alex.speedup.core.speed;

import com.alex.speedup.core.common.CommonUtils;
import com.google.common.base.Strings;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author xianshuangzhang@gmail.com
 * @date 2021-12-29 3:03 下午
 */
public class RemoteServer {
    private static final Logger log = LoggerFactory.getLogger(RemoteServer.class);
    private static RedirectingStream out;
    private static RedirectingStream err;
    public static ThreadLocal<ServletOutputStream> servletOutputStreamThreadLocal = new InheritableThreadLocal();
    public static AtomicInteger successCounter = new AtomicInteger(0);
    public static AtomicInteger failCounter = new AtomicInteger(0);
    public static byte[] blankBytes;
    public static Queue<String> errorQueue = new ConcurrentLinkedQueue();
    public static Queue<String> successQueue = new ConcurrentLinkedQueue();

    public RemoteServer() {
    }

    private static void withStream(OutputStream os, RemoteServer.Task task) throws Exception {
        try {
            blankBytes = "\n".getBytes("utf-8");
        } catch (UnsupportedEncodingException var7) {
            throw new RuntimeException(var7);
        }

        try {
            out.setRedirector(new LineBreakingStream('O', os));
            err.setRedirector(new LineBreakingStream('E', os));
            task.run();
        } finally {
            out.setRedirector((OutputStream)null);
            err.setRedirector((OutputStream)null);
        }

    }

    private static Class<?> getTestClass(HttpServletRequest request) throws ClassNotFoundException {
        String testClassName = request.getPathInfo().substring(request.getPathInfo().lastIndexOf(47) + 1);
//        Class<?> testClass2=Thread.currentThread().getContextClassLoader().loadClass(testClassName);
        Class<?> testClass = Class.forName(testClassName);
        return testClass;
    }

    public static void run(String[] args, final AbstractAction action) throws Exception {
        if (action != null) {
            log.info("onJvmStart ...");
            action.onJvmStart();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        RemoteServer.log.info("onJvmExit ...");
                        action.onJvmExit();
                    } catch (IOException var2) {
                        RemoteServer.log.info("speedup.Action.onJvmSuspend " + var2.getMessage());
                    }

                }
            });
        }

        out = new RedirectingStream(System.out);
        System.setOut(new PrintStream(out));
        err = new RedirectingStream(System.err);
        System.setErr(new PrintStream(err));
        Options opts = new Options();
        CmdLineParser parser = new CmdLineParser(opts);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException var6) {
            System.err.println(var6.getMessage());
            parser.printUsage(System.err);
            System.exit(-1);
        }

        Server server = new Server(opts.getPort());
        Connector connector = new SocketConnector();
        connector.setPort(opts.getPort());
        server.setConnectors(new Connector[]{connector});
        server.setHandler(new AbstractHandler() {
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
                try {
                    RemoteServer.log.info("看到这条日志说明TC是通过server运行的");
                    RemoteServer.successCounter = new AtomicInteger(0);
                    RemoteServer.failCounter = new AtomicInteger(0);
                    RemoteServer.errorQueue.clear();
                    RemoteServer.successQueue.clear();
                    RemoteServer.log.debug("post request");
                    Class<?> testClass = RemoteServer.getTestClass(request);
                    String method = request.getMethod();
                    RemoteServer.log.debug(testClass.getName() + "::" + method);
                    response.setBufferSize(512);
                    response.setHeader("Content-type", "text/html;charset=UTF-8");
                    response.setCharacterEncoding("UTF-8");
                    ServletOutputStream pw = response.getOutputStream();
                    RemoteServer.servletOutputStreamThreadLocal.set(pw);
                    if (!"POST".equalsIgnoreCase(method)) {
                        return;
                    }

                    response.setStatus(200);
                    response.flushBuffer();
                    RemoteServer.log.info("RemoteServer Thread.currentThread().getName() : {}", Thread.currentThread().getName());
                    if (Strings.isNullOrEmpty(request.getParameter("type"))) {
                        RemoteServer.handleJUnit(request, pw, testClass);
                    } else {
                        RemoteServer.handleTestNG(request, pw, testClass);
                    }

                    return;
                } catch (ClassNotFoundException var12) {
                    RemoteServer.log.info("找不到类", ExceptionUtils.getStackTrace(var12));
                    response.sendError(500, "RERROR" + var12.getMessage());
                } catch (Throwable var13) {
                    RemoteServer.log.info("RemoteServer其他错误", var13);
                    return;
                } finally {
                    baseRequest.setHandled(true);
                }

            }
        });
        server.start();
        log.info("Server running at http://localhost:" + opts.getPort());
        server.join();
    }

    private static void handleTestNG(HttpServletRequest request, ServletOutputStream pw, Class<?> testClass) throws IOException {
        String testMethodName = URLDecoder.decode(request.getParameter("method"), "UTF-8");
        final TestNG testNG = new TestNG();
        testNG.setOutputDirectory("target/test-out");
        XmlSuite xmlSuite = new XmlSuite();
        xmlSuite.setName(testClass.getName());
        xmlSuite.setVerbose(XmlSuite.DEFAULT_VERBOSE);
        XmlTest xmlTest = new XmlTest();
        xmlTest.setName(testClass.getName());
        xmlTest.setVerbose(XmlSuite.DEFAULT_VERBOSE);
        xmlTest.setSuite(xmlSuite);
        XmlClass xmlClass = new XmlClass();
        xmlClass.setName(testClass.getName());
        XmlInclude xmlInclude = new XmlInclude(testMethodName);
        xmlClass.setIncludedMethods(Collections.singletonList(xmlInclude));
        xmlTest.setClasses(Collections.singletonList(xmlClass));
        xmlSuite.addTest(xmlTest);
        testNG.setCommandLineSuite(xmlSuite);

        try {
            testNG.setListenerClasses(Collections.singletonList(RemoteServer.TestNGListener.class));
            withStream(pw, new RemoteServer.Task() {
                public void run() {
                    testNG.run();
                }
            });
        } catch (Exception var10) {
            log.info("执行出错", var10);
        }

    }

    private static void handleJUnit(HttpServletRequest request, ServletOutputStream pw, Class<?> testClass) throws IOException {
        final Runner runner = CommonUtils.createRunner(URLDecoder.decode(request.getParameter("runner"), "UTF-8"), testClass);
        String methodName = URLDecoder.decode(request.getParameter("method"), "UTF-8");
        if (request.getParameter("method") != null) {
            try {
                CommonUtils.filter(runner, Filter.matchMethodDescription(Description.createTestDescription(testClass, methodName)));
            } catch (NoTestsRemainException var8) {
                pw.println("RERRORNo tests remaining");
            }
        }

        try {
            RemoteServer.TestListener listener = new RemoteServer.TestListener();
            final RunNotifier notifier = new RunNotifier();
            notifier.addListener(listener);
            withStream(pw, new RemoteServer.Task() {
                public void run() {
                    runner.run(notifier);
                }
            });
            pw.write(listener.getResult().getBytes("utf-8"));
        } catch (Exception var7) {
            log.info("执行出错", var7);
        }

    }

    public static class TestNGListener implements ITestListener {
        public TestNGListener() {
        }

        public void onTestStart(ITestResult iTestResult) {
            RemoteServer.log.info("TestNGListener - onTestStart");
        }

        public void onTestSuccess(ITestResult iTestResult) {
            RemoteServer.log.info("TestNGListener - onTestSuccess");
            RemoteServer.failCounter.incrementAndGet();
            String result = "RSUCCESS";
            RemoteServer.errorQueue.add(result);
        }

        public void onTestFailure(ITestResult iTestResult) {
            RemoteServer.log.info("TestNGListener - onTestFailure");
            RemoteServer.failCounter.incrementAndGet();
            String result = "RERROR" + ExceptionUtils.getStackTrace(iTestResult.getThrowable()).replace("\n", "||");
            RemoteServer.errorQueue.add(result);
        }

        public void onTestSkipped(ITestResult iTestResult) {
            RemoteServer.log.info("TestNGListener - onTestSkipped");
        }

        public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
            RemoteServer.log.info("TestNGListener - onTestFailedButWithinSuccessPercentage");
        }

        public void onStart(ITestContext iTestContext) {
            RemoteServer.log.info("TestNGListener - onStart");
        }

        public void onFinish(ITestContext iTestContext) {
            ServletOutputStream pw = (ServletOutputStream)RemoteServer.servletOutputStreamThreadLocal.get();
            RemoteServer.successQueue.stream().forEach((success) -> {
                try {
                    pw.write(RemoteServer.blankBytes);
                    pw.write(success.getBytes("utf-8"));
                    pw.write(RemoteServer.blankBytes);
                } catch (Exception var3) {
                    RemoteServer.log.info("onFinish - Exception", var3);
                }

            });
            RemoteServer.errorQueue.stream().forEach((error) -> {
                try {
                    pw.write(RemoteServer.blankBytes);
                    pw.write(error.getBytes("utf-8"));
                    pw.write(RemoteServer.blankBytes);
                } catch (Exception var3) {
                    RemoteServer.log.info("onFinish - Exception", var3);
                }

            });
            RemoteServer.log.info("TestNGListener - onFinish - errorQueueCount : {}, total : {}", RemoteServer.errorQueue.size(), RemoteServer.successCounter.get() + RemoteServer.failCounter.get());
        }
    }

    private static class TestListener extends RunListener {
        private String result;

        private TestListener() {
            this.result = "RSUCCESS";
        }

        public String getResult() {
            return this.result;
        }

        public void testFailure(Failure failure) throws Exception {
            this.result = "RERROR" + failure.getTrace();
        }

        public void testAssumptionFailure(Failure failure) {
        }

        public void testIgnored(Description description) throws Exception {
        }
    }

    private interface Task {
        void run() throws Exception;
    }
}
