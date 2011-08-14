// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent;

import java.lang.instrument.Instrumentation;

public class PreMain {

    // For details on Java agents, see http://java.sun.com/javase/6/docs/api/java/lang/instrument/package-summary.html

    public static void premain(String agentArgs, Instrumentation inst) {
        // TODO: enable the agent once it works
        //inst.addTransformer(new ThreadSafetyCheckerTransformer());
    }
}
