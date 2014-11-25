// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.io.*;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AnnotationProcessorTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private File outputDir;

    @Before
    public void setup() {
        outputDir = tempDir.getRoot();
    }

    @Test
    public void generates_eventizers() throws IOException {
        compile(new JavaSourceFromString("DummyInterface", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer\n" +
                "public interface DummyInterface {\n" +
                "}"
        ));

        assertThat(new File(outputDir, "com/example/DummyInterfaceEventizer.java"), hasProperty("file", equalTo(true)));
        assertThat(new File(outputDir, "com/example/DummyInterfaceEventizer.class"), hasProperty("file", equalTo(true)));
    }

    private void compile(JavaFileObject... compilationUnits) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputDir));

        boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(compilationUnits)).call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic);
        }
        assertThat("compile succeeded", success, is(true));
    }
}
