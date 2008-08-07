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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ca.sqlpower.architect.layout.LineStraightenerLayout;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;

/**
 * Factory class that creates a PlayPen instance that's set up for use
 * in relational modeling (tables and relationships).
 */
public class RelationalPlayPenFactory {

    public static PlayPen createPlayPen(ArchitectSwingSession session) {
        PlayPen pp = new PlayPen(session);
        pp.setPopupFactory(new RelationalPopupFactory(pp, session));
        return pp;
    }
    
    private static class RelationalPopupFactory implements PopupMenuFactory {
        
        private final PlayPen pp;
        private final ArchitectSwingSession session;

        RelationalPopupFactory(PlayPen pp, ArchitectSwingSession session) {
            this.pp = pp;
            this.session = session;
        }

        public JPopupMenu createPopupMenu() {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem mi = new JMenuItem();
            mi.setAction(session.getArchitectFrame().getCreateTableAction());
            menu.add(mi);
            
            mi = new JMenuItem();
            Icon icon = new ImageIcon(
                    ClassLoader.getSystemResource("icons/famfamfam/wrench.png")); //$NON-NLS-1$
            AutoLayoutAction layoutAction =
                new AutoLayoutAction(
                        session,
                        Messages.getString("PlayPen.straightenLinesActionName"),  //$NON-NLS-1$
                        Messages.getString("PlayPen.straightenLinesActionDescription"), //$NON-NLS-1$
                        icon); 
            layoutAction.setLayout(new LineStraightenerLayout());
            mi.setAction(layoutAction);
            menu.add(mi);
            
            if (pp.isDebugEnabled()) {
                menu.addSeparator();
                mi = new JMenuItem("Show Relationships"); //$NON-NLS-1$
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JList(new java.util.Vector(pp.getRelationships()))));
                    }
                });
                menu.add(mi);
                
                mi = new JMenuItem("Show PlayPen Components"); //$NON-NLS-1$
                mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        StringBuffer componentList = new StringBuffer();
                        for (int i = 0; i < pp.getContentPane().getComponentCount(); i++) {
                            PlayPenComponent c = pp.getContentPane().getComponent(i);
                            componentList.append(c).append("["+c.getModel()+"]\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JTextArea(componentList.toString())));
                    }
                });
                menu.add(mi);
                
                mi = new JMenuItem("Show Undo Vector"); //$NON-NLS-1$
                mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JTextArea(session.getUndoManager().printUndoVector())));
                    }
                });
                menu.add(mi);
            }
            
            return menu;
        }
        
    }
}
