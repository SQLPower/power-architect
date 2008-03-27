package ca.sqlpower.architect.profile.event;

/**
 * An interface that defines an event listener that reacts to changes
 * in the status of a profiling operation, such as when the profiling
 * operation has started, when it's finished, and if it's cancelled. 
 */
public interface ProfileResultListener {
    
    /**
     * Called when the profile operation has started 
     */
    public void profileStarted(ProfileResultEvent event);
    
    /**
     * Called when the profile operation is finished 
     */
    public void profileFinished(ProfileResultEvent event);
    
    /**
     * Called when the profile operation has been cancelled 
     */
    public void profileCancelled(ProfileResultEvent event);
}
