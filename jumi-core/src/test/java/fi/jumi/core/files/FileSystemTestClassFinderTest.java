// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.core.files.dummies.DummyTest;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import static org.mockito.Mockito.*;

public class FileSystemTestClassFinderTest {

    private static final String DUMMIES_PACKAGE = DummyTest.class.getPackage().getName();

    private TestClassFinderListener listener = mock(TestClassFinderListener.class);

    @Test
    public void finds_an_explicitly_named_class() throws IOException, URISyntaxException {
        findTestsMatchingPattern(DummyTest.class.getName());

        verify(listener).onTestClassFound(DummyTest.class);
    }

    @Test
    public void is_silent_if_no_test_classes_match_the_pattern() throws URISyntaxException {
        findTestsMatchingPattern(DUMMIES_PACKAGE + ".NoSuchTest");

        verifyZeroInteractions(listener);
    }

    private void findTestsMatchingPattern(String pattern) throws URISyntaxException {
        File classesDir = getClassesDirectory(getClass());
        FileSystemTestClassFinder finder = new FileSystemTestClassFinder(Arrays.asList(classesDir), pattern);
        finder.findTestClasses(listener);
    }

    private static File getClassesDirectory(Class<?> clazz) throws URISyntaxException {
        URL classFile = clazz.getResource(clazz.getSimpleName() + ".class");
        File file = new File(classFile.toURI());
        int directoryDepth = clazz.getName().split("\\.").length;
        for (int i = 0; i < directoryDepth; i++) {
            file = file.getParentFile();
        }
        return file;
    }
}
