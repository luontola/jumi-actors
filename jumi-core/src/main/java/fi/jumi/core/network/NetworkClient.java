// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.network;

import java.io.Closeable;

public interface NetworkClient extends Closeable {

    <In, Out> void connect(String hostname, int port, NetworkEndpoint<In, Out> endpoint);
}
