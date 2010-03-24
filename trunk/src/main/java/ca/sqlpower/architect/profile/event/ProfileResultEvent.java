package ca.sqlpower.architect.profile.event;

import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.sqlobject.SQLObject;

/**
 * This event represents a change in the state of a profiling operation.
 * For instance, the process of calculating a profile's results has started,
 * completed normally, or has been canceled.
 */
public class ProfileResultEvent {
    
    /**
     * The profile result that this event pertains to.
     */
    private ProfileResult<SQLObject> source;
    
    /**
     * Takes a ProfileResult that is the source of this event.
     */
    public ProfileResultEvent(ProfileResult source) {
        this.source = source;
    }
    
    /**
     * Returns the ProfileResult that is the source of this event.
     */
    public ProfileResult getSource() {
        return source;
    }
}
