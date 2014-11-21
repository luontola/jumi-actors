// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

@GenerateEventizer
public interface DummyListener {

    void onSomething(String foo, String bar);

    void onOther();
}
