package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.daemon.Daemon;
import org.apache.commons.io.IOUtils;
import org.junit.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

public class BuildTest {

    private static final List<String> JAR_WHITELIST = Arrays.asList(
            "META-INF/maven/net.orfjackal.jumi/",
            "net/orfjackal/jumi/"
    );
    private static final List<String> DEPENDENCY_WHITELIST = Arrays.asList(
            "net.orfjackal.jumi"
    );
    private File[] projectJars;
    private File[] projectPoms;

    @Before
    public void readProperties() throws IOException {
        Properties testing = new Properties();
        InputStream in = BuildTest.class.getResourceAsStream("/testing.properties");
        try {
            testing.load(in);
        } finally {
            IOUtils.closeQuietly(in);
        }
        File projectArtifactsDir = new File(testing.getProperty("test.projectArtifactsDir"));
        projectJars = projectArtifactsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        projectPoms = projectArtifactsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".pom");
            }
        });
    }

    @Test
    public void embedded_daemon_jar_contains_only_jumi_classes() throws IOException {
        assertJarContainsOnly(JAR_WHITELIST, Daemon.getDaemonJarAsStream());
    }

    @Test
    public void project_artifact_jars_contain_only_jumi_classes() throws IOException {
        assertThat("project JARs", projectJars.length, is(4));
        for (File projectJar : projectJars) {
            assertJarContainsOnly(JAR_WHITELIST, projectJar);
        }
    }

    @Test
    public void project_artifact_poms_do_not_have_external_dependencies() throws Exception {
        assertThat("project POMs", projectPoms.length, is(4));
        for (File projectPom : projectPoms) {
            // TODO: check also artifact IDs, say explicitly what are the allowed dependencies per artifact?
            assertHasRuntimeDependenciesOnly(DEPENDENCY_WHITELIST, projectPom); // TODO: better name?
        }
    }

    // helper methods

    private static void assertJarContainsOnly(List<String> whitelist, File jar) throws IOException {
        try {
            assertJarContainsOnly(whitelist, new FileInputStream(jar));
        } catch (AssertionError e) {
            throw (AssertionError) new AssertionError(jar + " " + e.getMessage()).initCause(e);
        }
    }

    private static void assertJarContainsOnly(List<String> whitelist, InputStream jarAsStream) throws IOException {
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

    private void assertHasRuntimeDependenciesOnly(List<String> whitelist, File projectPom) throws Exception {
        for (Node dependency : getRuntimeDependencies(parseXml(projectPom))) {
            String groupId = getGroupId(dependency);
            assertTrue(projectPom + " had an unallowed dependency: " + groupId, whitelist.contains(groupId));
        }
    }

    private static List<Node> getRuntimeDependencies(Document doc) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return asList((NodeList) xpath.evaluate(
                "/project/dependencies/dependency[not(scope) or scope='compile' or scope='runtime']",
                doc, XPathConstants.NODESET));
    }

    private static String getGroupId(Node dependency) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (String) xpath.evaluate("groupId", dependency, XPathConstants.STRING);
    }

    // xml parsing

    private static List<Node> asList(NodeList nodes) {
        List<Node> result = new ArrayList<Node>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add(nodes.item(i));
        }
        return result;
    }

    private static Document parseXml(File file) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(false);
        return domFactory.newDocumentBuilder().parse(file);
    }
}
