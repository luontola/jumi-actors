// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

import java.util.regex.Pattern;

/**
 * Matches class names with a pattern syntax similar to <a href="http://ant.apache.org/manual/dirtasks.html">Ant</a>.
 * <pre>
 * foo.Bar - Single class foo.bar
 * foo.*   - All classes in package foo
 * foo.**  - All classes in package foo and its subpackages
 * </pre>
 */
public class ClassNameMatcher {

    private static final String PACKAGE_REGEX = "[^\\.]*";
    private static final String SUBPACKAGE_REGEX = ".*";

    private final Pattern pattern;

    public ClassNameMatcher(String pattern) {
        this.pattern = Pattern.compile(toRegex(pattern));
    }

    private static String toRegex(String pattern) {
        String regex = "";
        for (int i = 0; i < pattern.length(); i++) {
            if (subpackagePatternAt(i, pattern)) {
                regex += SUBPACKAGE_REGEX;
            } else if (packagePatternAt(i, pattern)) {
                regex += PACKAGE_REGEX;
            } else {
                regex += quoteCharAt(i, pattern);
            }
        }
        return regex;
    }

    private static boolean subpackagePatternAt(int i, String pattern) {
        return packagePatternAt(i, pattern)
                && packagePatternAt(i + 1, pattern); // PIT: false warning about "Replaced integer addition with subtraction"
    }

    private static boolean packagePatternAt(int i, String pattern) {
        return i < pattern.length()
                && pattern.charAt(i) == '*';
    }

    private static String quoteCharAt(int i, String pattern) {
        return Pattern.quote("" + pattern.charAt(i));
    }

    public boolean matches(String className) {
        return pattern.matcher(className).matches();
    }
}
