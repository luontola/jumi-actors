import static org.junit.Assert.*

def generatedSourceFile = new File(basedir, "target/generated-sources/jumi/example/generated/ExampleListenerFactory.java")
assertTrue("should have compiled the event interface with external libraries on the classpath", generatedSourceFile.exists())
