// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

import java.util.Locale;

public class TargetPackageResolver {

    private final boolean createSubPackages;
    private final String targetPackage;

    public TargetPackageResolver(boolean createSubPackages, String targetPackage) {
        this.createSubPackages = createSubPackages;
        this.targetPackage = targetPackage;
    }

    public String getTargetPackage(String eventInterface) {
        // TODO: write a unit test for this
        if (createSubPackages) {
            String subpackage = getSimpleName(eventInterface).replaceAll("Listener$", "").toLowerCase(Locale.ENGLISH);
            return targetPackage + "." + subpackage;
        } else {
            return targetPackage;
        }
    }

    private static String getSimpleName(String name) {
        return name.substring(name.lastIndexOf('.') + 1);
    }
}
