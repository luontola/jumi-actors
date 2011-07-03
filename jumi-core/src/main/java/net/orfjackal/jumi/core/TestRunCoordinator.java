// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.RunVia;
import net.orfjackal.jumi.api.drivers.Driver;

import java.io.File;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

public class TestRunCoordinator implements CommandListener {

    private static final long TIMEOUT = 1000;

    // TODO: support for multiple clients?
    private SuiteListener listener = null;

    public void addSuiteListener(SuiteListener listener) {
        this.listener = listener;
    }

    public void runTests(List<File> classPath, String testsToIncludePattern) {
        listener.onSuiteStarted();

        System.out.println("classPath = " + classPath);
        System.out.println("testsToIncludePattern = " + testsToIncludePattern);

        try {
            // TODO: find tests (async)
            URLClassLoader loader = new URLClassLoader(asUrls(classPath));
            Class<?> testClass = loader.loadClass(testsToIncludePattern);
            System.out.println("testClass = " + testClass);

            // TODO: run the tests (async)
            TestClassRunner runner = new TestClassRunner(listener, testClass);
            ExecutorService executor = Executors.newCachedThreadPool();

            RunVia runVia = testClass.getAnnotation(RunVia.class);
            Driver driver = runVia.value().newInstance();
            driver.findTests(testClass, runner.getSuiteNotifier(), executor);

            // TODO: wait for the suite to finish (async)
            executor.shutdown();
            executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
        }

        listener.onSuiteFinished();
    }

    private static URL[] asUrls(List<File> files) throws MalformedURLException {
        URL[] urls = new URL[files.size()];
        for (int i = 0, filesLength = files.size(); i < filesLength; i++) {
            urls[i] = files.get(i).toURI().toURL();
        }
        return urls;
    }
}
