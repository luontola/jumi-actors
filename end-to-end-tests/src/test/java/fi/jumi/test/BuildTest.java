// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.luontola.buildtest.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.concurrent.*;
import java.io.*;
import java.util.*;

import static fi.luontola.buildtest.AsmMatchers.*;
import static fi.luontola.buildtest.AsmUtils.annotatedWithOneOf;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class BuildTest {

    private static final String MANIFEST = "META-INF/MANIFEST.MF";
    private static final String ANNOTATION_PROCESSOR = "META-INF/services/javax.annotation.processing.Processor";
    private static final String POM_FILES = "META-INF/maven/fi.jumi.actors/";
    private static final String BASE_PACKAGE = "fi/jumi/";

    private static final String[] DOES_NOT_NEED_JSR305_ANNOTATIONS = {
            // ignore, because only invoked by the compiler
            "fi/jumi/actors/generator/",
            // ignore, because the ThreadSafetyAgent anyways won't check itself
            "fi/jumi/threadsafetyagent/",
    };

    private final String artifactId;
    private final Integer[] expectedClassVersion;
    private final List<String> expectedDependencies;
    private final List<String> expectedContents;
    private final Deprecations expectedDeprecations;

    public BuildTest(String artifactId, List<Integer> expectedClassVersion, List<String> expectedDependencies, List<String> expectedContents, Deprecations expectedDeprecations) {
        this.artifactId = artifactId;
        this.expectedClassVersion = expectedClassVersion.toArray(new Integer[expectedClassVersion.size()]);
        this.expectedDependencies = expectedDependencies;
        this.expectedContents = expectedContents;
        this.expectedDeprecations = expectedDeprecations;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"jumi-actors",
                        asList(Opcodes.V1_6),
                        asList(),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "actors/"),
                        new Deprecations()
                },
                {"jumi-actors-generator",
                        asList(Opcodes.V1_6),
                        asList(
                                "fi.jumi.actors:jumi-actors"),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "actors/generator/",
                                ANNOTATION_PROCESSOR),
                        new Deprecations()
                },
                {"thread-safety-agent",
                        asList(Opcodes.V1_5, Opcodes.V1_6),
                        asList(),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "threadsafetyagent/"),
                        new Deprecations()
                },
        });
    }

    @Test
    public void pom_contains_only_allowed_dependencies() throws Exception {
        List<String> dependencies = MavenUtils.getRuntimeDependencies(getProjectPom());
        assertThat("dependencies of " + artifactId, dependencies, is(expectedDependencies));
    }

    @Test
    public void jar_contains_only_allowed_files() throws Exception {
        File jarFile = getProjectJar();
        JarUtils.assertContainsOnly(jarFile, expectedContents);
    }

    @Test
    public void jar_contains_a_pom_properties_with_the_maven_artifact_identifiers() throws IOException {
        Properties p = getPomProperties();
        assertThat("groupId", p.getProperty("groupId"), is("fi.jumi.actors"));
        assertThat("artifactId", p.getProperty("artifactId"), is(artifactId));
        assertThat("version", p.getProperty("version"), is(TestEnvironment.VERSION_NUMBERING));
    }

    @Test
    public void release_jar_contains_build_properties_with_the_Git_revision_ID() throws IOException {
        assumeReleaseBuild();

        Properties p = getBuildProperties();
        assertThat(p.getProperty("revision")).as("revision").matches("[0-9a-f]{40}");
    }

    @Test
    public void none_of_the_artifacts_may_have_dependencies_to_external_libraries() {
        for (String dependency : expectedDependencies) {
            assertThat("artifact " + artifactId, dependency, startsWith("fi.jumi.actors:"));
        }
    }

    @Test
    public void none_of_the_artifacts_may_contain_classes_from_external_libraries_without_shading_them() {
        for (String content : expectedContents) {
            assertThat("artifact " + artifactId, content, Matchers.<String>
                    either(startsWith(BASE_PACKAGE))
                    .or(startsWith(POM_FILES))
                    .or(startsWith(MANIFEST))
                    .or(startsWith(ANNOTATION_PROCESSOR)));
        }
    }

    @Test
    public void all_classes_must_use_the_specified_bytecode_version() throws IOException {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .assertThatIt(hasClassVersion(isOneOf(expectedClassVersion)));

        JarUtils.checkAllClasses(getProjectJar(), matcher);
    }

    @Test
    public void all_classes_must_be_annotated_with_JSR305_concurrent_annotations() throws Exception {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .excludeIf(is(anInterface()))
                .excludeIf(is(syntheticClass()))
                .excludeIf(nameStartsWithOneOf(DOES_NOT_NEED_JSR305_ANNOTATIONS))
                .assertThatIt(is(annotatedWithOneOf(Immutable.class, NotThreadSafe.class, ThreadSafe.class)));

        JarUtils.checkAllClasses(getProjectJar(), matcher);
    }

    @Test
    public void deprecated_methods_are_removed_after_the_transition_period() throws IOException {
        expectedDeprecations.verify(new ClassesInJarFile(getProjectJar()));
    }


    // helper methods

    private File getProjectPom() throws IOException {
        return TestEnvironment.ARTIFACTS.getProjectPom(artifactId);
    }

    private File getProjectJar() throws IOException {
        return TestEnvironment.ARTIFACTS.getProjectJar(artifactId);
    }

    private void assumeReleaseBuild() throws IOException {
        String version = getPomProperties().getProperty("version");
        assumeTrue(TestEnvironment.VERSION_NUMBERING.isRelease(version));
    }

    private Properties getBuildProperties() throws IOException {
        return getMavenArtifactProperties(getProjectJar(), "build.properties");
    }

    private Properties getPomProperties() throws IOException {
        return getMavenArtifactProperties(getProjectJar(), "pom.properties");
    }

    private Properties getMavenArtifactProperties(File jarFile, String filename) {
        return JarUtils.getProperties(jarFile, POM_FILES + artifactId + "/" + filename);
    }
}
