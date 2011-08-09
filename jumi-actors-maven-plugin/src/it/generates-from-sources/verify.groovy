// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import org.apache.commons.io.FileUtils

def compiledListenerClass = new File(basedir, "target/jumi/example/ExampleListener.class")
assertThat("should have compiled the listener class", compiledListenerClass.exists())

File[] workDir = [new File(basedir, "target/jumi")]
def loader = new URLClassLoader(FileUtils.toURLs(workDir))
def listenerClass = loader.loadClass("example.ExampleListener")
def someUtf8Text = listenerClass.getField("SOME_UTF8_TEXT").get(null)
assertThat("should have compiled the listener class using project encoding", "åäö", is(someUtf8Text))
