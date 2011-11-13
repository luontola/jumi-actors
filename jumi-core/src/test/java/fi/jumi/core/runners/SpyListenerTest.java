// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.api.drivers.TestId;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SpyListenerTest {

    private SpyListener<TestClassRunnerListener> spy = new SpyListener<TestClassRunnerListener>(TestClassRunnerListener.class);
    private TestClassRunnerListener listener = spy.getListener();

    @Test
    public void no_expectations() {
        spy.replay();

        passes();
    }

    // TODO: use a guinea pig interface instead of TestClassRunnerListener

    @Test
    public void matching_expectations() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);

        passes();
    }

    // TODO: write as white-box unit tests, instead of two "POS" and "NEG" test methods
    @Test
    public void throwable_arguments_are_matched_by_type_and_message_POS() {
        listener.onFailure(TestId.ROOT, new IllegalArgumentException("foo"));
        spy.replay();

        listener.onFailure(TestId.ROOT, new IllegalArgumentException("foo"));

        passes();
    }

    @Test
    public void throwable_arguments_are_matched_by_type_and_message_NEG() {
        listener.onFailure(TestId.ROOT, new IllegalArgumentException("foo"));
        spy.replay();

        listener.onFailure(TestId.ROOT, new IllegalArgumentException("bar"));

        fails();
    }

    @Test
    public void fails_if_wrong_method_is_called() {
        listener.onTestStarted(TestId.ROOT);
        spy.replay();

        listener.onTestFinished(TestId.ROOT);

        fails();
    }

    @Test
    public void fails_if_right_method_is_called_with_wrong_parameters() {
        listener.onTestStarted(TestId.of(1));
        spy.replay();

        listener.onTestFinished(TestId.of(2));

        fails();
    }

    @Test
    public void fails_if_right_calls_were_made_in_wrong_order() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        listener.onTestFinished(TestId.ROOT);
        listener.onTestStarted(TestId.ROOT);

        fails();
    }

    @Test
    public void fails_if_there_were_fewer_calls_than_expected() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        listener.onTestStarted(TestId.ROOT);

        fails();
    }

    @Test
    public void fails_if_there_were_more_calls_than_expected() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);

        fails();
    }

    @Test
    public void failure_messages_contain_an_ordered_list_of_all_expected_method_calls() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        String message = fails();
        assertThat(message, containsString("1. onTestStarted(TestId())"));
        assertThat(message, containsString("2. onTestFinished(TestId())"));
    }

    @Test
    public void failure_messages_contain_an_ordered_list_of_all_actual_method_calls() {
        spy.replay();

        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);

        String message = fails();
        assertThat(message, containsString("1. onTestStarted(TestId())"));
        assertThat(message, containsString("2. onTestFinished(TestId())"));
    }

    @Test
    public void failure_messages_highlight_the_problematic_calls() {
        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.ROOT);
        spy.replay();

        listener.onTestStarted(TestId.ROOT);
        listener.onTestFinished(TestId.of(1));

        String message = fails();
        assertThat(message, containsString("2. onTestFinished(TestId())\n" + SpyListener.ERROR_MARKER));
        assertThat(message, containsString("2. onTestFinished(TestId(1))\n" + SpyListener.ERROR_MARKER));
        assertThat(occurrencesOf(SpyListener.ERROR_MARKER, message), is(2));
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_call_replay_twice() {
        spy.replay();
        spy.replay();
    }


    // helper methods

    private void passes() {
        spy.verify();
    }

    private String fails() {
        try {
            spy.verify();
        } catch (AssertionError expected) {
            return expected.getMessage();
        }
        throw new AssertionError("expected to throw an AssertionError, but it did not");
    }

    private static int occurrencesOf(String needle, String haystack) {
        int count = 0;
        for (int pos = 0; pos < haystack.length(); pos++) {
            pos = haystack.indexOf(needle, pos);
            if (pos < 0) {
                break;
            }
            count++;
            pos = pos + needle.length();
        }
        return count;
    }
}
