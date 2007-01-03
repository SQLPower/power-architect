package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.BrowserUtil;

/**
 * Creates a JPanel that is the Welcome Screen, for adding to the main window.
 */
public class WelcomeScreen {
    /**
     * The contents of the Welcome Screen text.
     * XXX Better HTML is a distinct possibility here...
     */
    final static String welcomeHTMLstuff =
        "<html><h1 align=\"center\">Power*Architect %s</h1>" +
        "<br><br><br>" +
        "<p>&nbsp;&nbsp;Please visit our <a href=\"" + ArchitectFrame.FORUM_URL + "\">support forum</a>" +
        "   if you have any questions, comments, suggestions, or if you just need a friend." +
        "<p>&nbsp;&nbsp;Click anywhere on this panel to go to the Architect.";

    /**
     * (Create and) Return the Panel to display the welcome panel
     * @param r A Runnable to be run onClick(); <em>must be very short</em> as it
     * is run on the Event Dispatch Thread
     * @return
     */
    public static JComponent getPanel(final Runnable r) {
        JPanel p = new JPanel(new BorderLayout());

        p.add(new JLabel(ASUtils.createIcon("architect", "Large Architect Logo")), BorderLayout.NORTH);

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        JEditorPane htmlComponent = new JEditorPane();
        htmlComponent.setEditorKit(htmlKit);
        htmlComponent.setText(String.format(welcomeHTMLstuff, ArchitectUtils.APP_VERSION));
        htmlComponent.setEditable(false);

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
        p.add(htmlComponent, BorderLayout.CENTER);

        /** Get rid of the GlassPane when the user clicks */
        MouseListener closer = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                r.run();    // Run it on the event thread - it better be short!
            }
        };
        p.addMouseListener(closer);
        htmlComponent.addMouseListener(closer);
        return p;
    }
}
