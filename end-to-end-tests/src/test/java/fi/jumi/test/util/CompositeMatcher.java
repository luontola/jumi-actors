// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.base.Joiner;
import org.hamcrest.*;

import java.util.*;

public class CompositeMatcher<T> {

    private final List<Matcher<T>> exclusions = new ArrayList<Matcher<T>>();
    private Matcher<T> assertion;
    private List<String> errorMessages = new ArrayList<String>();
    private StackTraceElement[] errorStackTrace;

    public CompositeMatcher<T> assertThatIt(Matcher<T> matcher) {
        assertion = matcher;
        return this;
    }

    public CompositeMatcher<T> excludeIf(Matcher<T> matcher) {
        exclusions.add(matcher);
        return this;
    }

    public void check(T item) {
        if (isIncluded(item)) {
            try {
                MatcherAssert.assertThat(item, assertion);
            } catch (AssertionError e) {
                errorMessages.add(describeItem(item) + "\n" + e.getMessage().trim());
                errorStackTrace = e.getStackTrace();
            }
        }
    }

    private boolean isIncluded(T item) {
        for (Matcher<T> exclusion : exclusions) {
            if (exclusion.matches(item)) {
                return false;
            }
        }
        return true;
    }

    public void rethrowErrors() {
        if (errorMessages.isEmpty()) {
            return;
        }
        AssertionError e = new AssertionError("\n" + Joiner.on("\n\n").join(errorMessages));
        e.setStackTrace(errorStackTrace);
        throw e;
    }

    protected String describeItem(T item) {
        return String.valueOf(item);
    }
}
