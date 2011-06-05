// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

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
