In addition to our JavaFormattingConventions, we have some guiding principles that we
try to follow when making design decisions (both code-wise and in the user interface).

This list is neither exhaustive nor static: Much of the practice of writing good, clean,
maintainable code comes from experience.

# Prefer Simplicity #


# Obvious API #


# Testable Implementations #


# Separation of Concerns #


# Beware of Singletons #

In Java, a singleton is a class for which only one instance is ever created over the
lifetime of the JVM.  This one instance is usually obtainable from anywhere via a
`public static` getter method on that class.

The problem we've had with singletons is that while they're really easy to create,
they are much more difficult to get rid of when, for one reason or another, it comes
time for there to be more than one instance of the class.

The classic example of a bad singleton in the Architect itself was the
`ArchitectFrame.getMainInstance()` method, which returned the one-and-only frame of the
Architect's Swing GUI.  When it came time to allow multiple sessions in the user
interface, were were faced with the huge task of removing hundreds of references to
this singleton `ArchitectFrame` instance, and finding some way to replace them with
something that works properly when multiple frames are visible at the same time.

A safer, more flexible, and more maintainable approach is the [Inversion of Control or Dependency Injection](http://www.google.ca/search?q=ioc%2Fdi)
pattern, which enjoys quite a bit of popularity these days.  There are a number of
tools for "wiring" Java objects together in an IoC-based system, most of which use
XML-based configuration files.  We prefer using the Java language directly for wiring
our objects together.

Other than long-term flexibility, another big advantage to IoC is that IoC-based APIs
are much easier to unit test than those which rely on singletons, since it's easy to
stub out every collaborator that a particular object wants to work with and test that
one object truly in isolation.


# Don't Duplicate Existing Flaws #

A lot of experience comes from learning by mistakes, and we have more than our fair
share of those reflected in the Architect's code base!  The idea of this guideline is
basically to ask around before creating a new module, API, subsystem, or what-have-you
based on the design of an existing one.  It has happened before that we've been saddled
with two copies of an old design we'd been planning to get rid of once we found the time.

If you ask around first, it's also an excellent opportunity to get feedback and
suggestions from your peers about better ways of approaching the problem.