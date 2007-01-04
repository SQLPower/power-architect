package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.BrowserUtil;

/**
 * Creates a JPanel that is the Welcome Screen, for adding to the main window.
 */
public class WelcomeScreen {
    
    private static final Logger logger = Logger.getLogger(WelcomeScreen.class);
    
    /**
     * The contents of the Welcome Screen text.
     */
    final static String welcomeHTMLstuff =
        "<html><head><style type=\"text/css\">body {margin-left: 100px; margin-right: 100px;}</style></head>" +
        "<body>" +
        "<h1 align=\"center\">Power*Architect " + ArchitectUtils.APP_VERSION + "</h1>" +
        "<br><br><br>" +
        "<p>&nbsp;&nbsp;Please visit our <a href=\"" + ArchitectFrame.FORUM_URL + "\">support forum</a>" +
        "   if you have any questions, comments, suggestions, or if you just need a friend." +
        "<br><br>" + 
        "<p>&nbsp;&nbsp;Check out the JDBC drivers section under <i>How to Use Power*Architect</i> in the " +
        "help for configuring JDBC drivers." +
        "<br>" +
        "<p>&nbsp;&nbsp;Need help finding the JDBC drivers? Visit our <a href=\"" + ArchitectFrame.DRIVERS_URL + "\">forum thread</a>";

    /**
     * Creates and returns the welcome panel.
     */
    public static JComponent getPanel() {
        Box b = Box.createVerticalBox();
        
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        iconPanel.add(new JLabel(ASUtils.createIcon("architect", "Large Architect Logo")));
        b.add(iconPanel);

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        final JEditorPane htmlComponent = new JEditorPane();
        htmlComponent.setEditorKit(htmlKit);
        htmlComponent.setText(welcomeHTMLstuff);
        htmlComponent.setEditable(false);
        htmlComponent.setBackground(null);

        /** Jump to the forum (in the user's configured browser)
         * when a link is clicked.
         */
        htmlComponent.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent evt) {
                if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URL url = evt.getURL();
                    try {
                        BrowserUtil.launch(url.toString());
                    } catch (IOException e1) {
                        throw new RuntimeException("Unexpected error in launch", e1);
                    }
                }
            }
        });
        b.add(htmlComponent);
        
        return b;
    }
}
