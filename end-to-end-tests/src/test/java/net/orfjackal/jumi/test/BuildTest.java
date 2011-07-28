// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.intellij.lang.annotations.Language;
import org.junit.*;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class BuildTest {

    private static final String POM_FILES = "META-INF/maven/fi.jumi/";
    private static final String BASE_PACKAGE = "net/orfjackal/jumi/";
    private static final Map<String, List<String>> DEPENDENCIES = new HashMap<String, List<String>>();

    static {
        DEPENDENCIES.put("jumi-api", Arrays.<String>asList());
        DEPENDENCIES.put("jumi-core", Arrays.asList("fi.jumi:jumi-api"));
        DEPENDENCIES.put("jumi-daemon", Arrays.<String>asList());
        DEPENDENCIES.put("jumi-launcher", Arrays.asList("fi.jumi:jumi-core"));
    }

    private File[] projectPoms;
    private File apiJar;
    private File coreJar;
    private File daemonJar;
    private File launcherJar;

    @Before
    public void init() throws IOException {
        projectPoms = TestEnvironment.getProjectPoms();
        File[] projectJars = TestEnvironment.getProjectJars();
        assertThat("project JARs", projectJars.length, is(4));
        apiJar = pick("jumi-api", projectJars);
        coreJar = pick("jumi-core", projectJars);
        daemonJar = pick("jumi-daemon", projectJars);
        launcherJar = pick("jumi-launcher", projectJars);
    }

    private static File pick(String namePrefix, File[] files) {
        for (File file : files) {
            if (file.getName().startsWith(namePrefix)) {
                return file;
            }
        }
        throw new IllegalArgumentException("file starting with " + namePrefix + " not found in " + Arrays.toString(files));
    }

    @Test
    public void embedded_daemon_jar_contains_only_jumi_classes() throws IOException {
        assertJarContainsOnly(Daemon.getDaemonJarAsStream(),
                POM_FILES,
                BASE_PACKAGE
        );
    }

    @Test
    public void contents_of_api_jar() throws IOException {
        assertJarContainsOnly(apiJar,
                POM_FILES,
                BASE_PACKAGE + "api/"
        );

    }

    @Test
    public void contents_of_core_jar() throws IOException {
        assertJarContainsOnly(coreJar,
                POM_FILES,
                BASE_PACKAGE + "core/"
        );
    }

    @Test
    public void contents_of_daemon_jar() throws IOException {
        assertJarContainsOnly(daemonJar,
                POM_FILES,
                BASE_PACKAGE + "api/",
                BASE_PACKAGE + "core/",
                BASE_PACKAGE + "daemon/"
        );
    }

    @Test
    public void contents_of_launcher_jar() throws IOException {
        assertJarContainsOnly(launcherJar,
                POM_FILES,
                BASE_PACKAGE + "launcher/"
        );
    }

    @Test
    public void project_artifact_poms_do_not_have_external_dependencies() throws Exception {
        assertThat("project POMs", projectPoms.length, is(4));
        for (File projectPom : projectPoms) {
            Document doc = parseXml(projectPom);

            String artifactId = getArtifactId(doc);
            List<String> dependencies = getRuntimeDependencies(doc);
            assertThat("dependencies of " + artifactId + " were " + dependencies, dependencies, is(DEPENDENCIES.get(artifactId)));
        }
    }

    // helper methods

    private static void assertJarContainsOnly(File jar, String... whitelist) throws IOException {
        try {
            assertJarContainsOnly(new FileInputStream(jar), whitelist);
        } catch (AssertionError e) {
            throw (AssertionError) new AssertionError(jar + " " + e.getMessage()).initCause(e);
        }
    }

    private static void assertJarContainsOnly(InputStream jarAsStream, String... whitelist) throws IOException {
        JarInputStream in = new JarInputStream(jarAsStream);
        JarEntry entry;
        while ((entry = in.getNextJarEntry()) != null) {
            assertIsWhitelisted(entry, whitelist);
        }
    }

    private static void assertIsWhitelisted(JarEntry entry, String... whitelist) {
        boolean allowed = false;
        for (String s : whitelist) {
            allowed |= entry.getName().startsWith(s);
            allowed |= s.startsWith(entry.getName());
        }
        assertTrue("contained a not allowed entry: " + entry, allowed);
    }

    private static String getArtifactId(Document doc) throws XPathExpressionException {
        return xpath("/project/artifactId", doc);
    }

    private static List<String> getRuntimeDependencies(Document doc) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath(
                "/project/dependencies/dependency[not(scope) or scope='compile' or scope='runtime']",
                doc, XPathConstants.NODESET);

        List<String> results = new ArrayList<String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node dependency = nodes.item(i);

            String groupId = xpath("groupId", dependency);
            String artifactId = xpath("artifactId", dependency);
            results.add(groupId + ":" + artifactId);
        }
        return results;
    }

    // xml parsing

    private static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        return domFactory.newDocumentBuilder().parse(file);
    }

    private static String xpath(@Language("XPath") String expression, Node node) throws XPathExpressionException {
        return (String) xpath(expression, node, XPathConstants.STRING);
    }

    private static Object xpath(@Language("XPath") String expression, Node item, QName returnType) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return xpath.evaluate(expression, item, returnType);
    }
}
