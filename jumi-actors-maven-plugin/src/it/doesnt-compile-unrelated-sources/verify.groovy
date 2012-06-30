// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

import static org.junit.Assert.*

def tempDir = new File(basedir, "target/jumi-actors")

def compiledListenerClass = new File(tempDir, "example/ExampleListener.class")
assertTrue("should have compiled the listener class", compiledListenerClass.exists())

def compiledUnrelatedClass = new File(tempDir, "example/Unrelated.class")
assertFalse("should not have compiled other classes than the listener", compiledUnrelatedClass.exists())
