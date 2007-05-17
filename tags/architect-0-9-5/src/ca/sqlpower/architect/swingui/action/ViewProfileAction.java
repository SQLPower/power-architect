package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.profile.TableProfileManager;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class ViewProfileAction extends AbstractAction {


    private PlayPen pp;
    private TableProfileManager profileManager;

    public ViewProfileAction() {
        super("View Profile...", ASUtils.createJLFIcon( "general/History",
                        "View Profiles",
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));

        putValue(SHORT_DESCRIPTION, "View Profiled Tables");

    }

    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }

    public PlayPen getPlayPen() {
        return pp;
    }

    public void setPlayPen(PlayPen pp) {
        this.pp = pp;
    }

    public TableProfileManager getProfileManager() {
        return profileManager;
    }

    public void setProfileManager(TableProfileManager profileManager) {
        this.profileManager = profileManager;
    }

}
