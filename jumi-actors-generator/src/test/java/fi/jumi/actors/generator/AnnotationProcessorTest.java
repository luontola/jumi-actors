// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.tools.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AnnotationProcessorTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void foo() {
        compile(new JavaSourceFromString("DummyInterface", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer\n" +
                "public interface DummyInterface {\n" +
                "}"
        ));
    }

    private void compile(JavaFileObject... compilationUnits) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        List<String> options = Arrays.asList("-d", tempDir.getRoot().getAbsolutePath());
        boolean success = compiler.getTask(null, null, diagnostics, options, null, Arrays.asList(compilationUnits)).call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic);
        }
        assertThat("compile succeeded", success, is(true));
    }
}
