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
package ca.sqlpower.architect.ddl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class GenericDDLWarningComponent implements DDLWarningComponent {

    private JButton quickFixButton = new JButton("Quick fix");

    private DDLWarning warning;

    public GenericDDLWarningComponent(final DDLWarning warning) {
        this.warning = warning;

        if (warning.isQuickFixable()) {
            quickFixButton.setToolTipText(warning.getQuickFixMessage());
            quickFixButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    boolean fixed = warning.quickFix();
                    warning.setFixed(fixed);
                    quickFixButton.setEnabled(false);
                }
            });
        } else {
            quickFixButton.setEnabled(false);
        }
    }
    public DDLWarning getWarning() {
        return warning;
    }

    public JButton getQuickFixButton() {
        return quickFixButton;
    }
}
