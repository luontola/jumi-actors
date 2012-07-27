// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import java.util.*;

public class Threads {

    public static List<Thread> getActiveThreads(ThreadGroup threadGroup) {
        return getActiveThreads(threadGroup, 10);
    }

    static List<Thread> getActiveThreads(ThreadGroup threadGroup, int expectedCount) {
        Thread[] enumerated = new Thread[expectedCount];
        int actualCount = threadGroup.enumerate(enumerated, true);
        if (actualCount < enumerated.length) {
            return Arrays.asList(Arrays.copyOfRange(enumerated, 0, actualCount));
        } else {
            return getActiveThreads(threadGroup, expectedCount * 2);
        }
    }
}
