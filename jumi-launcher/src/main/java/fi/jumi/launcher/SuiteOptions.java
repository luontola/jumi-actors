// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.util.*;

@NotThreadSafe
public class SuiteOptions {

    // TODO: support for main and test class paths

    public final List<File> classPath = new ArrayList<File>();
    public final List<String> jvmOptions = new ArrayList<String>();
    public final Properties systemProperties = new Properties();
    public String testsToIncludePattern;
}
