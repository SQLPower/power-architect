/*
 * Copyright (c) 2026, SQL Power Group Inc.
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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * A first-run dialog that lets the user pick the UI language before the main
 * window opens. It replaces the language selector the legacy izpack installer
 * offered (jpackage installers do not support one). The choice is persisted to
 * the same setting the Preferences panel uses ({@link
 * ArchitectSwingUserSettings#DEFAULT_LOCALE}), so it only appears when no
 * language has been chosen yet.
 */
public class FirstRunLanguageDialog extends JDialog {

    private Locale selectedLocale;

    public FirstRunLanguageDialog(Component owner) {
        super(owner == null ? null : (java.awt.Frame) javax.swing.SwingUtilities.getAncestorOfClass(
                java.awt.Frame.class, owner));
        setTitle("Select Language");
        setModal(true);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel heading = new JLabel(
                "<html><center><b>Welcome to SQL Power Architect</b><br>"
                + "Select your language:</center></html>",
                SwingConstants.CENTER);
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
        content.add(heading, BorderLayout.NORTH);

        final List<Locale> locales = SupportedLanguages.getSupportedLocales();
        final JComboBox<Locale> combo = new JComboBox<Locale>(
                new DefaultComboBoxModel<Locale>(locales.toArray(new Locale[0])));
        combo.setRenderer(new ListCellRenderer<Locale>() {
            public Component getListCellRendererComponent(JList<? extends Locale> list,
                    Locale value, int index, boolean isSelected, boolean cellHasFocus) {
                return new JLabel(value.getDisplayLanguage(value)
                        + "  (" + value.getDisplayLanguage(Locale.ENGLISH) + ")");
            }
        });
        combo.setSelectedIndex(0);
        content.add(combo, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.add(new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                selectedLocale = (Locale) combo.getSelectedItem();
                setVisible(false);
            }
        }));
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog and returns the chosen locale (never null once OK is
     * pressed).
     */
    public Locale showDialog() {
        setVisible(true);
        return selectedLocale;
    }
}