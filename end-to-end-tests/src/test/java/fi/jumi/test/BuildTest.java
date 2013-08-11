// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.test.util.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.w3c.dom.*;

import javax.annotation.concurrent.*;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static fi.jumi.test.util.AsmMatchers.*;
import static fi.jumi.test.util.AsmUtils.annotatedWithOneOf;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class BuildTest {

    private static final String MANIFEST = "META-INF/MANIFEST.MF";
    private static final String POM_FILES = "META-INF/maven/fi.jumi.actors/";
    private static final String BASE_PACKAGE = "fi/jumi/";

    private static final String[] DOES_NOT_NEED_JSR305_ANNOTATIONS = {
            // ignore, because the ThreadSafetyAgent anyways won't check itself
            "fi/jumi/threadsafetyagent/",
    };

    public static final String RELEASE_VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";
    public static final String SNAPSHOT_VERSION_PATTERN = "\\d+\\.\\d+-SNAPSHOT";

    private final String artifactId;
    private final Integer[] expectedClassVersion;
    private final List<String> expectedDependencies;
    private final List<String> expectedContents;

    public BuildTest(String artifactId, List<Integer> expectedClassVersion, List<String> expectedDependencies, List<String> expectedContents) {
        this.artifactId = artifactId;
        this.expectedClassVersion = expectedClassVersion.toArray(new Integer[expectedClassVersion.size()]);
        this.expectedDependencies = expectedDependencies;
        this.expectedContents = expectedContents;
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
                                BASE_PACKAGE + "actors/")
                },

                {"thread-safety-agent",
                        asList(Opcodes.V1_5, Opcodes.V1_6),
                        asList(),
                        asList(
                                MANIFEST,
                                POM_FILES,
                                BASE_PACKAGE + "threadsafetyagent/")
                },
        });
    }

    @Test
    public void pom_contains_only_allowed_dependencies() throws Exception {
        Path pomFile = TestEnvironment.getProjectPom(artifactId);
        Document pom = XmlUtils.parseXml(pomFile);
        List<String> dependencies = getRuntimeDependencies(pom);
        assertThat("dependencies of " + artifactId, dependencies, is(expectedDependencies));
    }

    @Test
    public void jar_contains_only_allowed_files() throws Exception {
        Path jarFile = TestEnvironment.getProjectJar(artifactId);
        assertJarContainsOnly(jarFile, expectedContents);
    }

    @Test
    public void jar_contains_a_pom_properties_with_the_maven_artifact_identifiers() throws IOException {
        Properties p = getPomProperties();
        assertThat("groupId", p.getProperty("groupId"), is("fi.jumi.actors"));
        assertThat("artifactId", p.getProperty("artifactId"), is(artifactId));

        String version = p.getProperty("version");
        assertTrue("should be either release or snapshot: " + version, isRelease(version) != isSnapshot(version));
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
                    .or(startsWith(MANIFEST)));
        }
    }

    @Test
    public void all_classes_must_use_the_specified_bytecode_version() throws IOException {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .assertThatIt(hasClassVersion(isOneOf(expectedClassVersion)));

        checkAllClasses(matcher, TestEnvironment.getProjectJar(artifactId));
    }

    @Test
    public void all_classes_must_be_annotated_with_JSR305_concurrent_annotations() throws Exception {
        CompositeMatcher<ClassNode> matcher = newClassNodeCompositeMatcher()
                .excludeIf(is(anInterface()))
                .excludeIf(is(syntheticClass()))
                .excludeIf(nameStartsWithOneOf(DOES_NOT_NEED_JSR305_ANNOTATIONS))
                .assertThatIt(is(annotatedWithOneOf(Immutable.class, NotThreadSafe.class, ThreadSafe.class)));

        checkAllClasses(matcher, TestEnvironment.getProjectJar(artifactId));
    }


    // helper methods

    private void assumeReleaseBuild() throws IOException {
        String version = getPomProperties().getProperty("version");
        assumeTrue(isRelease(version));
    }

    private Properties getBuildProperties() throws IOException {
        return getMavenArtifactProperties("build.properties");
    }

    private Properties getPomProperties() throws IOException {
        return getMavenArtifactProperties("pom.properties");
    }

    private static boolean isRelease(String version) {
        return version.matches(RELEASE_VERSION_PATTERN);
    }

    private static boolean isSnapshot(String version) {
        return version.matches(SNAPSHOT_VERSION_PATTERN);
    }

    private Properties getMavenArtifactProperties(String filename) throws IOException {
        Path jarFile = TestEnvironment.getProjectJar(artifactId);
        return readPropertiesFromJar(jarFile, POM_FILES + artifactId + "/" + filename);
    }

    private static Properties readPropertiesFromJar(Path jarFile, String resource) throws IOException {
        URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toUri().toURL()});
        try (InputStream in = cl.getResourceAsStream(resource)) {
            assertNotNull("resource not found: " + resource, in);

            Properties p = new Properties();
            p.load(in);
            return p;
        }
    }

    private static void checkAllClasses(CompositeMatcher<ClassNode> matcher, Path jarFile) {
        for (ClassNode classNode : JarFileUtils.classesIn(jarFile)) {
            matcher.check(classNode);
        }
        try {
            matcher.rethrowErrors();
        } catch (AssertionError e) {
            // XXX: get the parameterized runner improved so that it would be easier to see which of the parameters broke a test
            System.err.println("Found errors in " + jarFile);
            throw e;
        }
    }

    private static void assertJarContainsOnly(final Path jarFile, final List<String> whitelist) throws IOException {
        JarFileUtils.walkZipFile(jarFile, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                assertTrue(jarFile + " contained a not allowed entry: " + file,
                        isWhitelisted(file, whitelist));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean isWhitelisted(Path file, List<String> whitelist) {
        boolean allowed = false;
        for (String s : whitelist) {
            allowed |= file.startsWith("/" + s);
        }
        return allowed;
    }

    private static List<String> getRuntimeDependencies(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) XmlUtils.xpath(
                "/project/dependencies/dependency[not(scope) or scope='compile' or scope='runtime']",
                doc, XPathConstants.NODESET);

        List<String> results = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node dependency = nodes.item(i);

            String groupId = XmlUtils.xpath("groupId", dependency);
            String artifactId = XmlUtils.xpath("artifactId", dependency);
            results.add(groupId + ":" + artifactId);
        }
        return results;
    }
}
