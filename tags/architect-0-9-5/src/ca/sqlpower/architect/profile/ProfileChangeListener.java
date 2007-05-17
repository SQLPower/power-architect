package ca.sqlpower.architect.profile;

import java.util.EventListener;

public interface ProfileChangeListener extends EventListener{

    /** One or Many profiles were added */
    public void profilesAdded(ProfileChangeEvent e);

    /** One or Many profiles were removed */
    public void profilesRemoved(ProfileChangeEvent e);

    /** The list changed in some major way; listeners should re-fetch it */
    public void profileListChanged(ProfileChangeEvent event);

}
