package net.orfjackal.jumi.core;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class Hello {
    public static String hello() {
        // dependency to some external library - needs to be shaded
        IOUtils.closeQuietly((InputStream) null);

        return "Hello world";
    }
}
