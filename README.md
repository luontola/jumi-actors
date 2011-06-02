
Jumi
====

Common test runner for the JVM. Natively supports running tests in parallel, class loader caching, test order priorization and other measures to run tests faster. Contains the full stack for running tests (the responsibilities which used to be spread over build tools, IDEs and test runners) in order to overcome limitations of previously existing tools. Will overcome JUnit's test runner's limitations to better support all testing frameworks on the JVM.


Project Goals
-------------

- **Reliability** - A test runner is the most important tool of a software developer, second only to a compiler. Thus it should have [zero bugs](http://jamesshore.com/Agile-Book/no_bugs.html). In the unlikely case that you find a bug from Jumi, you will be richly rewarded.

- **Speed** - When tests are run after each change to a line of code, many times per minute, [speed matters](http://agileinaflash.blogspot.com/2009/02/first.html). Jumi will take any measures necessary (which don't compromise reliability) to give faster feedback.

- **Usability** - Many times high usability in the user interface affects also design decisions at the system's lowest implementation levels. By covering the whole stack of running tests, from UI to class loading, Jumi will be able to maximize its usability.

- **Compatibility** - Jumi attempts to become the next *de facto* test runner on the JVM. Thus it needs to integrate well with all tools and testing frameworks. We take backward compatibility seriously and will run any [consumer contract tests](http://martinfowler.com/articles/consumerDrivenContracts.html) from framework and tool developers. Any incompatible changes will be done carefully over a transition period.

- **Simplicity** - The system should be as simple as possible, but no simpler. Adding new features should be done with great criticism and unnecessary features should be removed. To achieve high reliability, [simplicity](http://www.jbrains.ca/permalink/the-four-elements-of-simple-design) in implementation is critical.

More details on the project's motivation can be read from its original announcements at the [junit](http://tech.groups.yahoo.com/group/junit/message/22933) and [scala-tools](http://scala-programming-language.1934581.n4.nabble.com/scala-tools-Common-Test-Runner-for-JVM-td2536290.html) mailing lists.


License
-------

Copyright © 2011 Esko Luontola <<http://www.orfjackal.net>>  
This software is released under the Apache License 2.0.  
The license text is at <http://www.apache.org/licenses/LICENSE-2.0>
