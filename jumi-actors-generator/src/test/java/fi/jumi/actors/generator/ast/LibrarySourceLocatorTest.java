// Copyright © 2011-2015, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.ast;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LibrarySourceLocatorTest {

    private final LibrarySourceLocator locator = new LibrarySourceLocator();

    @Test
    public void finds_sources_from_JDK_src_zip() {
        String sources = locator.findSources(java.lang.String.class.getName());
        assertThat(sources, containsString("class String"));
    }

    @Test
    public void cannot_find_sources_of_unpublished_JDK_classes() {
        String sources = locator.findSources(sun.misc.Unsafe.class.getName());
        assertThat(sources, is(nullValue()));
    }

    @Test
    public void finds_sources_from_Maven_sources_jar() {
        String sources = locator.findSources(com.google.common.io.ByteStreams.class.getName());
        assertThat(sources, containsString("class ByteStreams"));
    }

    @Test
    public void finds_sources_from_classpath() {
        String sources = locator.findSources(org.mockito.asm.Opcodes.class.getName());
        assertThat(sources, containsString("interface Opcodes"));
    }
}
