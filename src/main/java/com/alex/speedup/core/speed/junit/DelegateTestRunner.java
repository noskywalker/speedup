package com.alex.speedup.core.speed.junit;

import com.alex.speedup.core.common.CommonUtils;
import com.alex.speedup.core.speed.Remote;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;


/**
 * xianshuangzhang@gmail.com
 */
public class DelegateTestRunner extends Runner implements Filterable, Sortable {
    private static final Logger log = LoggerFactory.getLogger(DelegateTestRunner.class);
    private Runner delegate;

    public DelegateTestRunner(Class<?> clazz) throws InitializationError {
        Remote remote = (Remote) CommonUtils.findAnnotation(clazz, Remote.class);
        String endpoint;
        Class remoteRunnerClass;
        if (remote != null) {
            endpoint = remote.endpoint();
            remoteRunnerClass = remote.testRunner();
        } else {
            endpoint = "http://localhost:7890/";
            remoteRunnerClass = BlockJUnit4ClassRunner.class;
        }

        log.debug("Trying remote server {} with runner {}", endpoint, remoteRunnerClass.getName());
        if (this.isAnyRemoteUp(endpoint)) {
            this.delegate = new InternalRemoteRunner(clazz, endpoint, remoteRunnerClass);
        } else {
            this.delegate = CommonUtils.createRunner(remoteRunnerClass, clazz);
        }

    }

    private boolean isAnyRemoteUp(String eps) {
        String[] var2 = eps.split(",");
        int var3 = var2.length;
        int var4 = 0;

        while(var4 < var3) {
            String ep = var2[var4];
            URI uri = URI.create(ep.trim());

            try {
                Socket s = new Socket(uri.getHost(), uri.getPort());
                s.close();
                return true;
            } catch (IOException var8) {
                ++var4;
            }
        }

        return false;
    }

    public Description getDescription() {
        return this.delegate.getDescription();
    }

    public void run(RunNotifier notifier) {
        this.delegate.run(notifier);
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        CommonUtils.filter(this.delegate, filter);
    }

    public void sort(Sorter sorter) {
        CommonUtils.sort(this.delegate, sorter);
    }
}
