// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.actors.*;
import fi.jumi.core.*;
import fi.jumi.core.events.suite.SuiteListenerToEvent;
import fi.jumi.launcher.ui.TextUI;
import org.junit.*;

import java.io.*;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TextUIParserTest {

    private static final int RUN_1 = 1;
    private static final int RUN_2 = 2;

    private final MessageQueue<Event<SuiteListener>> stream = new MessageQueue<Event<SuiteListener>>();
    private final SuiteListener listener = new SuiteListenerToEvent(stream);

    @Test
    public void get_number_of_passing_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(textUI().getPassingCount(), is(0));

        SuiteMother.onePassingTest(listener);
        assertThat(textUI().getPassingCount(), is(1));
    }

    @Test
    public void get_number_of_failing_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(textUI().getFailingCount(), is(0));

        SuiteMother.oneFailingTest(listener);
        assertThat(textUI().getFailingCount(), is(1));
    }

    @Test
    public void get_total_number_of_tests() {
        SuiteMother.emptySuite(listener);
        assertThat(textUI().getTotalCount(), is(0));

        SuiteMother.onePassingTest(listener);
        assertThat(textUI().getTotalCount(), is(1));

        SuiteMother.oneFailingTest(listener);
        assertThat(textUI().getTotalCount(), is(1));
    }

    @Test
    public void get_test_start_and_end_events() {
        SuiteMother.onePassingTest(listener);
        assertThat(textUI().getTestStartAndEndEvents(RUN_1), is(asList("DummyTest", "/")));

        SuiteMother.nestedFailingAndPassingTests(listener);
        assertThat(textUI().getTestStartAndEndEvents(RUN_1), is(asList("DummyTest", "testOne", "/", "testTwo", "/", "/")));
    }

    @Test
    @Ignore // TODO
    public void distinguishes_between_multiple_runs() {
        SuiteMother.twoPassingRuns(listener);
        assertThat(textUI().getTestStartAndEndEvents(RUN_1), is(asList("DummyTest", "testOne", "/", "/")));

        SuiteMother.twoPassingRuns(listener);
        assertThat(textUI().getTestStartAndEndEvents(RUN_2), is(asList("DummyTest", "testTwo", "/", "/")));
    }

    // TODO: start and end events for multiple runs

    private TextUIParser textUI() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TextUI ui = new TextUI(new PrintStream(out), new PrintStream(out), stream);
        ui.update();
        return new TextUIParser(out.toString());
    }
}
