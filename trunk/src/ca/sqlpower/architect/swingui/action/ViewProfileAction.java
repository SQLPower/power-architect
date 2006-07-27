package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class ViewProfileAction extends AbstractAction {

    
    protected PlayPen pp;
    private ProfileManager profileManager;
    
    public ViewProfileAction(PlayPen pp, ProfileManager profileManager) {
        super("View Profile...", ASUtils.createJLFIcon( "general/History",
                        "View Profiles", 
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        
        putValue(SHORT_DESCRIPTION, "View Profiled Tables");
        this.pp = pp;
        this.profileManager = profileManager;
    }
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

}
