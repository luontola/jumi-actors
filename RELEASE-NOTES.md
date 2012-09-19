
Release Notes
=============

### Jumi 0.1.196 (2012-09-19)

First release of the Jumi test runner. This release is targeted at the developers of testing frameworks and other tools, for them to start implementing support for Jumi. This release includes the following features:

- Launches a new JVM process for running the tests
- Runs tests in parallel
- Expressive test notification model
  - Nested tests
  - More than one failure per test
  - New tests can be discovered as test execution progresses; no need to know all tests up-front
- Reports what tests print to `System.out` and `System.err` more accurately than all other test runners
- Text user interface for showing the results, including exact events of when nested tests start and finish
- An example [SimpleUnit](https://github.com/orfjackal/jumi/tree/master/simpleunit) testing framework to show how to write a testing framework driver for Jumi

Some features critical for regular users are still missing and will be implemented in following releases. Some of the most obvious limitations are:

- No IDE and build tool integration yet; launching a suite requires configuring the suite's classpath manually, in a main method
- Runs only one test class per suite; cannot yet discover test classes automatically
- No JUnit test runner backward compatibility yet
- The text user interface shows all test runs; you cannot hide passing tests
- The number of worker threads is hard-coded to 4
- The working directory is hard-coded to be the same as what the launching process has
- Creates a `.jumi` directory in the working directory; eventually it will probably be placed in the user's home directory (it will contain settings and temporary files)
- The exception classes of all test failures must be also in the classpath of the launching process and must be serializable
- Does not report uncaught exceptions outside tests. To debug some categories of testing framework bugs, you must explicitly configure the launcher to print the daemon's raw standard output

Additionally this release includes the following changes to the Jumi Actors library:

- Improved logging of events with string parameters; special characters are now escaped
- Made configurable the language level for the Java compiler used by the Jumi Actors Maven plugin. Enables the use of event interfaces which depend on Java 7+ language features

### Jumi 0.1.64 (2012-07-10)

- Javadocs for the public APIs of Jumi Actors
- Fixed a concurrency bug in `WorkerCounter`

### Jumi 0.1.46 (2012-07-07)

- Initial release of Jumi Actors
