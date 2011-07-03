// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.test.simpleunit;

import net.orfjackal.jumi.core.*;
import org.junit.Test;
import sample.*;

import java.util.Collection;
import java.util.concurrent.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class SimpleUnitTest {
    private static final long TIMEOUT = 1000;

    private SuiteListener dummyListener = mock(SuiteListener.class);
    private ExecutorService executor = Executors.newCachedThreadPool();
    private SimpleUnit driver = new SimpleUnit();

    @Test
    public void the_test_class_is_named_after_its_simple_name() throws InterruptedException {
        Class<OnePassingTest> testClass = OnePassingTest.class;
        TestClassRunner runner = new TestClassRunner(dummyListener, testClass);
        driver.findTests(testClass, runner.getSuiteNotifier(), executor);
        waitForTestsToExecute();

        assertThat(runner.getTestNames(), hasItem("OnePassingTest"));
    }

    @Test
    public void the_tests_are_methods_whose_name_starts_with_test() throws InterruptedException {
        Class<OnePassingTest> testClass = OnePassingTest.class;
        TestClassRunner runner = new TestClassRunner(dummyListener, testClass);
        driver.findTests(testClass, runner.getSuiteNotifier(), executor);
        waitForTestsToExecute();

        Collection<String> testNames = runner.getTestNames();
        assertThat(testNames, hasItem("testPassing"));
        assertThat(testNames.size(), is(2)); // one root plus the one passing test
    }

    private void waitForTestsToExecute() throws InterruptedException {
        // XXX: for a differnt kind of testing framework, shutdown() won't work, because it prevents new tasks from being added
        executor.shutdown();
        executor.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS);
    }

    // TODO: these tests will probably need to be rewritten; they should also serve as an example of how to write tests for a testing framework
}
