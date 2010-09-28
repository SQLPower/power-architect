/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import java.awt.Point;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.Popup;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import ca.sqlpower.sqlobject.UserDefinedSQLType;
import ca.sqlpower.swingui.PopupListenerHandler;
import ca.sqlpower.swingui.SPSUtils;

/**
 * This {@link Action} creates a {@link Popup} for choosing domains and data
 * types in the {@link ColumnEditPanel}. 
 */
public class SQLTypeTreePopupAction extends AbstractAction {
    
    private final JPanel panel;
    private final JTree colType;
    private final JButton typeChooserButton;
    private PopupListenerHandler popupListenerHandler;

    /**
     * Creates a new {@link SQLTypeTreePopupAction} given the {@link JPanel} the
     * created {@link Popup} should appear on, the {@link JTree} the
     * {@link Popup} should embed, and the {@link JButton} that performs this
     * action.
     * 
     * @param panel
     *            The {@link JPanel} this {@link Popup} should appear on.
     * @param colType
     *            The {@link JTree} the {@link Popup} should embed.
     * @param typeChooserButton
     *            The {@link JButton} that should perform this action.
     */
    public SQLTypeTreePopupAction(JPanel panel, JTree colType, JButton typeChooserButton) {
        super();
        
        this.panel = panel;
        this.colType = colType;
        this.typeChooserButton = typeChooserButton;
    }

    /**
     * Creates a {@link Popup} if it is not visible, and connects the
     * appropriate listeners to check for when to hide the {@link Popup}.
     * Otherwise, it hides the {@link Popup} and disconnects the listeners.
     */
    public void actionPerformed(ActionEvent e) {
        if (popupListenerHandler != null && popupListenerHandler.isPopupVisible()) {
            popupListenerHandler.cleanup();
        } else {
            Point windowLocation = new Point(0, 0);
            SwingUtilities.convertPointToScreen(windowLocation, typeChooserButton);
            windowLocation.y += typeChooserButton.getHeight();
            
            // Popup the type choosing tree and attach the 
            // popup listener handler to the tree
            popupListenerHandler = 
                SPSUtils.popupComponent(panel, colType, windowLocation);
            popupListenerHandler.connect();
            colType.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    TreePath path = e.getNewLeadSelectionPath();
                    Object node = path.getLastPathComponent();
                    if (node instanceof UserDefinedSQLType) {
                        popupListenerHandler.cleanup();
                    }
                }
            });
        }
    }

}
