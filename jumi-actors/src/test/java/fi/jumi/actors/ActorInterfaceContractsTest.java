// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors;

import fi.jumi.actors.eventizers.Eventizers;
import org.junit.*;
import org.junit.rules.ExpectedException;

public class ActorInterfaceContractsTest {

    public static final Class<HasNonVoidMethods> INVALID_ACTOR_INTERFACE = HasNonVoidMethods.class;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void must_be_interfaces() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("actor interfaces must be interfaces, " +
                "but got class fi.jumi.actors.ActorInterfaceContractsTest$NotAnInterface");

        Eventizers.validateActorInterface(NotAnInterface.class);
    }

    @Test
    public void must_contain_only_void_methods() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("actor interface methods must be void, " +
                "but method onSomething of interface fi.jumi.actors.ActorInterfaceContractsTest$HasNonVoidMethods " +
                "had return type java.lang.String");

        Eventizers.validateActorInterface(HasNonVoidMethods.class);
    }

    @Test
    public void must_not_throw_exceptions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("actor interface methods may not throw exceptions, " +
                "but method onSomething of interface fi.jumi.actors.ActorInterfaceContractsTest$HasExceptionThrowingMethods " +
                "throws java.lang.Exception");

        Eventizers.validateActorInterface(HasExceptionThrowingMethods.class);
    }


    // guinea pigs

    public static abstract class NotAnInterface {
    }

    public interface HasNonVoidMethods {
        String onSomething();
    }

    public interface HasExceptionThrowingMethods {
        void onSomething() throws Exception;
    }
}
