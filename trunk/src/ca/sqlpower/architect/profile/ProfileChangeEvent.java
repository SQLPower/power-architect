package ca.sqlpower.architect.profile;

import java.util.EventObject;

import ca.sqlpower.architect.SQLObject;

public class ProfileChangeEvent extends EventObject {

    ProfileResult pr;
    
    public ProfileChangeEvent(Object source, ProfileResult pr) {
        super(source);
        this.pr = pr;        
    }

    public ProfileResult getProfileResult() {
        return pr;
    }

    @Override
    public String toString() {
        SQLObject profiledObject = pr.getProfiledObject();        
        String name = profiledObject !=null? profiledObject.getName()
                :"unknown profiled object";
        return String.format("ProfileChangeEvent(%s, %s)", name, pr);
    }
}
