// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

import static org.junit.Assert.*

private def assertGeneratedFile(File suffixless) {
  assertTrue("did not generate file: " + suffixless, suffixless.exists())
}

def generatedDir = new File(basedir, "target/generated-sources/jumi")

assertGeneratedFile(new File(generatedDir, "example/generated/suffixless/SuffixlessFactory.java"))
assertGeneratedFile(new File(generatedDir, "example/generated/typical/TypicalListenerFactory.java"))
assertGeneratedFile(new File(generatedDir, "example/generated/manywords/ManyWordsListenerFactory.java"))
