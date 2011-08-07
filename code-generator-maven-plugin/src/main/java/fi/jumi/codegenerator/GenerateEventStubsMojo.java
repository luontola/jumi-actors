// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.compiler.*;
import org.codehaus.plexus.compiler.javac.JavacCompiler;

import java.io.*;
import java.net.*;
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
     * @parameter default-value="${project.build.directory}/jumi"
     */
    public File tempDirectory;

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
            EventStubGenerator generator = new EventStubGenerator(loadClass(eventInterface), targetPackage);

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

    private Class<?> loadClass(String eventInterface) {
        // TODO: use classpath of the project
        try {
            return Class.forName(eventInterface);
        } catch (ClassNotFoundException e) {
            // the class is part of this project and is still a source file
        }

        HashSet<String> includes = new HashSet<String>();
        includes.add(eventInterface.replace('.', '/') + ".java");

        CompilerConfiguration config = new CompilerConfiguration();
        config.addSourceLocation(sourceDirectory.getAbsolutePath());
        config.setIncludes(includes);
        config.setOutputLocation(tempDirectory.getAbsolutePath());
        config.setSourceVersion("1.6"); // TODO: use the current project's setting
        config.setTargetVersion("1.6"); // TODO: use the current project's setting
        // TODO: compile using project classpath

        try {
            JavacCompiler javac = new JavacCompiler();
            getLog().debug("Compiling event interface using: " + Arrays.toString(javac.createCommandLine(config)));
            List<CompilerError> messages = javac.compile(config);
            printCompilerMessages(messages);

        } catch (CompilerException e1) {
            throw new RuntimeException(e1);
        }

        try {
            URL[] urls = FileUtils.toURLs(new File[]{tempDirectory});
            URLClassLoader loader = new URLClassLoader(urls);
            return loader.loadClass(eventInterface);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void printCompilerMessages(List<CompilerError> messages) {
        for (CompilerError message : messages) {
            if (message.isError()) {
                getLog().error(message.toString());
            } else {
                getLog().warn(message.toString());
            }
        }
    }
}
