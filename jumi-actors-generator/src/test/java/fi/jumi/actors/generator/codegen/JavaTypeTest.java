// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator.codegen;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings({"UnusedDeclaration"})
public class JavaTypeTest {

    private static final GenericType<String> SINGLE_TYPE_ARGUMENT = null;
    private static final GenericType<?> WILDCARD_TYPE_ARGUMENT = null;
    private static final GenericType2<String, Integer> MULTIPLE_TYPE_ARGUMENTS = null;
    private static final GenericType<List<String>> NESTED_TYPE_ARGUMENTS = null;

    @Test
    public void regular_type() {
        JavaType t = JavaType.of(NonGenericType.class);

        assertThat(t.getPackage(), is("fi.jumi.actors.generator.codegen"));
        assertThat(t.getRawName(), is("NonGenericType"));
        assertThat(t.getSimpleName(), is("NonGenericType"));
    }

    @Test
    public void generic_type() throws Exception {
        JavaType t = JavaType.of(genericTypeOfField("SINGLE_TYPE_ARGUMENT"));

        assertThat(t.getPackage(), is("fi.jumi.actors.generator.codegen"));
        assertThat(t.getRawName(), is("GenericType"));
        assertThat(t.getSimpleName(), is("GenericType<String>"));
    }

    @Test
    public void wildcard_type() throws Exception {
        JavaType t = JavaType.of(genericTypeOfField("WILDCARD_TYPE_ARGUMENT"));

        assertThat(t.getPackage(), is("fi.jumi.actors.generator.codegen"));
        assertThat(t.getRawName(), is("GenericType"));
        assertThat(t.getSimpleName(), is("GenericType<?>"));
    }

    @Test
    public void multiple_type_arguments() throws Exception {
        JavaType t = JavaType.of(genericTypeOfField("MULTIPLE_TYPE_ARGUMENTS"));

        assertThat(t.getRawName(), is("GenericType2"));
        assertThat(t.getSimpleName(), is("GenericType2<String, Integer>"));

    }

    @Test
    public void nested_type_arguments() throws Exception {
        JavaType t = JavaType.of(genericTypeOfField("NESTED_TYPE_ARGUMENTS"));

        assertThat(t.getRawName(), is("GenericType"));
        assertThat(t.getSimpleName(), is("GenericType<List<String>>"));
    }

    private static Type genericTypeOfField(String fieldName) throws Exception {
        return JavaTypeTest.class.getDeclaredField(fieldName).getGenericType();
    }
}

class NonGenericType {
}

class GenericType<T> {
}

class GenericType2<T, U> {
}
