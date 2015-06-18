# Introduction #

This new feature of the core Architect API will provide a version history for every SQLObject. It is primarily intended that version history apply to the source database connections, but it may also prove useful in the "target" or "playpen" database.

The primary motivation for this feature is to provide a working "Refresh" feature in the Architect user interface while having a miniml impact on existing features that refer to SQLObjects (profiles and ETL lineage) and minimal compromise in terms of unexpected side effects of refreshing a particular source database.

# First Session #

Things to keep in mind:
  * tie-ins to profiling
  * tie-ins to ETL lineage (source column)
  * populated vs. unpopulated SQLObjects
  * what happens when:
    * source database is temporarily not available
    * refresh fails part way through
    * refresh succeeds, but the entire source schema/catalog/database is now empty or non-existent
  * should we auto-refresh before forward engineering into a source database?
  * should we auto-refresh on some time interval?
  * will it integrate with DBTree enough that the Data Mover will "just work?"
  * will it integrate with Compare DM enough that it will "just work?"
  * how to modify Compare DM GUI to allow differencing previous versions of databases?

Desirable outcomes:
  * a simple, working refresh feature (this is the main motivation)
  * notify users when and what in a source database has changed
  * provide a good GUI to browse/prune revision history of source/target databases

Implementation strategies to remember:
  * test first, then implement!
  * most existing tests must not change
  * use MVCC strategy (explained well in the PostgreSQL manual)
    * each object will have a startRevision (the revision number when this version of the object became valid) and an optional endRevision (the revision number when this version of the object was removed or superceeded by a newer version)
    * could use currentTimeMillis or just an incrementing long value in the session.
  * can keep version control completely optional (by default, endRevision == null and nothing will automatically set it.. this could be done by an add-on revision history API)
  * disallow modification to SQLObjects having endRevision != null (throw IllegalStateException)
  * could make a wrapper API to present historical versions of a SQLObject tree to revision-ignorant clients
    * could use a ThreadLocal revision object that SQLObjects would respect (we decided this is not a great idea)
    * could use a dynamic proxy that wraps the object being handed to the ignorant client, and autowraps itself around all requested child objects (this would carry down recursively as far as necessary to provide a consistent historical view)
      * one problem here is that the client won't know the objects are immutable
  * save historical revisions of objects in project file (current objects will not have an endRevision attribute)
  * load historical objects from project file
  * Proposed additions to the SQLObject class:
    * public Long beginRevision
    * public Long endRevision
    * public List

&lt;SQLObject&gt;

 getChildren(Long version) -- where null version means current
    * protected void checkCurrentVersion() -- throws IllegalStateException or some custom unchecked exception like NotCurrentVersionException if not current version; all mutator methods would call this first

Open Questions:
  * will this make the current undo/redo system redundant?
  * should undoing a refresh be possible, and what would the mechanism be internally?
  * should/could we use the current undo/redo system instead of MVCC?
  * should we model groups of objects at certain versions with a SQLObjectRevision class, so we can store change comments, revision properties, and show which objects changed at the same time, as a result of a single action (like Subversion)? (This sounds like it  really would be redundant with our undo/redo compound edit system)