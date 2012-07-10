// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.process;

import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class SystemProcessLauncherTest {

    @Test
    public void passes_system_properties_to_the_process() {
        Properties systemProperties = new Properties();

        systemProperties.setProperty("propertyKey", "propertyValue");

        List<String> command = new SystemProcessLauncher().buildCommand(new File("dummy.jar"), new ArrayList<String>(), systemProperties, new String[0]);

        assertThat(command, hasItem("-DpropertyKey=propertyValue"));
    }
}
