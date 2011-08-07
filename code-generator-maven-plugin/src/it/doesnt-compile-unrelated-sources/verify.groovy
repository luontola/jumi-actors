private def errorMessage(String message) {
  def error = "ERROR: " + message
  System.out.println(error)
  return error
}

if (!new File(basedir, "target/jumi/example/ExampleListener.class").exists()) {
  return errorMessage("should have compiled the listener class")
}

if (new File(basedir, "target/jumi/example/Unrelated.class").exists()) {
  return errorMessage("should not have compiled other classes than the listener")
}
