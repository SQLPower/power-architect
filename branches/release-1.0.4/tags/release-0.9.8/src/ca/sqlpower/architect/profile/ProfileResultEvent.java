package ca.sqlpower.architect.profile;

import ca.sqlpower.architect.SQLObject;

/**
 * This event represents a change in the state of a profiling operation 
 */
public class ProfileResultEvent {
    private ProfileResult<SQLObject> source;
    
    /**
     * Takes a ProfileResult that is the source of this event 
     */
    public ProfileResultEvent(ProfileResult source) {
        this.source = source;
    }
    
    /**
     * Returns the ProfileResult that is the source of this event 
     */
    public ProfileResult getSource() {
        return source;
    }
}
