package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.sqlpower.architect.ArchitectUtils;

public class WelcomeScreen {
    
    public JComponent getPanel() {
        JPanel p = new JPanel(new BorderLayout());
        
        p.add(new JLabel(ASUtils.createIcon("architect", "Large Architect Logo")), BorderLayout.NORTH);
        
        StringBuilder htmlstuff = new StringBuilder();
        htmlstuff.append("<html><h1 align=\"center\">Power*Architect "+ArchitectUtils.APP_VERSION+"</h1>");
        htmlstuff.append("<br><br><br>");
        htmlstuff.append("<p>Please visit our <a href=\"http://www.sqlpower.ca/forum/\">support forum</a>");
        htmlstuff.append("   if you have any questions, comments, suggestions, or if you just need a friend.");
        
        p.add(new JLabel(htmlstuff.toString()), BorderLayout.CENTER);
        
        return p;
    }
}
