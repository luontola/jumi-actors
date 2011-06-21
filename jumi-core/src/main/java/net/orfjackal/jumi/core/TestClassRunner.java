// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.core;

import net.orfjackal.jumi.api.drivers.*;

import java.util.*;

public class TestClassRunner {

    private final Map<TestId, String> tests = new HashMap<TestId, String>();
    private final SuiteListener listener;

    public TestClassRunner(SuiteListener listener) {
        this.listener = listener;
    }

    public Collection<String> getTestNames() {
        return Collections.unmodifiableCollection(tests.values());
    }

    public SuiteNotifier getSuiteNotifier() {
        // TODO: parameterize the suite notifer with the test class
        return new DefaultSuiteNotifier();
    }

    private class DefaultSuiteNotifier implements SuiteNotifier {

        public void fireTestFound(TestId id, String name) {
            if (hasNotBeenFoundBefore(id)) {
                checkParentWasFoundFirst(id);
                tests.put(id, name);
                listener.onTestFound(id, name);
            } else {
                checkNameIsSameAsBefore(id, name);
            }
        }

        private boolean hasNotBeenFoundBefore(TestId id) {
            return !tests.containsKey(id);
        }

        private void checkParentWasFoundFirst(TestId id) {
            if (!id.isRoot() && hasNotBeenFoundBefore(id.getParent())) {
                throw new IllegalStateException("parent of " + id + " must be found first");
            }
        }

        private void checkNameIsSameAsBefore(TestId id, String newName) {
            String oldName = tests.get(id);
            if (oldName != null && !oldName.equals(newName)) {
                throw new IllegalStateException("test " + id + " was already found with another name: " + oldName);
            }
        }

        public TestNotifier fireTestStarted(TestId id) {
            return null; // TODO
        }
    }

    // TODO: forward messages to TestSuiteRunner
    // TODO: executing tests, firing an event when the class is done?
}
