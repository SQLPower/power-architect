package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;

public class HelpAction extends AbstractAction {
    
    public HelpAction() {
    super("Help",      
        ASUtils.createJLFIcon( "general/Help",
        "Help", 
        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String helpHS = "jhelpset.hs";
            ClassLoader cl = getClass().getClassLoader();
            URL hsURL = HelpSet.findHelpSet(cl, helpHS);
            HelpSet hs = new HelpSet(null, hsURL);
            HelpBroker hb = hs.createHelpBroker();
            // Need to make it about 800x600 on 1024x768, or 640x480 on smaller screens
            CSH.DisplayHelpFromSource helpDisplay = new CSH.DisplayHelpFromSource(hb);
            helpDisplay.actionPerformed(e);

        } catch (Exception ev) {
            setEnabled(false);
            JOptionPane.showMessageDialog(ArchitectFrame.getMainInstance(), 
                    "Could not load Help File\n" + e + "\n" +
                    "Help function disabled",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }         
    }
}