// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.test.simpleunit;

import net.orfjackal.jumi.api.drivers.SuiteNotifier;
import net.orfjackal.jumi.core.notifiers.DefaultSuiteNotifier;
import org.junit.*;
import sample.*;

import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SimpleUnitTest {
    private TestResultCollector collector = new TestResultCollector();
    private SuiteNotifier notifier;
    private Executor executor;

    @Ignore
    @Test
    public void tests_are_methods_which_start_with_test() {
        SimpleUnit driver = new SimpleUnit();
        driver.findTests(OnePassingTest.class, collector.getSuiteNotifier(), executor);

        List<String> testNames = collector.getTestNames();
        assertThat(testNames, contains("testPassing"));
    }
}

class TestResultCollector {

    public List<String> getTestNames() {
        return null;
    }

    public SuiteNotifier getSuiteNotifier() {
        // TODO: parameterize the suite notifer with the test class
        return new DefaultSuiteNotifier();
    }
}
