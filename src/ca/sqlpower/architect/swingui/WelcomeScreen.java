/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

/**
 * Creates a JPanel that is the Welcome Screen, for adding to the main window.
 */
public class WelcomeScreen {

    private final ArchitectSwingSessionContext context;
    
    private final JLabel imageLabel;

    private JCheckBox showPrefsAgain;

    public WelcomeScreen(ArchitectSwingSessionContext context) {
        this.context = context;
        imageLabel = new JLabel(SPSUtils.createIcon("architect_welcome_heading", "Large Architect Logo"));
    }
    
    /**
     * The contents of the Welcome Screen text.
     */
    final static String welcomeHTMLstuff =
        "<html><head><style type=\"text/css\">body {margin-left: 100px; margin-right: 100px;}</style></head>" +
        "<body>" +
        "<h1 align=\"center\">Power*Architect " + ArchitectVersion.APP_VERSION + "</h1>" +
        "<br><br><br>" +
        "<p>Please visit our <a href=\"" + SPSUtils.FORUM_URL + "\">support forum</a>" +
        "   if you have any questions, comments, suggestions, or if you just need a friend." +
        "<br><br>" +
        "<p>Check out the JDBC drivers section under <i>How to Use Power*Architect</i> in the " +
        "help for configuring JDBC drivers." +
        "<br>" +
        "<p>Need help finding the JDBC drivers? Visit our <a href=\"" + ArchitectSwingSessionContext.DRIVERS_URL + "\">forum thread</a>." +
        "<br><br><br>";

    public void showWelcomeDialog(Component dialogOwner) {
        final JDialog d = SPSUtils.makeOwnedDialog(dialogOwner, "Welcome to the Power*Architect");
        d.setLayout(new BorderLayout(0, 12));
        
        d.add(imageLabel, BorderLayout.NORTH);
        
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        final JEditorPane htmlComponent = new JEditorPane();
        htmlComponent.setEditorKit(htmlKit);
        htmlComponent.setText(welcomeHTMLstuff);
        htmlComponent.setEditable(false);
        htmlComponent.setBackground(null);
        
        /** Jump to the URL (in the user's configured browser)
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
        d.add(htmlComponent, BorderLayout.CENTER);
        
        showPrefsAgain = new JCheckBox("Show this Welcome Screen in future");
        showPrefsAgain.setSelected(true);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                context.getUserSettings().getSwingSettings().setBoolean(
                        ArchitectSwingUserSettings.SHOW_WELCOMESCREEN,
                        showPrefsAgain.isSelected());
                d.dispose();
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bottomPanel.add(showPrefsAgain, BorderLayout.WEST);
        bottomPanel.add(closeButton, BorderLayout.EAST);
        d.add(bottomPanel, BorderLayout.SOUTH);

        d.getRootPane().setDefaultButton(closeButton);
        d.pack();
        
        // The dialog is just a few pixels too wide after packing.
        d.setSize(d.getSize().width - 4, d.getSize().height);
        
        d.setLocationRelativeTo(dialogOwner);
        d.setVisible(true);
    }
}
