package ca.sqlpower.architect.profile;

import java.util.EventListener;

public interface ProfileChangeListener extends EventListener{
    
    public void profileRemoved(ProfileChangeEvent e);
    
    public void profileAdded(ProfileChangeEvent e);            

}
