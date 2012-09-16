// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers;

import javax.annotation.concurrent.NotThreadSafe;
import java.nio.charset.*;

@NotThreadSafe
public class EventToString {

    private final CharsetEncoder charsetEncoder;
    private final StringBuilder result = new StringBuilder();

    public EventToString(Charset charset) {
        charsetEncoder = charset.newEncoder();
    }

    public static String format(String className, String methodName, Object... args) {
        return new EventToString(Charset.defaultCharset())
                .formatMethodCall(className, methodName, args)
                .build();
    }

    public String build() {
        return result.toString();
    }

    public EventToString formatMethodCall(String className, String methodName, Object... args) {
        result.append(className);
        result.append('.');
        result.append(methodName);
        result.append('(');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            formatArg(args[i]);
        }
        result.append(')');
        return this;
    }

    private void formatArg(Object arg) {
        if (arg instanceof String) {
            result.append('"');
            escapeSpecialChars((String) arg);
            result.append('"');
        } else {
            result.append(arg);
        }
    }

    // package-private for testing
    EventToString escapeSpecialChars(String arg) {
        for (int i = 0; i < arg.length(); i++) {
            escapeSpecialChar(arg.charAt(i));
        }
        return this;
    }

    private void escapeSpecialChar(char ch) {
        switch (ch) {
            case '\b':
                result.append("\\b");
                return;
            case '\t':
                result.append("\\t");
                return;
            case '\n':
                result.append("\\n");
                return;
            case '\f':
                result.append("\\f");
                return;
            case '\r':
                result.append("\\r");
                return;
            case '\"':
                result.append("\\\"");
                return;
            case '\\':
                result.append("\\\\");
                return;
        }

        if (Character.isISOControl(ch) || isUnmappable(ch)) {
            String mask = "\\u0000";
            String hex = Integer.toHexString(ch);
            result.append(mask, 0, mask.length() - hex.length());
            result.append(hex);
        } else {
            result.append(ch);
        }
    }

    private boolean isUnmappable(char ch) {
        return !charsetEncoder.canEncode(ch);
    }
}
