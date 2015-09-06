
Release Notes
=============

### Upcoming Changes

- Fixed generating imports for nested generics

### Jumi Actors 1.0.270 (2015-08-28)

- Jumi Actors is from this release onwards its own project with its own release cycle
- Maven groupId was changed from `fi.jumi` to `fi.jumi.actors`
- Upgraded to ASM 5, making the thread-safety-agent Java 8 compatible
- New annotation processor based code generator. Includes the following enhancements:
  - Getters for event classes
  - Cleaner generated code

### Jumi Actors 0.1.196 (2012-09-19)

- Improved logging of events with string parameters; special characters are now escaped
- Made configurable the language level for the Java compiler used by the Jumi Actors Maven plugin. Enables the use of event interfaces which depend on Java 7+ language features

### Jumi Actors 0.1.64 (2012-07-10)

- Javadocs for the public APIs of Jumi Actors
- Fixed a concurrency bug in `WorkerCounter`

### Jumi Actors 0.1.46 (2012-07-07)

- Initial release of Jumi Actors
