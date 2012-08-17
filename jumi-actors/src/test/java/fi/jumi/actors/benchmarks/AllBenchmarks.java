// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.benchmarks;

import com.google.caliper.Runner;

public class AllBenchmarks {

    public static void main(String[] args) {
        new Runner().run(WarmStartupBenchmark.class.getName());
        new Runner().run("-DringSize=1,10,100,1000,10000", "-DroundTrips=1000", RingBenchmark.class.getName());
        System.exit(0);
    }
}
