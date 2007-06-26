package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;

public class ViewProfileAction extends AbstractArchitectAction {

    private TableProfileManager profileManager;

    public ViewProfileAction(ArchitectSwingSession session) {
        super(session, "View Profile...", "View Profiled Tables", "general/History");
    }

    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

    public TableProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(TableProfileManager profileManager) {
        this.profileManager = profileManager;
    }

}
