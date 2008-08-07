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
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.LineStraightenerLayout;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;

/**
 * Factory class that creates a PlayPen instance that's set up for use
 * in relational modeling (tables and relationships).
 */
public class RelationalPlayPenFactory {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RelationalPlayPenFactory.class);
    
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
                        JOptionPane.showMessageDialog(pp,
                                new JScrollPane(
                                    new JList(
                                        new java.util.Vector<Relationship>(
                                            pp.getRelationships()))));
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
                        JOptionPane.showMessageDialog(pp,
                                new JScrollPane(
                                    new JTextArea(componentList.toString())));
                    }
                });
                menu.add(mi);
                
                mi = new JMenuItem("Show Undo Vector"); //$NON-NLS-1$
                mi.setActionCommand(ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JOptionPane.showMessageDialog(pp,
                                new JScrollPane(
                                    new JTextArea(
                                        session.getUndoManager().printUndoVector())));
                    }
                });
                menu.add(mi);
            }
            
            return menu;
        }
        
    }
    
    /**
     * Asks the playpen to set up its own generic keyboard actions (select,
     * edit, cancel, keyboard navigation) and then adds the relational-specific
     * keyboard actions on top of those.  This is not done in the factory method
     * because there are some circular startup dependencies between PlayPen and
     * ArchitectFrame, so these actions have to be set up later.
     * 
     * @param pp The playpen to activate the keyboard actions on
     * @param session The session the playpen belongs to
     */
    static void setupKeyboardActions(final PlayPen pp, final ArchitectSwingSession session) {
        pp.setupKeyboardActions();
        final ArchitectFrame af = session.getArchitectFrame();

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateTableAction().getValue(Action.ACCELERATOR_KEY), "NEW TABLE"); //$NON-NLS-1$
        pp.getActionMap().put("NEW TABLE", af.getCreateTableAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getInsertColumnAction().getValue(Action.ACCELERATOR_KEY), "NEW COLUMN"); //$NON-NLS-1$
        pp.getActionMap().put("NEW COLUMN", af.getInsertColumnAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getInsertIndexAction().getValue(Action.ACCELERATOR_KEY), "NEW INDEX"); //$NON-NLS-1$
        pp.getActionMap().put("NEW INDEX", af.getInsertIndexAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY), "NEW IDENTIFYING RELATION"); //$NON-NLS-1$
        pp.getActionMap().put("NEW IDENTIFYING RELATION", af.getCreateIdentifyingRelationshipAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) af.getCreateNonIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY), "NEW NON IDENTIFYING RELATION"); //$NON-NLS-1$
        pp.getActionMap().put("NEW NON IDENTIFYING RELATION", af.getCreateNonIdentifyingRelationshipAction()); //$NON-NLS-1$

        final Object KEY_EDIT_SELECTION = "ca.sqlpower.architect.PlayPen.KEY_EDIT_SELECTION"; //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), KEY_EDIT_SELECTION);
        pp.getActionMap().put(KEY_EDIT_SELECTION, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ActionEvent ev = new ActionEvent(e.getSource(), e.getID(),
                                ArchitectSwingConstants.ACTION_COMMAND_SRC_PLAYPEN,
                                e.getWhen(), e.getModifiers());
                af.getEditSelectedAction().actionPerformed(ev);
            }
        });
        
    }

}
