# Introduction #

Until now, SQL Power Architect has been using the Swing threading model: one event dispatch thread is created by the GUI toolkit, and all access to the object model and the GUI must be performed on this thread. Additionally, all event notifications are processed on this thread. Most user input that causes a short-running operation is handled entirely on Swing's event dispatch thread, in the context of an event from the Swing toolkit (usually the actionPerformed() method of an Action.) For long-running operations, the Architect has a support class called SPSwingWorker (formerly called ArchitectSwingWorker) that encapsulates the thread wrangling.


# Motivation for change #

Now that we are adding CriticsFunctionality to SQL Power Architect, we would like to allow a secondary thread (or threads) read-only access to the entire model. We are concerned that processing critics on the "foreground" thread so the critics don't hog the foreground thread and make the GUI feel sluggish.


# Requirements #

Here are the requirements we have to satisfy (or at least balance) in SQL Power Architect's concurrency model:

  * Must be compatible with the Swing/AWT event dispatch thread model (all model state is accessible from the Event Dispatch Thread, and all events are processed on the Event Dispatch Thread.)
  * Must be compatible with the Servlet threading model (Servlet container provides many threads; all work for a request happens on the current thread, which is expected to be different for each request.)
  * Have to be able to map or convert the existing SPSwingWorker implementations to this new model
  * Within the GUI, secondary read-only threads:
    * should not block the user's progress
    * should not lag the GUI
    * should either see a consistent snapshot of the model they are reading, /or/ get canceled when they read inconsistent data. In the latter case, they should be able to restart their operation.
  * Code written against this threading model should be easy and straightforward in the normal case
  * Incorrect use of threads should "fail fast," and the fail-fast mechanism should be testable from within JUnit
  * Should be able to automatically update a status bar with a phrase and progress meter
  * Should manage the cursor so it shows as busy when the event dispatch thread is actually busy, and perhaps a "kinda busy" when there are background tasks executing


# Sources of Inspiration to Consider #

  * [Java Concurrency in Practice](http://www.amazon.ca/Java-Concurrency-Practice-Brian-Goetz/dp/0321349601/ref=sr_1_1?ie=UTF8&s=books&qid=1271715830&sr=8-1)
  * [Effective Java, 2nd Ed.](http://www.amazon.ca/Effective-Java-Joshua-Bloch/dp/0321356683/ref=sr_1_1?ie=UTF8&s=books&qid=1271715863&sr=1-1)


# Ideas #

  1. Let the session be an ExecutorService, and do all object model manipulations by passing Runnables or Callables into it
    * Advantages:
      * Compatible with both the Swing and Servlet threading models
      * Easy migration for existing SPSwingWorkers (could even allow an equivalent API such as BackgroundTask with setup() runBackground() and cleanup() and accept Runnable, Callable, or BackgroundJob tasks)
      * Advantage: no explicit use of threads or locking by client code
    * Disadvantages:
      * For simplicity and completeness, would have to migrate all SPSwingWorkers in all apps to this new model
  1. Use an AWT-style tree lock, but use read-write locks rather than exclusive locks
    * Advantages:
      * Allows fine-grained locking by clients who are only interested in a subset of the model
      * Compatible with Swing and Servlet threading models
    * Disadvantages:
      * client code must explicitly make the proper locks
      * deadlocks possible unless order of lock acquisition is carefully controlled
  1. Mutli-version Concurrency Control
    * Advantages:
      * Better than read-write locks, because writers won't block readers and readers won't block anything
      * Simple concurrency model for use by clients
    * Disadvantages:
      * Probably too complex to implement in the available time: how can we make long-lived model objects that support listeners versionable?
  1. Think about the Actors model?
    * This is probably too much to be considered an incremental change, and may even be impossible to reconcile with the Swing thread model