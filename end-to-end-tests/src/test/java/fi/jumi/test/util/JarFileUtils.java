// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.collect.AbstractIterator;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.Iterator;
import java.util.jar.*;

public class JarFileUtils {

    public static Iterable<ClassNode> classesIn(final File jarFile) {
        return new Iterable<ClassNode>() {
            @Override
            public Iterator<ClassNode> iterator() {
                try {
                    return new ClassNodeIterator(jarFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static class ClassNodeIterator extends AbstractIterator<ClassNode> {

        private final JarInputStream in;

        public ClassNodeIterator(File jarFile) throws IOException {
            in = new JarInputStream(new FileInputStream(jarFile));
        }

        @Override
        protected ClassNode computeNext() {
            try {
                JarEntry entry;
                while ((entry = in.getNextJarEntry()) != null) {
                    if (!isClassFile(entry)) {
                        continue;
                    }
                    return AsmUtils.readClass(in);
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return endOfData();
        }

        private static boolean isClassFile(JarEntry entry) {
            return !entry.isDirectory() && entry.getName().endsWith(".class");
        }
    }
}
