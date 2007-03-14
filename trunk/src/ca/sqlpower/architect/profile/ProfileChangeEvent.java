package ca.sqlpower.architect.profile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

import ca.sqlpower.architect.SQLObject;

public class ProfileChangeEvent extends EventObject {

    List<ProfileResult> profileResultList = new ArrayList<ProfileResult>();
    
    public ProfileChangeEvent(Object source, ProfileResult pr) {
        super(source);
        this.profileResultList.add(pr);       
    }
    
    public ProfileChangeEvent(Object source, List<ProfileResult> prList) {
        super(source);
        this.profileResultList = new ArrayList<ProfileResult>(prList);
    }


    public List<ProfileResult> getProfileResult() {
        return Collections.unmodifiableList(profileResultList);
    }

    @Override
    public String toString() { 
        StringBuffer buf = new StringBuffer();
        buf.append("ProfileChangeEvent(");
        for (ProfileResult<SQLObject> pr: profileResultList) {
            String name;
            SQLObject profiledObject = pr.getProfiledObject();        
            name = profiledObject !=null? profiledObject.getName()
                :"unknown profiled object";
            
            buf.append(String.format(" [%s, %s]", name, pr));
        }
        buf.append(" )");
        return buf.toString();
    }
    
}
