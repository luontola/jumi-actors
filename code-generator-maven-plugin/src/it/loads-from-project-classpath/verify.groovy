import static org.junit.Assert.*

def generatedSourceFile = new File(basedir, "target/generated-sources/jumi/example/generated/ExternalResourceReleasableFactory.java")
assertTrue("should have loaded the event interface from external library", generatedSourceFile.exists())
