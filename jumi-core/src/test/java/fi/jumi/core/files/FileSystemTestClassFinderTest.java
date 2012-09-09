// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;
import fi.jumi.core.files.dummies.DummyTest;
import org.junit.Test;

import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class FileSystemTestClassFinderTest {

    private static final String DUMMIES_PACKAGE = DummyTest.class.getPackage().getName();

    private final TestClassFinderListener listener = mock(TestClassFinderListener.class);

    @Test
    public void finds_an_explicitly_named_class() throws IOException {
        findTestsMatchingPattern(DummyTest.class.getName());

        verify(listener).onTestClassFound(DummyTest.class);
    }

    @Test
    public void is_silent_if_no_test_classes_match_the_pattern() {
        findTestsMatchingPattern(DUMMIES_PACKAGE + ".NoSuchTest");

        verifyZeroInteractions(listener);
    }

    private void findTestsMatchingPattern(String pattern) {
        Path classesDir = getClassesDirectory(getClass());
        FileSystemTestClassFinder finder = new FileSystemTestClassFinder(Arrays.asList(classesDir.toUri()), pattern);
        finder.findTestClasses(ActorRef.wrap(listener));
    }

    private static Path getClassesDirectory(Class<?> clazz) {
        try {
            URL classFile = clazz.getResource(clazz.getSimpleName() + ".class");
            Path path = Paths.get(classFile.toURI());

            int directoryDepth = clazz.getName().split("\\.").length;
            for (int i = 0; i < directoryDepth; i++) {
                path = path.getParent();
            }
            return path;

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
