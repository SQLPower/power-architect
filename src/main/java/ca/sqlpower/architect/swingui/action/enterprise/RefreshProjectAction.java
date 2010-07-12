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

package ca.sqlpower.architect.swingui.action.enterprise;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionImpl;

public class RefreshProjectAction extends AbstractAction {

    private static final ImageIcon REFRESH_ICON = new ImageIcon(RefreshProjectAction.class.getResource("/icons/arrow_refresh16.png"));
    
    private final ArchitectFrame frame;

    public RefreshProjectAction(final ArchitectFrame frame) {
        super("Refresh", REFRESH_ICON);
        this.frame = frame;
        putValue(Action.SHORT_DESCRIPTION, "Refresh");
        
        setEnabled(frame.getCurrentSession().isEnterpriseSession());
        
        frame.addPropertyChangeListener(new PropertyChangeListener() {
           public void propertyChange(PropertyChangeEvent e) {
               if ("currentSession".equals(e.getPropertyName())) {
                   setEnabled(frame.getCurrentSession().isEnterpriseSession());
               }
           }
        });

    }
    
    public void actionPerformed(ActionEvent e) {
        ((ArchitectSwingSessionImpl) frame.getCurrentSession()).refresh();
    }
}
