package net.orfjackal.jumi.test;

import net.orfjackal.jumi.launcher.JumiLauncher;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

import java.io.*;

public class TempProjectBootstrapTest {

    // TODO: use a proper sandbox utility
    // TODO: configure sandbox dir location in a maven-filtered properties file (otherwise IDE and Maven use different dir)
    private final File sandboxDir = new File("target/sandbox").getAbsoluteFile();

    @Before
    public void createSandbox() throws IOException {
        assertTrue("Unable to create " + sandboxDir, sandboxDir.mkdirs());
    }

    @After
    public void deleteSandbox() throws IOException {
        FileUtils.forceDelete(sandboxDir);
    }

    @Test
    public void starts_daemon_in_a_new_process() throws Exception {
        StringWriter out = new StringWriter();

        JumiLauncher launcher = new JumiLauncher();
        launcher.setJumiHome(sandboxDir);
        launcher.setOutputListener(out);
        launcher.start();
        launcher.join();

        assertThat(out.toString().trim(), is("Hello world"));
    }
}
