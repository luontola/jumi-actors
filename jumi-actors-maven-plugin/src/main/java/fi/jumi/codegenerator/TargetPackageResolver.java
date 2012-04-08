// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import fi.jumi.codegenerator.java.JavaType;

public class TargetPackageResolver {

    private final String targetPackage;

    public TargetPackageResolver(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getFactoryPackage() {
        return targetPackage;
    }

    public String getStubsPackage(JavaType listenerInterface) {
        // TODO: should we support type-parameterized interfaces?
        return targetPackage + "." + listenerInterface.getSimpleName();
    }
}
