package example;

public interface ExampleListener {

    /**
     * The plugin must use the project encoding instead of platform default encoding
     * when compiling this interface into its working directory.
     */
    String SOME_UTF8_TEXT = "åäö";

    void onSomething();
}
