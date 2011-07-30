// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.codegenerator;

public class EventStubGenerator {

    private Class<?> listenerType;
    private String targetPackage;
    private String eventInterface;
    private String factoryInterface;
    private String senderInterface;

    public String getFactoryPath() {
        return fileForClass(listenerType.getSimpleName() + "Factory");
    }

    private String fileForClass(String className) {
        return targetPackage.replace('.', '/') + "/" + className + ".java";
    }

    // generated setters

    public void setListenerType(Class<?> listenerType) {
        this.listenerType = listenerType;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public void setEventInterface(String eventInterface) {
        this.eventInterface = eventInterface;
    }

    public void setFactoryInterface(String factoryInterface) {
        this.factoryInterface = factoryInterface;
    }

    public void setSenderInterface(String senderInterface) {
        this.senderInterface = senderInterface;
    }
}
