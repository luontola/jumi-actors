// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.actors.*;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.*;

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

    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @readonly
     */
    protected MavenProject project;


    public void execute() throws MojoExecutionException {
        for (String eventInterface : eventInterfaces) {
            EventStubGenerator generator = new EventStubGenerator();
            try {
                // TODO: use classpath of the project
                generator.setListenerType(Class.forName(eventInterface));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            generator.setTargetPackage(targetPackage);

            // TODO: make these non-configurable
            generator.setEventInterface(Event.class.getName());
            generator.setFactoryInterface(ListenerFactory.class.getName());
            generator.setSenderInterface(MessageSender.class.getName());

            List<GeneratedClass> generated = new ArrayList<GeneratedClass>();
            generated.add(generator.getFactory());
            generated.add(generator.getFrontend());
            generated.add(generator.getBackend());
            generated.addAll(generator.getEvents());

            for (GeneratedClass c : generated) {
                try {
                    FileUtils.write(new File(outputDirectory, c.path), c.source); // TODO: encoding
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }
    }
}
