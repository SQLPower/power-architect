package ca.sqlpower.architect.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.architect.swingui.HelpPrintPanel;
import ca.sqlpower.architect.swingui.SwingUserSettings;

/**
 * Help Display for the Architect.
 * XXX Might be better to reimplement using Java Help API.
 */
/**
 * X is a Y.
 */
public class HelpAction extends AbstractAction  {

    private final String HOME_URL_STRING = "file:///P:/test_data/PowerArchitectUserGuide.html";
    private JEditorPane editorPane;

    private JPanel buttonPanel;
    private Action homeAction;
    private Action backAction;
    private Action forwardAction;
    private Action printHelpAction;
    private URLHistoryList history;
    
 
    public HelpAction() {
        
        super("Help",ASUtils.createJLFIcon( "general/Help",
                                            "Help", 
                                            ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
        putValue(AbstractAction.SHORT_DESCRIPTION, "Help");
        
        editorPane = new JEditorPane();       
        editorPane.setEditable(false);
        editorPane.setPreferredSize(new Dimension(600,600));

        editorPane.addHyperlinkListener(new Hyperactive());
        
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        homeAction = new AbstractAction("Home",
                                    ASUtils.createJLFIcon( "navigation/Home",
                                                "Help",
                                                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 16)) ) {
            public void actionPerformed(ActionEvent evt) {
                try {
                    editorPane.setPage(HOME_URL_STRING);
                    history.add(HOME_URL_STRING);
                } catch (IOException e) {
                    handleHelpError(e);
                }
                resetButtoms();
            }
        };
        homeAction.putValue(Action.NAME, "Home");
        JButton homeButton = new JButton(homeAction);
        buttonPanel.add(homeButton);
        
        backAction = new AbstractAction("Back",
                                    ASUtils.createJLFIcon( "navigation/Back",
                                                "Back",
                                                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 16)) ) {
            public void actionPerformed(ActionEvent evt) {
                URL url = history.getBack();
                if ( url == null )
                    return;
                try {
                    editorPane.setPage(url);
                } catch (IOException e) {
                    handleHelpError(e);
                }
                resetButtoms();
            }
        };
        backAction.putValue(Action.NAME, "Back");
        JButton backButton = new JButton(backAction);
        buttonPanel.add(backButton);
        
        forwardAction = new AbstractAction("Forward",
                ASUtils.createJLFIcon( "navigation/Forward",
                            "Forward",
                            ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 16)) ) {
            public void actionPerformed(ActionEvent evt) {
                URL url = history.getForward();
                if ( url == null )
                    return;
                try {
                    editorPane.setPage(url);
                } catch (IOException e) {
                    handleHelpError(e);
                }
                resetButtoms();
            }
        };

        forwardAction.putValue(Action.NAME, "Forward");
        JButton forwardButton = new JButton(forwardAction);
        buttonPanel.add(forwardButton);
        

        printHelpAction = new AbstractAction("Print",
                ASUtils.createJLFIcon( "general/Print",
                        "Print",
                        ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 16)) ) {
            public void actionPerformed(ActionEvent evt) {
                final HelpPrintPanel printPanel = new HelpPrintPanel(editorPane);
                
                final JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(
                        printPanel, 
                        ArchitectFrame.getMainInstance(),
                        "Print", "Print");
                
                printPanel.setZoom(0.3);
                d.setSize(800,600);
                d.pack();
                d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
                d.setVisible(true);
                
            }
        };
    
        printHelpAction.putValue(Action.NAME, "Print");
        JButton printButton = new JButton(printHelpAction);
        buttonPanel.add(printButton);            

        history = new URLHistoryList();
    }            

    void handleHelpError(Throwable t) {
        StringBuilder sb= new StringBuilder();
        sb.append("<html><h1>Help Failed</h1>");
        sb.append("<p>The help page failed to load because of the following error:");
        sb.append("<p><b>").append(t).append("</b></p>");
        //sb.append("<p>The following traceback will help our developers find the problem if necessary:</p>");
        //sb.append("<pre>").append("(not available yet"); // XXXt.printStacktrace() to a memorywriter...
        editorPane.setText(sb.toString());
    }
        
    public void actionPerformed(ActionEvent e) {

        final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
                                      "Power*Architect Help");

        JPanel cp = new JPanel(new BorderLayout(12,12));
        cp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        ArchitectPanelBuilder.makeJDialogCancellable(
                d, new CommonCloseAction(d));
        d.setContentPane(cp);
        cp.add(buttonPanel, BorderLayout.NORTH);
        
        JScrollPane scrollableTextArea = new JScrollPane(editorPane);
        cp.add(scrollableTextArea, BorderLayout.CENTER);

        if ( editorPane.getPage() == null ) {
            try {
                editorPane.setPage(HOME_URL_STRING);
                history.add(HOME_URL_STRING);
            } catch (IOException e1) {
                handleHelpError(e1);
            }
        }
        cp.validate();

        resetButtoms();
        d.pack();
        d.setLocationRelativeTo(ArchitectFrame.getMainInstance());
        d.setVisible(true);
    }

    public void resetButtoms() {
        if ( history.isBackwardable() )
            backAction.setEnabled(true);
        else
            backAction.setEnabled(false);
        
        if ( history.isForwardable() )
            forwardAction.setEnabled(true);
        else
            forwardAction.setEnabled(false);
    }

    class Hyperactive implements HyperlinkListener {
        
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                JEditorPane pane = (JEditorPane) e.getSource();
                if (e instanceof HTMLFrameHyperlinkEvent) {
                    HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e;
                    HTMLDocument doc = (HTMLDocument)pane.getDocument();
                    doc.processHTMLFrameHyperlinkEvent(evt);
                } else {
                    URL url = e.getURL();
                    try {
                        pane.setPage(url);
                        history.add(url);
                    } catch (Throwable t) {
                        handleHelpError(t);
                    }
                    resetButtoms();
                }
            }
        }
    }
    
    /**
     * A simple list of visited pages. 
     * XXX Reimplement based on java.util.Stack.
     */
    class URLHistoryList {

        List<URL> urlArray;
        int currentIndex;
        
        public URLHistoryList() {
            urlArray = new ArrayList<URL>();
            currentIndex = -1;
        }
        
        public boolean add(String urlStr) {
            URL url;
            try {
                url = new URL(urlStr);
                return add(url);
            } catch (MalformedURLException e) {
                handleHelpError(e);
            }
            return false;
        }      
        
        public boolean add(URL url) {
            boolean returnCode = true;

            if ( currentIndex >= 0 && currentIndex <= urlArray.size()-1 ) {
                URL currentURL = (URL)urlArray.get(currentIndex);
                if ( currentURL.equals(url))
                    return returnCode;
            }
            
            if ( currentIndex < 0 || currentIndex >= (urlArray.size()-1) ) {
                returnCode = urlArray.add(url);
                currentIndex = urlArray.size()-1;
            }
            else {
                urlArray.add(++currentIndex,url);
                while( urlArray.size() > currentIndex+1 ) {
                    urlArray.remove(currentIndex+1);
                }
            }
            
            return returnCode;
        }
        
        public URL getBack() {
            if ( currentIndex > 0 )
                return (URL)urlArray.get(--currentIndex);
            else if ( currentIndex == 0 )
                return (URL)urlArray.get(currentIndex=0);
            else
                return null;
        }
        
        public URL getForward() {
            if ( currentIndex >= 0 && currentIndex < urlArray.size()-1 )
                return (URL)urlArray.get(++currentIndex);
            else
                return null;
        }
        
        public String toString() {
            StringBuffer s = new StringBuffer("current=");
            s.append(currentIndex);
            s.append("  ");
            s.append(urlArray.toString());
            return s.toString();           
        }
        
        public boolean isBackwardable() {
            if ( currentIndex > 0 )
                return true;
            else
                return false;
        }
        
        public boolean isForwardable() {
            if ( currentIndex >= 0 && currentIndex < urlArray.size() - 1 )
                return true;
            else
                return false;
        }
   
    }
 
}
