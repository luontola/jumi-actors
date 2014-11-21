// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.generator;

import fi.jumi.actors.generator.codegen.JavaType;

import static com.google.common.base.CaseFormat.*;

public class TargetPackageResolver {

    private final String targetPackage;

    public TargetPackageResolver(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getEventizerPackage() {
        return targetPackage;
    }

    public String getStubsPackage(JavaType listenerInterface) {
        return targetPackage + "." + UPPER_CAMEL.to(LOWER_CAMEL, listenerInterface.getSimpleName());
    }
}
