// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import org.apache.maven.plugin.*;

import java.io.File;
import java.util.Arrays;

/**
 * @goal generate-event-stubs
 * @phase generate-sources
 */
public class GenerateEventStubsMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.build.sourceDirectory}"
     */
    public File sourceDirectory;

    /**
     * @parameter default-value="${project.build.directory}/generated-sources/jumi"
     */
    public File outputDirectory;

    /**
     * @parameter
     */
    public String targetPackage;

    /**
     * @parameter
     */
    public String[] eventInterfaces;

    public void execute() throws MojoExecutionException {
        getLog().info("Hello, world.");

        System.out.println("sourceDirectory = " + sourceDirectory);
        System.out.println("outputDirectory = " + outputDirectory);
        System.out.println("targetPackage = " + targetPackage);
        System.out.println("eventInterfaces = " + Arrays.toString(eventInterfaces));
    }
}
