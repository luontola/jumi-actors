// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SpyListenerTest {

    private SpyListener<DummyListener> spy = new SpyListener<DummyListener>(DummyListener.class);
    private DummyListener listener = spy.getListener();

    @Test
    public void no_expectations() {
        spy.replay();

        passes();
    }

    @Test
    public void matching_expectations() {
        listener.onFirst();
        listener.onSecond();
        spy.replay();

        listener.onFirst();
        listener.onSecond();

        passes();
    }

    // TODO: write as white-box unit tests, instead of two "POS" and "NEG" test methods
    @Test
    public void throwable_arguments_are_matched_by_type_and_message_POS() {
        listener.onException(new IllegalArgumentException("foo"));
        spy.replay();

        listener.onException(new IllegalArgumentException("foo"));

        passes();
    }

    @Test
    public void throwable_arguments_are_matched_by_type_and_message_NEG() {
        listener.onException(new IllegalArgumentException("foo"));
        spy.replay();

        listener.onException(new IllegalArgumentException("bar"));

        fails();
    }

    @Test
    public void fails_if_wrong_method_is_called() {
        listener.onFirst();
        spy.replay();

        listener.onSecond();

        fails();
    }

    @Test
    public void fails_if_right_method_is_called_with_wrong_parameters() {
        listener.onParameter(1);
        spy.replay();

        listener.onParameter(2);

        fails();
    }

    @Test
    public void fails_if_right_calls_were_made_in_wrong_order() {
        listener.onFirst();
        listener.onSecond();
        spy.replay();

        listener.onSecond();
        listener.onFirst();

        fails();
    }

    @Test
    public void fails_if_there_were_fewer_calls_than_expected() {
        listener.onFirst();
        listener.onSecond();
        spy.replay();

        listener.onFirst();

        fails();
    }

    @Test
    public void fails_if_there_were_more_calls_than_expected() {
        listener.onFirst();
        listener.onSecond();
        spy.replay();

        listener.onFirst();
        listener.onSecond();
        listener.onSecond();

        fails();
    }

    @Test
    public void fails_if_an_overloaded_version_of_the_expected_method_was_called() {
        listener.overloadedMethod(1);
        spy.replay();

        listener.overloadedMethod(1, 2);

        fails();
    }

    @Test
    public void failure_messages_contain_an_ordered_list_of_all_expected_method_calls() {
        listener.onFirst();
        listener.onSecond();
        spy.replay();

        String message = fails();
        assertThat(message, containsString("1. onFirst()"));
        assertThat(message, containsString("2. onSecond()"));
    }

    @Test
    public void failure_messages_contain_an_ordered_list_of_all_actual_method_calls() {
        spy.replay();

        listener.onFirst();
        listener.onSecond();

        String message = fails();
        assertThat(message, containsString("1. onFirst()"));
        assertThat(message, containsString("2. onSecond()"));
    }

    @Test
    public void failure_messages_highlight_the_problematic_calls() {
        listener.onFirst();
        listener.onParameter(1);
        spy.replay();

        listener.onFirst();
        listener.onParameter(2);

        String message = fails();
        assertThat(message, containsString("2. onParameter(1)\n" + SpyListener.ERROR_MARKER));
        assertThat(message, containsString("2. onParameter(2)\n" + SpyListener.ERROR_MARKER));
        assertThat(occurrencesOf(SpyListener.ERROR_MARKER, message), is(2));
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_call_replay_twice() {
        spy.replay();
        spy.replay();
    }

    @Test(expected = IllegalStateException.class)
    public void cannot_call_verify_without_first_calling_replay() {
        spy.verify();
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


    private interface DummyListener {

        void onFirst();

        void onSecond();

        void onParameter(int parameter);

        void onException(Throwable cause);

        void overloadedMethod(int one);

        void overloadedMethod(int one, int two);
    }
}
