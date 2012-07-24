// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core;

import java.io.File;
import java.util.List;

public interface CommandListener {

    void addSuiteListener(SuiteListener listener);

    void runTests(List<File> classPath, String testsToIncludePattern);

    void shutdown();
}
