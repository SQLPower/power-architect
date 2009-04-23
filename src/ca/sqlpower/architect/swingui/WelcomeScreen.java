/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Creates a JPanel that is the Welcome Screen, for adding to the main window.
 */
public class WelcomeScreen {

    private final ArchitectSwingSessionContext context;
    
    private final JLabel imageLabel;

    private JCheckBox showPrefsAgain;

    public WelcomeScreen(ArchitectSwingSessionContext context) {
        this.context = context;
        imageLabel = new JLabel(SPSUtils.createIcon("architect_welcome_heading", "Large Architect Logo")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * The contents of the Welcome Screen text.
     */
    final static String welcomeHTMLstuff =
        "<html><head><style type=\"text/css\">body {margin-left: 100px; margin-right: 100px;}</style></head>" + //$NON-NLS-1$
        "<body>" + //$NON-NLS-1$
        "<h1 align=\"center\">Power*Architect " + ArchitectVersion.APP_FULL_VERSION + "</h1>" + //$NON-NLS-1$ //$NON-NLS-2$
        "<br><br><br>" + //$NON-NLS-1$
        "<p>" + Messages.getString("WelcomeScreen.forumInfo", SPSUtils.FORUM_URL) + //$NON-NLS-1$ //$NON-NLS-2$
        "<br><br>" + //$NON-NLS-1$
        "<p>" + Messages.getString("WelcomeScreen.jdbcDriversHelp") + //$NON-NLS-1$ //$NON-NLS-2$
        "<br>" + //$NON-NLS-1$
        "<p>" + Messages.getString("WelcomeScreen.helpFindingJdbcDrivers", ArchitectSwingSessionContext.DRIVERS_URL) + //$NON-NLS-1$ //$NON-NLS-2$
        "<br><br><br>"; //$NON-NLS-1$

    public void showWelcomeDialog(Component dialogOwner) {
        final JDialog d = SPSUtils.makeOwnedDialog(dialogOwner, Messages.getString("WelcomeScreen.welcomeScreenTitle")); //$NON-NLS-1$
        d.setLayout(new BorderLayout(0, 12));
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref:grow, fill:pref, pref:grow", "pref, pref, pref"));
        CellConstraints cc = new CellConstraints();
        JPanel logoPanel = LogoLayout.generateLogoPanel();
        builder.add(logoPanel, cc.xyw(1, 1, 3));
        d.add(builder.getPanel(), BorderLayout.NORTH);
        logoPanel.getLayout().layoutContainer(logoPanel);
        
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
                        throw new RuntimeException(Messages.getString("WelcomeScreen.unexpectedError"), e1); //$NON-NLS-1$
                    }
                }
            }
        });
        d.add(htmlComponent, BorderLayout.CENTER);
        
        showPrefsAgain = new JCheckBox(Messages.getString("WelcomeScreen.showWelcomeInFuture")); //$NON-NLS-1$
        showPrefsAgain.setSelected(true);

        JButton closeButton = new JButton(Messages.getString("WelcomeScreen.closeButton")); //$NON-NLS-1$
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
    
    private static class LogoLayout implements LayoutManager {

        private int textStartY = 130;
        private int textStartX = 400;
        
        public static JPanel generateLogoPanel() {
            JPanel panel = new JPanel(new LogoLayout());
            
            JLabel bgLabel = new JLabel(new ImageIcon(WelcomeScreen.class.getClassLoader().getResource("icons/architect_header_bkgd.png")));
            JLabel welcomeLabel = new JLabel(new ImageIcon(WelcomeScreen.class.getClassLoader().getResource("icons/architect_header_welcome.png")));
            JLabel architectLabel = new JLabel(new ImageIcon(WelcomeScreen.class.getClassLoader().getResource("icons/architect_header_architect.png")));
            JLabel sqlpowerLabel = new JLabel(new ImageIcon(WelcomeScreen.class.getClassLoader().getResource("icons/architect_header_sqlpower.png")));
            JLabel versionLabel = new JLabel("" + ArchitectVersion.APP_VERSION);
            versionLabel.setForeground(new Color(0x999999));
            
            panel.add(welcomeLabel);
            panel.add(architectLabel);
            panel.add(sqlpowerLabel);
            panel.add(versionLabel);
            panel.add(bgLabel);
            return panel;
        }
        
        private LogoLayout() {
            //Do nothing for init.
        }
        
        public void layoutContainer(Container parent) {
            JLabel bgLabel = (JLabel) parent.getComponent(4);
            JLabel welcomeLabel = (JLabel) parent.getComponent(0);
            JLabel architectLabel = (JLabel) parent.getComponent(1);
            JLabel sqlpowerLabel = (JLabel) parent.getComponent(2);
            JLabel versionLabel = (JLabel) parent.getComponent(3);
            
            int headerStartX = (parent.getWidth() - 800) / 2;
            
            bgLabel.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            welcomeLabel.setBounds(headerStartX, 0, welcomeLabel.getPreferredSize().width, welcomeLabel.getPreferredSize().height);
            architectLabel.setBounds(welcomeLabel.getX() + welcomeLabel.getPreferredSize().width, 0, architectLabel.getPreferredSize().width, architectLabel.getPreferredSize().height);
            sqlpowerLabel.setBounds(headerStartX + 800 - sqlpowerLabel.getPreferredSize().width, 0, sqlpowerLabel.getPreferredSize().width, sqlpowerLabel.getPreferredSize().height);
            versionLabel.setBounds(architectLabel.getX() + textStartX, architectLabel.getY() + textStartY, versionLabel.getPreferredSize().width, versionLabel.getPreferredSize().height);
        }

        public Dimension minimumLayoutSize(Container parent) {
            JLabel welcomeLabel = (JLabel) parent.getComponent(0);
            JLabel architectLabel = (JLabel) parent.getComponent(1);
            JLabel sqlpowerLabel = (JLabel) parent.getComponent(2);
            
            return new Dimension(welcomeLabel.getWidth() + architectLabel.getWidth() + sqlpowerLabel.getWidth(),
                    Math.max(Math.max(welcomeLabel.getHeight(), architectLabel.getHeight()), sqlpowerLabel.getHeight()));
        }

        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }

        public void removeLayoutComponent(Component comp) {
            // no-op
        }
        
        public void addLayoutComponent(String name, Component comp) {
            // no-op
        }
    }
}
