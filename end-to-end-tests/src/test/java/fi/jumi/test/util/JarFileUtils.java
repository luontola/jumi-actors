// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.collect.AbstractIterator;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;

public class JarFileUtils {

    public static Iterable<ClassNode> classesIn(final Path jarFile) {
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

    public static void walkZipFile(Path jarFile, SimpleFileVisitor<Path> visitor) throws IOException {
        URI uri = URI.create("jar:" + jarFile.toUri());
        HashMap<String, String> env = new HashMap<>();
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Files.walkFileTree(fs.getPath("/"), visitor);
        }
    }

    private static class ClassNodeIterator extends AbstractIterator<ClassNode> {

        private final JarInputStream in;

        public ClassNodeIterator(Path jarFile) throws IOException {
            in = new JarInputStream(Files.newInputStream(jarFile));
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
