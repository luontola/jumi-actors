// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import java.io.File;
import java.net.*;
import java.util.List;

public class FileSystemTestClassFinder implements TestClassFinder {
    // TODO: cover with unit tests

    private final List<File> classPath;
    private final String testsToIncludePattern;

    public FileSystemTestClassFinder(List<File> classPath, String testsToIncludePattern) {
        this.classPath = classPath;
        this.testsToIncludePattern = testsToIncludePattern;
    }

    public void findTestClasses(TestClassFinderListener listener) {
        System.out.println("classPath = " + classPath);
        System.out.println("testsToIncludePattern = " + testsToIncludePattern);

        try {
            // TODO: find all test classes from classpath
            URLClassLoader loader = new URLClassLoader(asUrls(classPath));
            Class<?> testClass = loader.loadClass(testsToIncludePattern);
            System.out.println("testClass = " + testClass);

            listener.onTestClassFound(testClass);

        } catch (MalformedURLException e) {
            // TODO: use sneaky throw? http://blog.jayway.com/2010/01/29/sneaky-throw/
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL[] asUrls(List<File> files) throws MalformedURLException {
        URL[] urls = new URL[files.size()];
        for (int i = 0, filesLength = files.size(); i < filesLength; i++) {
            urls[i] = files.get(i).toURI().toURL();
        }
        return urls;
    }
}
