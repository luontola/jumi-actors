// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import com.google.common.io.CharStreams;

import java.io.*;
import java.net.*;
import java.util.zip.*;

public class LibrarySourceLocator {

    public String findSources(String className) {
        String result = null;
        String sourceFilePath = className.replace('.', '/') + ".java";
        try {
            // classpath
            InputStream in = getClass().getClassLoader().getResourceAsStream(sourceFilePath);
            if (in != null) {
                return toString(in);
            }

            // JDK
            File javaHome = new File(System.getProperty("java.home"));
            File jdkSourcesFile = new File(javaHome.getParentFile(), "src.zip");
            if (jdkSourcesFile.isFile()) {
                result = findZipFileEntry(jdkSourcesFile, sourceFilePath);
            }

            // local Maven repository
            if (result == null) {
                File classesJar = findJarContaining(className);
                if (classesJar != null) {
                    File sourcesJar = new File(classesJar.getPath().replaceAll("\\.jar$", "-sources.jar"));
                    if (sourcesJar.isFile()) {
                        result = findZipFileEntry(sourcesJar, sourceFilePath);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding sources for " + className);
            e.printStackTrace();
        }
        return result;
    }

    private File findJarContaining(String className) throws MalformedURLException {
        URL resource = getClass().getClassLoader().getResource(className.replace('.', '/') + ".class");
        if (resource != null && resource.getProtocol().equals("jar")) {
            String path = new URL(resource.getPath()).getPath();
            File file = new File(path.substring(0, path.indexOf("!")));
            if (file.isFile()) {
                return file;
            }
        }
        return null;
    }

    private String findZipFileEntry(File zipFile, String entryName) throws IOException {
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            ZipEntry entry = zip.getEntry(entryName);
            if (entry == null) {
                return null;
            }
            return toString(zip.getInputStream(entry));
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    private static String toString(InputStream in) throws IOException {
        try {
            return CharStreams.toString(new InputStreamReader(in, "UTF-8"));
        } finally {
            in.close();
        }
    }
}
