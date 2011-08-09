package example;

public interface ExampleListener {

    // We need an interface which references some external library,
    // which is not on this plugin's classpath

    void onSomething(org.jboss.netty.buffer.ChannelBuffer buffer);
}
