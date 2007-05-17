package ca.sqlpower.architect.profile;

import ca.sqlpower.architect.Monitorable;
import ca.sqlpower.architect.SQLObject;

/**
 * A ProfileResult is an interface for populating profile data about
 * database objects (for example, tables and columns), and for monitoring
 * the progress of the profiling operation, which can often take considerable
 * time to calculate.
 * 
 * @param T The type of SQLObject that this profile result calculates
 * and holds results for.
 */
public interface ProfileResult<T extends SQLObject> extends Monitorable {
    
    /**
     * Returns the SQLObject that is profiled by this ProfileResult.
     */
    public abstract T getProfiledObject();

    /**
     * Returns the date and time that this ProfileResult started
     * profiling the profiled object.
     */
    public abstract long getCreateStartTime();

    /**
     * Returns the time it took to create this ProfileResult in milliseconds.
     */
    public abstract long getTimeToCreate();

    /**
     * Returns the date and time that this ProfileResult finished profiling
     * the profiled object.
     */
    public abstract long getCreateEndTime();

    /**
     * Returns the Exception that occured during the profiling of the
     * profiled object. If this method returns null then the profiled
     * object is not done populating yet or it has sucessfully populated 
     * without throwing an Exception.
     */
    public abstract Exception getException();

    /**
     * Populates the ProfileResult of the object being profiled. You can
     * call this method as many times as you want, but it will have no effect
     * if isFinished() returns true and getException() returns null (these
     * are the conditions of a successful populate having happened in the past).
     */
    public void populate();

}