// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test;

import fi.jumi.core.RunId;

import java.util.*;
import java.util.regex.*;

public class TextUIParser {

    private static final String RUN_HEADER = "(^ > Run #(\\d+) in (\\S+)$)";
    private static final int RUN_ID = 2;
    private static final int CLASS_NAME = 3;

    private static final String TEST_START_OR_END = "^ > \\s*([+|-]) (.*)$";
    private static final String START_SYMBOL = "+";
    private static final int SYMBOL = 1;
    private static final int TEST_NAME = 2;

    private static final String ANY_LINES = "(?s:(.*?))";

    private static final String FOOTER = "(^Pass: (\\d+), Fail: (\\d+), Total: (\\d+)$)";
    private static final int PASSING_COUNT = 2;
    private static final int FAILING_COUNT = 3;
    private static final int TOTAL_COUNT = 4;

    private final String fullOutput;
    private final Map<RunId, RunParsed> runsById = new HashMap<RunId, RunParsed>();
    private int passingCount;
    private int failingCount;
    private int totalCount;

    public TextUIParser(String output) {
        this.fullOutput = output;

        Matcher m = Pattern.compile(ANY_LINES + FOOTER, Pattern.MULTILINE).matcher(output);
        m.find();
        parseRuns(m.group(1));
        parseFooter(m.group(2));
    }

    private void parseRuns(String allRunsOutput) {
        Matcher m = Pattern.compile(RUN_HEADER + ANY_LINES + "(?=" + RUN_HEADER + "|\\z)", Pattern.MULTILINE).matcher(allRunsOutput);
        while (m.find()) {
            String runOutput = m.group();
            RunId runId = new RunId(Integer.parseInt(m.group(RUN_ID)));
            String className = m.group(CLASS_NAME);
            this.runsById.put(runId, new RunParsed(runOutput, runId, className));
        }
    }

    private void parseFooter(String footerOutput) {
        Matcher m = Pattern.compile(FOOTER).matcher(footerOutput);
        m.find();
        passingCount = Integer.parseInt(m.group(PASSING_COUNT));
        failingCount = Integer.parseInt(m.group(FAILING_COUNT));
        totalCount = Integer.parseInt(m.group(TOTAL_COUNT));
    }

    public int getPassingCount() {
        return passingCount;
    }

    public int getFailingCount() {
        return failingCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getRunCount() {
        return runsById.size();
    }

    public Set<RunId> getRunIds() {
        return new HashSet<RunId>(runsById.keySet());
    }

    public String getRunOutput(RunId runId) {
        return getRun(runId).output;
    }

    public List<String> getTestStartAndEndEvents(RunId runId) {
        return getRun(runId).getTestStartAndEndEvents();
    }

    private RunParsed getRun(RunId runId) {
        RunParsed run = runsById.get(runId);
        if (run == null) {
            throw new IllegalArgumentException("run not found: " + runId);
        }
        return run;
    }

    @Override
    public String toString() {
        return fullOutput;
    }


    private static class RunParsed {
        public final String output;
        public final RunId runId;
        public final String className;

        public RunParsed(String output, RunId runId, String className) {
            this.output = output;
            this.runId = runId;
            this.className = className;
        }

        private List<String> getTestStartAndEndEvents() {
            ArrayList<String> events = new ArrayList<String>();
            Matcher m = Pattern.compile(TEST_START_OR_END, Pattern.MULTILINE).matcher(output);
            while (m.find()) {
                String prefix = m.group(SYMBOL);
                String testName = m.group(TEST_NAME);
                events.add(shortEventName(prefix, testName));
            }
            return events;
        }

        private static String shortEventName(String prefix, String testName) {
            if (prefix.equals(START_SYMBOL)) {
                return testName;
            } else {
                return "/";
            }
        }
    }
}
