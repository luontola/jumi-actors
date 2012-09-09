// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package example;

public interface ExampleListener {

    // We need an interface which depends on a class which requires Java 7 to be compiled

    void onSomething(JavaSevenUser x);
}
