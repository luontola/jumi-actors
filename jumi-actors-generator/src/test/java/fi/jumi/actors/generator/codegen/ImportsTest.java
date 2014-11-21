// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ImportsTest {

    private final Imports imports = new Imports();

    @Test
    public void imports_classes() {
        imports.addImports(JavaType.of(ArrayList.class));

        assertThat(imports.toString().trim(), is("import java.util.ArrayList;"));
    }

    @Test
    public void imports_packages() {
        imports.addPackageImport("com.example");

        assertThat(imports.toString().trim(), is("import com.example.*;"));
    }

    @Test
    public void each_class_is_imported_only_once() {
        imports.addImports(JavaType.of(ArrayList.class));
        imports.addImports(JavaType.of(ArrayList.class));

        assertThat(imports.toString().trim(), is("import java.util.ArrayList;"));
    }

    @Test
    public void multiple_classes_from_the_same_package_are_imported_one_class_at_a_time() {
        imports.addImports(JavaType.of(ArrayList.class));
        imports.addImports(JavaType.of(LinkedList.class));

        assertThat(imports.toString().trim(), is("import java.util.ArrayList;\nimport java.util.LinkedList;"));
    }

    @Test
    public void does_not_import_java_lang_classes() {
        imports.addImports(JavaType.of(String.class));

        assertThat(imports.toString().trim(), is(""));
    }

    @Test
    public void does_not_import_primitive_types() {
        imports.addImports(JavaType.of(int.class));

        assertThat(imports.toString().trim(), is(""));
    }
}
