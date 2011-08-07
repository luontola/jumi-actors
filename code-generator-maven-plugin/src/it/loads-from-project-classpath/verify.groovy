private def errorMessage(String message) {
  def error = "ERROR: " + message
  System.out.println(error)
  return error
}

if (!new File(basedir, "target/generated-sources/jumi/example/generated/ExternalResourceReleasableFactory.java").exists()) {
  return errorMessage("should have loaded the event interface from external library")
}
