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

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.util.BrowserUtil;

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
        "<h1 align=\"center\">Power*Architect " + ArchitectVersion.APP_VERSION + "</h1>" +
        "<br><br><br>" +
        "<p>Please visit our <a href=\"" + ArchitectSwingSessionContext.FORUM_URL + "\">support forum</a>" +
        "   if you have any questions, comments, suggestions, or if you just need a friend." +
        "<br><br>" +
        "<p>Check out the JDBC drivers section under <i>How to Use Power*Architect</i> in the " +
        "help for configuring JDBC drivers." +
        "<br>" +
        "<p>Need help finding the JDBC drivers? Visit our <a href=\"" + ArchitectSwingSessionContext.DRIVERS_URL + "\">forum thread</a>.";

    /**
     * Creates and returns the welcome panel.
     */
    public static JComponent getPanel() {
        Box b = Box.createVerticalBox();

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));

        iconPanel.add(new JLabel(ASUtils.createIcon("sqlpower_transparent", "Large SQL*Power Logo")));
        iconPanel.add(new JLabel(ASUtils.createIcon("architect", "Large Architect Logo")));
        b.add(iconPanel);

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
        b.add(htmlComponent);

        return b;
    }
}
