package ca.sqlpower.architect.profile;

import java.util.EventListener;

public interface ProfileChangeListener extends EventListener{

    /** One profile was added */
    public void profileAdded(ProfileChangeEvent e);

    /** One profile was removed */
    public void profileRemoved(ProfileChangeEvent e);

    /** The list changed in some major way; listeners should re-fetch it */
    public void profileListChanged(ProfileChangeEvent event);

}
