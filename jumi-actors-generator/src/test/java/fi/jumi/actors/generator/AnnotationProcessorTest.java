// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import fi.jumi.actors.generator.ast.JavaSourceFromString;
import org.hamcrest.*;
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

        assertThat(new File(outputDir, "com/example/DummyInterfaceEventizer.java"), exists());
        assertThat(new File(outputDir, "com/example/DummyInterfaceEventizer.class"), exists());
    }

    @Test
    public void generates_eventizers_to_another_target_package() throws IOException {
        compile(new JavaSourceFromString("AnotherTargetPackage", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer(targetPackage = \"com.example.events\")\n" +
                "public interface AnotherTargetPackage {\n" +
                "}"
        ));

        assertThat(new File(outputDir, "com/example/events/AnotherTargetPackageEventizer.java"), exists());
        assertThat(new File(outputDir, "com/example/events/AnotherTargetPackageEventizer.class"), exists());
    }

    @Test
    public void generates_eventizers_for_3rd_party_classes() throws IOException {
        compile(new JavaSourceFromString("RunnableStub", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer(useParentInterface = true)\n" +
                "public interface RunnableStub extends Runnable {\n" +
                "}"
        ));

        assertThat(new File(outputDir, "com/example/RunnableEventizer.java"), exists());
        assertThat(new File(outputDir, "com/example/RunnableEventizer.class"), exists());
        assertThat("shouldn't compile the parent to working directory", new File("Runnable.class"), not(exists()));
        assertThat("shouldn't compile the parent to working directory", new File("java/lang/Runnable.class"), not(exists()));
        assertThat("shouldn't compile the parent to output directory", new File(outputDir, "Runnable.class"), not(exists()));
        assertThat("shouldn't compile the parent to output directory", new File(outputDir, "java/lang/Runnable.class"), not(exists()));
    }

    @Test
    public void cannot_generate_eventizers_for_3rd_party_classes_if_sources_not_found() throws IOException {
        doesNotCompile(new JavaSourceFromString("NoSourcesForParent", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer(useParentInterface = true)\n" +
                "public interface NoSourcesForParent extends sun.misc.Compare {\n" +
                "}"
        ));

        assertThat(outputDir.listFiles(), is(emptyArray()));
    }

    @Test
    public void finds_sources_of_3rd_party_classes_which_are_member_classes() throws IOException {
        compile(new JavaSourceFromString("MemberInterfaceStub", "" +
                "package com.example;\n" +
                "import " + EnclosingClass.MemberInterface.class.getCanonicalName() + ";\n" +
                "@fi.jumi.actors.generator.GenerateEventizer(useParentInterface = true)\n" +
                "public interface MemberInterfaceStub extends MemberInterface {\n" +
                "}"
        ));

        assertThat(new File(outputDir, "com/example/MemberInterfaceEventizer.java"), exists());
        assertThat(new File(outputDir, "com/example/MemberInterfaceEventizer.class"), exists());
    }

    @Test
    public void useParentInterface_requires_exactly_one_parent_interface() throws IOException {
        doesNotCompile(new JavaSourceFromString("TooManyParentInterfaces", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer(useParentInterface = true)\n" +
                "public interface TooManyParentInterfaces extends Runnable, Cloneable {\n" +
                "}"
        ));

        assertThat(outputDir.listFiles(), is(emptyArray()));
    }

    @Test
    public void requires_interface() throws IOException {
        doesNotCompile(new JavaSourceFromString("NotInterface", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer\n" +
                "public class NotInterface {\n" +
                "}"
        ));

        assertThat(outputDir.listFiles(), is(emptyArray()));
    }

    @Test
    public void requires_methods_to_return_void() throws IOException {
        doesNotCompile(new JavaSourceFromString("NonVoidMethods", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer\n" +
                "public interface NonVoidMethods {\n" +
                "    java.lang.String bad();\n" +
                "}"
        ));

        assertThat(outputDir.listFiles(), is(emptyArray()));
    }

    @Test
    public void requires_methods_to_not_throw_exceptions() throws IOException {
        doesNotCompile(new JavaSourceFromString("ThrowingMethods", "" +
                "package com.example;\n" +
                "@fi.jumi.actors.generator.GenerateEventizer\n" +
                "public interface ThrowingMethods {\n" +
                "    void bad() throws java.lang.Exception;\n" +
                "}"
        ));

        assertThat(outputDir.listFiles(), is(emptyArray()));
    }


    private void compile(JavaFileObject... compilationUnits) throws IOException {
        assertThat("compiled?", tryCompile(compilationUnits), is(true));
    }

    private void doesNotCompile(JavaFileObject... compilationUnits) throws IOException {
        assertThat("compiled?", tryCompile(compilationUnits), is(false));
    }

    private boolean tryCompile(JavaFileObject... compilationUnits) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(outputDir));

        boolean success = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(compilationUnits)).call();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            System.err.println(diagnostic);
        }
        return success;
    }

    private static Matcher<File> exists() {
        return new TypeSafeMatcher<File>() {
            @Override
            protected boolean matchesSafely(File item) {
                return item.exists();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("file exists");
            }

            @Override
            protected void describeMismatchSafely(File item, Description mismatchDescription) {
                mismatchDescription.appendText("no such file ").appendValue(item);
            }
        };
    }
}
