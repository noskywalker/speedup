package com.alex.speedup.core.speed.junit;

import com.alex.speedup.core.common.CommonUtils;
import jdk.nashorn.internal.ir.annotations.Ignore;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class InternalRemoteRunner extends BlockJUnit4ClassRunner {
    private static final Logger log = LoggerFactory.getLogger(InternalRemoteRunner.class);
    private static List<String> endpoints = new ArrayList();
    private static int currentEndpoint = 0;
    private Description description;
    private Map<Description, String> methodNames = new HashMap();
    private final Class<?> testClass;
    private Class<? extends Runner> remoteRunnerClass;
    private static ExecutorService executorService;
    private static AtomicInteger runningCount = new AtomicInteger(0);
    private List<Description> fChildren = new ArrayList();

    public InternalRemoteRunner(Class<?> testClass, String endpoint, Class<? extends Runner> remoteRunnerClass) throws InitializationError {
        super(testClass);
        this.testClass = testClass;
        this.remoteRunnerClass = remoteRunnerClass;
        TestClass tc = new TestClass(testClass);
        this.description = Description.createTestDescription(testClass, tc.getName(), tc.getAnnotations());
        Iterator iterator = tc.getAnnotatedMethods(Test.class).iterator();

        while(iterator.hasNext()) {
            FrameworkMethod method = (FrameworkMethod)iterator.next();
            String methodName = method.getName();
            Description child = Description.createTestDescription(testClass, methodName, method.getAnnotations());
            this.methodNames.put(child, methodName);
            this.fChildren.add(child);
        }

        if (executorService == null) {
            String ep = System.getProperty("junit.remote.endpoint");
            if (ep == null) {
                ep = endpoint;
            }

            String[] eps = ep.split(",");
            int epsLength = eps.length;

            for(int i = 0; i < epsLength; ++i) {
                String e = eps[i];
                if (!e.trim().equals("")) {
                    endpoints.add(e.trim());
                }
            }

            executorService = Executors.newFixedThreadPool(endpoints.size());
        }

        this.setScheduler(new RunnerScheduler() {
            public void schedule(Runnable childStatement) {
                InternalRemoteRunner.runningCount.incrementAndGet();
                InternalRemoteRunner.executorService.submit(childStatement);
            }

            public void finished() {
                while(InternalRemoteRunner.runningCount.get() > 0) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException var2) {
                        Thread.currentThread().interrupt();
                    }
                }

            }
        });
    }

    protected List<FrameworkMethod> getChildren() {
        Collection<String> values = this.methodNames.values();
        List<FrameworkMethod> unmodifiedList = this.computeTestMethods();
        List<FrameworkMethod> list = new ArrayList();
        list.addAll(unmodifiedList);
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            FrameworkMethod frameworkMethod = (FrameworkMethod)iterator.next();
            if (!values.contains(frameworkMethod.getMethod().getName())) {
                iterator.remove();
            }
        }

        return list;
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        List<Description> children = this.fChildren;
        Iterator itr = children.iterator();

        while(itr.hasNext()) {
            Description child = (Description)itr.next();
            if (!filter.shouldRun(child)) {
                itr.remove();
                this.methodNames.remove(child);
            }
        }

        if (children.isEmpty()) {
            throw new NoTestsRemainException();
        } else {
            Iterator iterator = children.iterator();

            while(iterator.hasNext()) {
                Description tmpdescription = (Description)iterator.next();
                this.description.addChild(tmpdescription);
            }

        }
    }

    public void sort(Sorter sorter) {
        Collections.sort(this.description.getChildren(), sorter);
    }

    public Description getDescription() {
        return this.description;
    }

    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        Description description = this.describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
            runningCount.decrementAndGet();
        } else {
            HttpURLConnection connection = null;
            InputStream is = null;

            try {
                notifier.fireTestStarted(description);
                String methodName = method.getName();
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
                    } else {
                        if (line.startsWith("RSUCCESS")) {
                            break;
                        }

                        if (line.startsWith("RERROR")) {
                            StringBuilder error = new StringBuilder(line.substring(6));

                            while((line = reader.readLine()) != null) {
                                error.append(line).append("\n");
                            }

                            throw new AssertionFailedError(error.toString());
                        }

                        log.error("Protocol  in response: {}", line);
                    }
                }
            } catch (Throwable var19) {
                notifier.fireTestFailure(new Failure(description, var19));
            } finally {
                try {
                    is.close();
                } catch (IOException var18) {
                    System.err.println(var18);
                }

                connection.disconnect();
                notifier.fireTestFinished(description);
                runningCount.decrementAndGet();
            }
        }

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

    private HttpURLConnection getUrl(String methodName, String httpMethod) {
        int count = 0;

        while(count < endpoints.size() * 2) {
            String ep = (String)endpoints.get(currentEndpoint++ % endpoints.size());
            if (!ep.endsWith("/")) {
                ep = ep + "/";
            }

            try {
                HttpURLConnection connection = (HttpURLConnection)(new URL(ep + this.testClass.getName() + "?method=" + URLEncoder.encode(methodName, "UTF-8") + "&runner=" + URLEncoder.encode(this.remoteRunnerClass.getName(), "UTF-8"))).openConnection();
                connection.setReadTimeout(3600000);
                connection.setAllowUserInteraction(false);
                connection.setUseCaches(false);
                connection.setRequestMethod(httpMethod);
                connection.setRequestProperty("Connection", "close");
                connection.connect();
                return connection;
            } catch (MalformedURLException var6) {
                throw new RuntimeException("Unable to create remote url", var6);
            } catch (ConnectException var7) {
                log.warn("Skipping host {}", ep);
                ++count;
            } catch (IOException var8) {
                throw new RuntimeException("Unable to connect", var8);
            }
        }

        throw new RuntimeException("No hosts available");
    }

    protected Statement childrenInvoker(RunNotifier notifier) {
        return super.childrenInvoker(notifier);
    }
}
