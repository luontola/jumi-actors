// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

import static org.junit.Assert.*

def generatedDir = new File(basedir, "target/generated-sources/jumi-actors")
def generatedSourceFile = new File(generatedDir, "example/generated/ExternalResourceReleasableEventizer.java")
assertTrue("should have loaded the event interface from external library", generatedSourceFile.exists())
