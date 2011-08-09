import static org.junit.Assert.*

def compiledListenerClass = new File(basedir, "target/jumi/example/ExampleListener.class")
assertTrue("should have compiled the listener class", compiledListenerClass.exists())

def compiledUnrelatedClass = new File(basedir, "target/jumi/example/Unrelated.class")
assertFalse("should not have compiled other classes than the listener", compiledUnrelatedClass.exists())
