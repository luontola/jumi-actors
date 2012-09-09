// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.daemon.EmbeddedDaemonJar;
import fi.jumi.test.PartiallyParameterized.NonParameterized;
import fi.jumi.test.util.*;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.w3c.dom.*;

import javax.annotation.concurrent.*;
import javax.xml.xpath.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import static fi.jumi.test.util.AsmMatchers.*;
import static fi.jumi.test.util.AsmUtils.annotatedWithOneOf;
import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(PartiallyParameterized.class)
public class BuildTest {

    private static final String POM_FILES = "META-INF/maven/fi.jumi/";
    private static final String BASE_PACKAGE = "fi/jumi/";

    private static final String[] DOES_NOT_NEED_JSR305_ANNOTATIONS = {
            // shaded classes
            "fi/jumi/core/INTERNAL/",
            "fi/jumi/daemon/INTERNAL/",
            "fi/jumi/launcher/INTERNAL/",
            // generated classes
            "fi/jumi/core/events/",
            // ignore, because the ThreadSafetyAgent anyways won't check itself
            "fi/jumi/threadsafetyagent/",
    };

    public static final String RELEASE_VERSION_PATTERN = "\\d+\\.\\d+\\.\\d+";
    public static final String SNAPSHOT_VERSION_PATTERN = "\\d+\\.\\d+-SNAPSHOT";
    public static final String VERSION_PATTERN = "(" + RELEASE_VERSION_PATTERN + "|" + SNAPSHOT_VERSION_PATTERN + ")";

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

    @Parameters
    @SuppressWarnings("unchecked")
    public static Collection<Object[]> data() {
        // TODO: upgrade shaded dependencies to Java 6/7 to benefit from their faster class loading
        return asList(new Object[][]{
                {"jumi-actors",
                        asList(Opcodes.V1_6),
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "actors/")
                },
                {"jumi-api",
                        asList(Opcodes.V1_7),
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "api/")
                },
                {"jumi-core",
                        asList(Opcodes.V1_5, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-actors",
                                "fi.jumi:jumi-api"),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "core/")
                },
                {"jumi-daemon",
                        asList(Opcodes.V1_5, Opcodes.V1_6, Opcodes.V1_7),
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "actors/",
                                BASE_PACKAGE + "api/",
                                BASE_PACKAGE + "core/",
                                BASE_PACKAGE + "daemon/")
                },
                {"jumi-launcher",
                        asList(Opcodes.V1_6, Opcodes.V1_7),
                        asList(
                                "fi.jumi:jumi-core"),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "launcher/")
                },
                {"thread-safety-agent",
                        asList(Opcodes.V1_5, Opcodes.V1_6),
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "threadsafetyagent/")
                },
        });
    }

    @Test
    public void pom_contains_only_allowed_dependencies() throws Exception {
        File pomFile = TestEnvironment.getProjectPom(artifactId);
        Document pom = XmlUtils.parseXml(pomFile);
        List<String> dependencies = getRuntimeDependencies(pom);
        assertThat("dependencies of " + artifactId, dependencies, is(expectedDependencies));
    }

    @Test
    public void jar_contains_only_allowed_files() throws Exception {
        File jarFile = TestEnvironment.getProjectJar(artifactId);
        assertJarContainsOnly(jarFile, expectedContents);
    }

    @Test
    public void jar_contains_a_pom_properties_with_the_maven_artifact_identifiers() throws IOException {
        Properties p = getPomProperties();
        assertThat("groupId", p.getProperty("groupId"), is("fi.jumi"));
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
    @NonParameterized
    public void embedded_daemon_jar_contains_only_jumi_classes() throws IOException {
        assertJarContainsOnly(new EmbeddedDaemonJar().getDaemonJarAsStream(), asList(
                POM_FILES,
                BASE_PACKAGE
        ));
    }

    @Test
    public void none_of_the_artifacts_may_have_dependencies_to_external_libraries() {
        for (String dependency : expectedDependencies) {
            assertThat("artifact " + artifactId, dependency, startsWith("fi.jumi:"));
        }
    }

    @Test
    public void none_of_the_artifacts_may_contain_classes_from_external_libraries_without_shading_them() {
        for (String content : expectedContents) {
            assertThat("artifact " + artifactId, content, Matchers.<String>
                    either(startsWith(BASE_PACKAGE)).or(startsWith(POM_FILES)));
        }
    }

    @Test
    public void all_classes_must_use_the_specified_bytecode_version() throws IOException {
        assumeReleaseBuild(); // XXX: compiling with IDEA compiles all modules with the same language level, so let's ignore this test when building in the IDE

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
        File jarFile = TestEnvironment.getProjectJar(artifactId);
        return readPropertiesFromJar(jarFile, POM_FILES + artifactId + "/" + filename);
    }

    private static Properties readPropertiesFromJar(File jarFile, String resource) throws IOException {
        URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
        InputStream in = cl.getResourceAsStream(resource);
        assertNotNull("resource not found: " + resource, in);
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } finally {
            in.close();
        }
    }

    private static void checkAllClasses(CompositeMatcher<ClassNode> matcher, File jarFile) {
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

    private static void assertJarContainsOnly(File jar, List<String> whitelist) throws IOException {
        try {
            assertJarContainsOnly(new FileInputStream(jar), whitelist);
        } catch (AssertionError e) {
            throw (AssertionError) new AssertionError(jar + " " + e.getMessage()).initCause(e);
        }
    }

    private static void assertJarContainsOnly(InputStream jarAsStream, List<String> whitelist) throws IOException {
        JarInputStream in = new JarInputStream(jarAsStream);
        JarEntry entry;
        while ((entry = in.getNextJarEntry()) != null) {
            assertIsWhitelisted(entry, whitelist);
        }
    }

    private static void assertIsWhitelisted(JarEntry entry, List<String> whitelist) {
        boolean allowed = false;
        for (String s : whitelist) {
            allowed |= entry.getName().startsWith(s);
            allowed |= s.startsWith(entry.getName());
        }
        assertTrue("contained a not allowed entry: " + entry, allowed);
    }

    private static List<String> getRuntimeDependencies(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) XmlUtils.xpath(
                "/project/dependencies/dependency[not(scope) or scope='compile' or scope='runtime']",
                doc, XPathConstants.NODESET);

        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node dependency = nodes.item(i);

            String groupId = XmlUtils.xpath("groupId", dependency);
            String artifactId = XmlUtils.xpath("artifactId", dependency);
            results.add(groupId + ":" + artifactId);
        }
        return results;
    }
}
