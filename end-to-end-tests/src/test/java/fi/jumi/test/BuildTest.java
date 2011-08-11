// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.launcher.daemon.Daemon;
import org.intellij.lang.annotations.Language;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class BuildTest {

    private static final String POM_FILES = "META-INF/maven/fi.jumi/";
    private static final String BASE_PACKAGE = "fi/jumi/";

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[][]{
                {"jumi-actors",
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "actors/")
                },
                {"jumi-api",
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "api/")
                },
                {"jumi-core",
                        asList(
                                "fi.jumi:jumi-actors",
                                "fi.jumi:jumi-api"),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "core/")
                },
                {"jumi-daemon",
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "actors/",
                                BASE_PACKAGE + "api/",
                                BASE_PACKAGE + "core/",
                                BASE_PACKAGE + "daemon/")
                },
                {"jumi-launcher",
                        asList(
                                "fi.jumi:jumi-core"),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "launcher/")
                },
                {"thread-safety-agent",
                        asList(),
                        asList(
                                POM_FILES,
                                BASE_PACKAGE + "threadsafetyagent/")
                },
        });
    }

    private final String artifactId;
    private final List<String> expectedDependencies;
    private final List<String> expectedContents;
    private final File jarFile;
    private final File pomFile;

    public BuildTest(String artifactId, List<String> expectedDependencies, List<String> expectedContents) {
        this.artifactId = artifactId;
        this.expectedDependencies = expectedDependencies;
        this.expectedContents = expectedContents;
        this.jarFile = TestEnvironment.getProjectJar(artifactId);
        this.pomFile = TestEnvironment.getProjectPom(artifactId);
    }

    @Test
    public void jar_contains_only_allowed_files() throws Exception {
        assertJarContainsOnly(jarFile, expectedContents);
    }

    @Test
    public void pom_contains_only_allowed_dependencies() throws Exception {
        Document pom = parseXml(pomFile);
        List<String> dependencies = getRuntimeDependencies(pom);
        assertThat("dependencies of " + artifactId, dependencies, is(expectedDependencies));
    }

    @Test
    public void embedded_daemon_jar_contains_only_jumi_classes() throws IOException {
        // XXX: not a parameterized test; it's also quite slow so let's run it only once
        if (!artifactId.equals("jumi-daemon")) {
            return;
        }
        assertJarContainsOnly(Daemon.getDaemonJarAsStream(), asList(
                POM_FILES,
                BASE_PACKAGE
        ));
    }


    // helper methods

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
