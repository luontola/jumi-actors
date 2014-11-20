// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.threadsafetyagent.util;

import org.apache.commons.io.*;

import java.io.*;
import java.lang.instrument.*;

public class TransformationTestClassLoader extends ClassLoader {

    private final ClassNameMatcher classesToInstrument;
    private final ClassFileTransformer transformer;
    private final File dirToWriteClasses;

    public TransformationTestClassLoader(String classesToInstrumentPattern, ClassFileTransformer transformer, File dirToWriteClasses) {
        super(TransformationTestClassLoader.class.getClassLoader());
        this.classesToInstrument = new ClassNameMatcher(classesToInstrumentPattern);
        this.transformer = transformer;
        this.dirToWriteClasses = dirToWriteClasses;
    }

    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            c = classesToInstrument.matches(name) ? findClass(name) : super.loadClass(name);
        }
        return c;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] original = readClassBytes(name);
            byte[] result = transformer.transform(this, name, null, null, original);
            if (result == null) {
                result = original;
            }
            if (dirToWriteClasses != null) {
                try {
                    FileUtils.writeByteArrayToFile(new File(dirToWriteClasses, name + ".class"), result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return defineClass(name, result, 0, result.length);

        } catch (IllegalClassFormatException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] readClassBytes(String name) throws ClassNotFoundException {
        InputStream in = getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
        if (in == null) {
            throw new ClassNotFoundException(name);
        }
        try {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
