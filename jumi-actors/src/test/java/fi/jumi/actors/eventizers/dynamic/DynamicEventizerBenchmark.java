// Copyright © 2011-2018, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.actors.eventizers.dynamic;

import com.google.caliper.Benchmark;
import com.google.caliper.api.VmOptions;
import com.google.caliper.runner.CaliperMain;
import fi.jumi.actors.eventizers.Eventizer;

// XXX: workaround for https://stackoverflow.com/questions/29199509/caliper-error-cicompilercount-of-1-is-invalid-must-be-at-least-2
@VmOptions("-XX:-TieredCompilation")
public class DynamicEventizerBenchmark {

    private final DynamicEventizerProvider provider = new DynamicEventizerProvider();

    @Benchmark
    public int timeEventizerLookup(int reps) {
        int junk = 0;
        for (int i = 0; i < reps; i++) {
            Eventizer<?> eventizer = provider.getEventizerForType(ListenerWithLotsOfMethods.class);
            junk += eventizer.hashCode();
        }
        return junk;
    }


    public static void main(String[] args) {
        CaliperMain.main(DynamicEventizerBenchmark.class, new String[0]);
    }

    private interface ListenerWithLotsOfMethods {

        void event1();

        void event2();

        void event3();

        void event4();

        void event5();

        void event6();

        void event7();

        void event8();

        void event9();

        void event10();

        void event11();

        void event12();

        void event13();

        void event14();

        void event15();

        void event16();

        void event17();

        void event18();

        void event19();

        void event20();
    }
}
