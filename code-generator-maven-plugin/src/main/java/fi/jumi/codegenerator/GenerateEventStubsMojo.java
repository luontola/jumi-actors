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
 * @requiresDependencyResolution compile
 * @threadSafe
 */
public class GenerateEventStubsMojo extends AbstractMojo {

    /**
     * @parameter default-value="${project.build.sourceDirectory}"
     * @required
     */
    public File sourceDirectory;

    /**
     * @parameter default-value="${project.build.directory}/generated-sources/jumi"
     * @required
     */
    public File outputDirectory;

    /**
     * @parameter default-value="${project.compileClasspathElements}"
     * @required
     * @readonly
     */
    private String[] projectClasspath;


    /**
     * @parameter default-value="${project.build.directory}/jumi"
     * @required
     */
    public File tempDirectory;

    /**
     * @parameter
     * @required
     */
    public String targetPackage;

    /**
     * @parameter
     * @required
     */
    public String[] eventInterfaces;

    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}"
     * @required
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

    private Class<?> loadClass(String eventInterface) throws MojoExecutionException {
        try {
            URLClassLoader loader = new URLClassLoader(FileUtils.toURLs(toFiles(projectClasspath)));
            return loader.loadClass(eventInterface);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            // the class is part of this project and is still a source file
        }

        HashSet<String> includes = new HashSet<String>();
        includes.add(eventInterface.replace('.', '/') + ".java");

        CompilerConfiguration config = new CompilerConfiguration();
        if (sourceDirectory.isDirectory()) {
            config.addSourceLocation(sourceDirectory.getAbsolutePath());
        }
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

        } catch (CompilerException e) {
            throw new MojoExecutionException("Cannot compile event interface: " + eventInterface, e);
        }

        try {
            URL[] urls = FileUtils.toURLs(new File[]{tempDirectory});
            URLClassLoader loader = new URLClassLoader(urls);
            return loader.loadClass(eventInterface);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Cannot load event interface: " + eventInterface, e);
        }
    }

    private static File[] toFiles(String[] paths) {
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        return files;
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
