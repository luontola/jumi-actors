private def errorMessage(String message) {
  def error = "ERROR: " + message
  System.out.println(error)
  return error
}

if (!new File(basedir, "target/generated-sources/jumi/example/generated/ExampleListenerFactory.java").exists()) {
  return errorMessage("should have compiled the event interface with external libraries on the classpath")
}
