// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EventToStringTest {

    @Test
    public void shows_the_method_and_all_its_arguments() {
        assertThat(EventToString.format("TheClass", "theMethod"), is("TheClass.theMethod()"));
        assertThat(EventToString.format("TheClass", "theMethod", 123), is("TheClass.theMethod(123)"));
        assertThat(EventToString.format("TheClass", "theMethod", 123, true), is("TheClass.theMethod(123, true)"));
    }

    @Test
    public void does_not_crash_to_null_arguments() {
        assertThat(EventToString.format("TheClass", "theMethod", (Object) null), is("TheClass.theMethod(null)"));
    }

    @Test
    public void quotes_string_arguments() {
        assertThat(EventToString.format("TheClass", "theMethod", "foo"), is("TheClass.theMethod(\"foo\")"));
    }

    @Test
    public void shows_strings_using_string_literal_escape_sequences() {
        assertThat(escapeSpecialChars("\b"), is("\\b"));
        assertThat(escapeSpecialChars("\t"), is("\\t"));
        assertThat(escapeSpecialChars("\n"), is("\\n"));
        assertThat(escapeSpecialChars("\f"), is("\\f"));
        assertThat(escapeSpecialChars("\r"), is("\\r"));
        assertThat(escapeSpecialChars("\""), is("\\\""));
        assertThat(escapeSpecialChars("\\"), is("\\\\"));
    }

    @Test
    public void escapes_ISO_control_characters() {
        assertThat(escapeSpecialChars("\0"), is("\\u0000"));
        assertThat(escapeSpecialChars("\1"), is("\\u0001"));
        assertThat(escapeSpecialChars("\u001f"), is("\\u001f"));
    }

    @Test
    public void escapes_unmappable_characters_for_the_current_charset() {
        assertThat(escapeSpecialChars("åäö", Charset.forName("ISO-8859-1")), is("åäö"));
        assertThat(escapeSpecialChars("åäö", Charset.forName("US-ASCII")), is("\\u00e5\\u00e4\\u00f6"));
    }


    // helpers

    private static String escapeSpecialChars(String arg) {
        return escapeSpecialChars(arg, Charset.forName("UTF-8"));
    }

    private static String escapeSpecialChars(String arg, Charset charset) {
        return new EventToString(charset).escapeSpecialChars(arg).build();
    }
}
