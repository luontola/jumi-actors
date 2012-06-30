// Copyright © 2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package sample;

import fi.jumi.api.RunVia;
import fi.jumi.test.simpleunit.SimpleUnit;

import java.util.concurrent.CountDownLatch;

@RunVia(SimpleUnit.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ParallelismTest {

    // The following two test methods must be run in parallel
    // or else they will block indefinitely, causing the end-to-end test to time out.

    private static CountDownLatch latch = new CountDownLatch(2);

    public void testOne() throws InterruptedException {
        latch.countDown();
        latch.await();
    }

    public void testTwo() throws InterruptedException {
        latch.countDown();
        latch.await();
    }
}
