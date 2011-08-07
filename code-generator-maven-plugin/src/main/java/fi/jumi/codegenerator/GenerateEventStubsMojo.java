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
            return loadFromClasspath(eventInterface, toFiles(projectClasspath));
        } catch (ClassNotFoundException e) {
            // the class is part of this project and is still a source file
        }

        compileClass(eventInterface, tempDirectory);

        List<File> classpath = new ArrayList<File>();
        classpath.add(tempDirectory);
        classpath.addAll(Arrays.asList(toFiles(projectClasspath)));

        try {
            return loadFromClassPath(eventInterface, classpath);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("Could not load event interface: " + eventInterface, e);
        }
    }

    private void compileClass(String classToCompile, File targetDir) throws MojoExecutionException {
        HashSet<String> includes = new HashSet<String>();
        includes.add(classToCompile.replace('.', '/') + ".java");

        CompilerConfiguration config = new CompilerConfiguration();
        if (sourceDirectory.isDirectory()) {
            config.addSourceLocation(sourceDirectory.getAbsolutePath());
        }
        config.setIncludes(includes);
        config.setOutputLocation(targetDir.getAbsolutePath());
        config.setSourceVersion("1.6"); // TODO: use the current project's setting
        config.setTargetVersion("1.6"); // TODO: use the current project's setting
        config.setClasspathEntries(Arrays.asList(projectClasspath));

        try {
            JavacCompiler javac = new JavacCompiler();
            getLog().debug("Compiling event interface using: " + Arrays.toString(javac.createCommandLine(config)));
            List<CompilerError> messages = javac.compile(config);
            checkForCompilerErrors(messages, classToCompile);
        } catch (CompilerException e) {
            throw new MojoExecutionException("Cannot compile event interface: " + classToCompile, e);
        }
    }

    private static Class<?> loadFromClassPath(String className, List<File> classpath) throws ClassNotFoundException {
        return loadFromClasspath(className, classpath.toArray(new File[classpath.size()]));
    }

    private static Class<?> loadFromClasspath(String className, File[] classpath) throws ClassNotFoundException {
        try {
            URL[] urls = FileUtils.toURLs(classpath);
            URLClassLoader loader = new URLClassLoader(urls);
            return loader.loadClass(className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void checkForCompilerErrors(List<CompilerError> messages, String eventInterface) throws MojoExecutionException {
        if (!messages.isEmpty()) {
            String compilerMessages = "";
            boolean hadErrors = false;
            for (CompilerError message : messages) {
                hadErrors |= message.isError();
                compilerMessages += "\n" + message;
            }
            if (hadErrors) {
                throw new MojoExecutionException("There were compiler errors when compiling the event interface: "
                        + eventInterface + compilerMessages);
            }
        }
    }


    private static File[] toFiles(String[] paths) {
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        return files;
    }
}
