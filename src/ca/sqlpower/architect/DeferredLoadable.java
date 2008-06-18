package ca.sqlpower.architect;

/**
 * The DeferredLoadable interface allows a heterogeneous collection of
 * objects to be managed by a single piece of code that knows when
 * they need to become fully loaded.  This is useful for classes that
 * have costly initialization code, such as database connections or
 * queries.
 */
public interface DeferredLoadable {

	/**
	 * A call to this method should cause implementing classes to
	 * perform all their costly startup procedures.  It is allowed to
	 * be called many times, so you should be sure to only preform the
	 * costly startup operation the first time.
	 */
	public void loadNow() throws ArchitectException;

	/**
	 * This method tells callers whether or not the loadNow() will do
	 * anything costly.
	 *
	 * @return true if the load operation has already been completed;
	 * false if the load operation will be costly.
	 */
	public boolean isLoaded() throws ArchitectException;
}
