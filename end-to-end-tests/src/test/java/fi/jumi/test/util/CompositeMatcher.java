// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.base.Joiner;
import org.hamcrest.*;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

public class CompositeMatcher {

    private final List<Matcher<ClassNode>> exclusions = new ArrayList<Matcher<ClassNode>>();
    private Matcher<ClassNode> assertion;
    private List<String> errorMessages = new ArrayList<String>();
    private StackTraceElement[] errorStackTrace;

    public CompositeMatcher assertThatIt(Matcher<ClassNode> matcher) {
        assertion = matcher;
        return this;
    }

    public CompositeMatcher excludeIf(Matcher<ClassNode> matcher) {
        exclusions.add(matcher);
        return this;
    }

    public void check(ClassNode cn) {
        if (isIncluded(cn)) {
            try {
                MatcherAssert.assertThat(cn, assertion);
            } catch (AssertionError e) {
                errorMessages.add(e.getMessage());
                errorStackTrace = e.getStackTrace();
            }
        }
    }

    private boolean isIncluded(ClassNode cn) {
        for (Matcher<ClassNode> exclusion : exclusions) {
            if (exclusion.matches(cn)) {
                return false;
            }
        }
        return true;
    }

    public void rethrowErrors() {
        if (errorMessages.isEmpty()) {
            return;
        }
        AssertionError e = new AssertionError(Joiner.on("").join(errorMessages));
        e.setStackTrace(errorStackTrace);
        throw e;
    }
}
