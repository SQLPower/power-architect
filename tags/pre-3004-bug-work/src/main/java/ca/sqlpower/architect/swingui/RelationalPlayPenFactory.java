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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.LineStraightenerLayout;
import ca.sqlpower.architect.swingui.action.AutoLayoutAction;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Factory class that creates a PlayPen instance that's set up for use in
 * relational modeling (tables and relationships).
 */
public class RelationalPlayPenFactory {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RelationalPlayPenFactory.class);

    public static PlayPen createPlayPen(ArchitectSwingSession session, DBTree dbTree) {
        PlayPen pp = new PlayPen(session);
        pp.setPopupFactory(new RelationalPopupFactory(pp, session));
        SelectionSynchronizer synchronizer = new SelectionSynchronizer(dbTree, pp);
        pp.addSelectionListener(synchronizer);
        dbTree.addTreeSelectionListener(synchronizer);
        pp.addMouseListener(synchronizer);
        return pp;
    }

    private static class RelationalPopupFactory implements PopupMenuFactory {

        private final PlayPen pp;

        private final ArchitectSwingSession session;

        RelationalPopupFactory(PlayPen pp, ArchitectSwingSession session) {
            this.pp = pp;
            this.session = session;
        }

        /**
         * Creates a popup menu for the object. But at the moment, we are only
         * using this to create popup menu for relational playpen
         * <p>
         * Specific component in relational playpen currently creates their own popup menus
         * <p>
         * It is expected that <code>sourceComponent</code> is <code>null</code>
         */
        public JPopupMenu createPopupMenu(Object sourceComponent) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem mi = new JMenuItem();
            mi.setAction(session.getArchitectFrame().getCreateTableAction());
            menu.add(mi);

            mi = new JMenuItem();
            Icon icon = new ImageIcon(getClass().getResource("/icons/famfamfam/wrench.png")); //$NON-NLS-1$
            AutoLayoutAction layoutAction = new AutoLayoutAction(session, session.getPlayPen(), Messages
                    .getString("PlayPen.straightenLinesActionName"), //$NON-NLS-1$
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
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JList(new java.util.Vector<Relationship>(
                                pp.getContentPane().getChildren(Relationship.class)))));
                    }
                });
                menu.add(mi);

                mi = new JMenuItem("Show PlayPen Components"); //$NON-NLS-1$
                mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        StringBuffer componentList = new StringBuffer();
                        for (PlayPenComponent c : pp.getContentPane().getChildren()) {                            
                            componentList.append(c).append("[" + c.getModel() + "]\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JTextArea(componentList.toString())));
                    }
                });
                menu.add(mi);

                mi = new JMenuItem("Show Undo Vector"); //$NON-NLS-1$
                mi.setActionCommand(PlayPen.ACTION_COMMAND_SRC_PLAYPEN);
                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JOptionPane.showMessageDialog(pp, new JScrollPane(new JTextArea(session.getUndoManager()
                                .printUndoVector())));
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
     * keyboard actions on top of those. This is not done in the factory method
     * because there are some circular startup dependencies between PlayPen and
     * ArchitectFrame, so these actions have to be set up later.
     * 
     * @param pp
     *            The playpen to activate the keyboard actions on
     * @param session
     *            The session the playpen belongs to
     */
    static void setupKeyboardActions(final PlayPen pp, final ArchitectSwingSession session) {
        pp.setupKeyboardActions();
        final ArchitectFrame af = session.getArchitectFrame();

        String KEY_DELETE_SELECTED = "ca.sqlpower.architect.swingui.PlayPen.KEY_DELETE_SELECTED"; //$NON-NLS-1$

        InputMap inputMap = pp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KEY_DELETE_SELECTED);
        pp.getActionMap().put(KEY_DELETE_SELECTED, af.getDeleteSelectedAction());
        if (af.getDeleteSelectedAction() == null)
            logger.warn("af.deleteSelectedAction is null!"); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getZoomToFitAction().getValue(Action.ACCELERATOR_KEY), "ZOOM TO FIT"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM TO FIT", af.getZoomToFitAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getZoomInAction().getValue(Action.ACCELERATOR_KEY), "ZOOM IN"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM IN", af.getZoomInAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getZoomOutAction().getValue(Action.ACCELERATOR_KEY), "ZOOM OUT"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM OUT", af.getZoomOutAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getZoomResetAction().getValue(Action.ACCELERATOR_KEY), "ZOOM RESET"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM RESET", af.getZoomResetAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getCreateTableAction().getValue(Action.ACCELERATOR_KEY), "NEW TABLE"); //$NON-NLS-1$
        pp.getActionMap().put("NEW TABLE", af.getCreateTableAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getInsertColumnAction().getValue(Action.ACCELERATOR_KEY), "NEW COLUMN"); //$NON-NLS-1$
        pp.getActionMap().put("NEW COLUMN", af.getInsertColumnAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getInsertIndexAction().getValue(Action.ACCELERATOR_KEY), "NEW INDEX"); //$NON-NLS-1$
        pp.getActionMap().put("NEW INDEX", af.getInsertIndexAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getCreateIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY),
                "NEW IDENTIFYING RELATION"); //$NON-NLS-1$
        pp.getActionMap().put("NEW IDENTIFYING RELATION", af.getCreateIdentifyingRelationshipAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                (KeyStroke) af.getCreateNonIdentifyingRelationshipAction().getValue(Action.ACCELERATOR_KEY),
                "NEW NON IDENTIFYING RELATION"); //$NON-NLS-1$
        pp.getActionMap().put("NEW NON IDENTIFYING RELATION", af.getCreateNonIdentifyingRelationshipAction()); //$NON-NLS-1$

        final Object KEY_EDIT_SELECTION = "ca.sqlpower.architect.PlayPen.KEY_EDIT_SELECTION"; //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                KEY_EDIT_SELECTION);
        pp.getActionMap().put(KEY_EDIT_SELECTION, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ActionEvent ev = new ActionEvent(e.getSource(), e.getID(),
                        PlayPen.ACTION_COMMAND_SRC_PLAYPEN, e.getWhen(), e.getModifiers());
                af.getEditSelectedAction().actionPerformed(ev);
            }
        });

    }

    static class SelectionSynchronizer implements SelectionListener, 
    ItemSelectionListener<SQLTable, SQLColumn>, TreeSelectionListener, MouseListener {

        private int eventDepth = 0;

        private final DBTree tree;

        private final PlayPen pp;

        public SelectionSynchronizer(DBTree tree, PlayPen pp) {
            this.tree = tree;
            this.pp = pp;
        }

        /**
         * Synchronizes the dbtTree selection with the playpen selections
         * 
         * @throws SQLObjectException
         * 
         */
        public void updateDBTree() {
            if (eventDepth != 1) {
                return;
            }
            tree.clearSelection();

            List<TreePath> selectionPaths = new ArrayList<TreePath>();
            boolean addedPaths = false;
            // Keep track of the last tree path
            TreePath lastPath = null;
            // finds all the TreePaths to select
            for (PlayPenComponent comp : pp.getSelectedItems()) {
                TreePath tp = tree.getTreePathForNode((SQLObject) comp.getModel());
                if (!selectionPaths.contains(tp)) {
                    selectionPaths.add(tp);
                    addedPaths = true;
                    lastPath = tp;
                }

                if (comp instanceof TablePane) {
                    for (SQLColumn col : ((TablePane) comp).getSelectedItems()) {
                        tp = tree.getTreePathForNode(col);
                        if (!selectionPaths.contains(tp)) {
                            selectionPaths.add(tp);
                            addedPaths = true;
                            lastPath = tp;
                        }
                    }
                }
            }

            // Scroll to last tree path.
            if (lastPath != null) {
                tree.scrollPathToVisible(lastPath);
            }

            tree.setSelectionPaths(selectionPaths.toArray(new TreePath[selectionPaths.size()]));
            if (addedPaths) {
                tree.clearNonPlayPenSelections();
            }
        }

        /**
         * Selects the corresponding objects from the given TreePaths on the
         * PlayPen.
         * 
         * @param treePaths
         *            TreePaths containing the objects to select.
         */
        private void selectInPlayPen(TreePath[] treePaths) {
            if (eventDepth != 1)
                return;
            if (treePaths == null) {
                pp.selectNone();
            } else {
                List<SPObject> objects = new ArrayList<SPObject>();
                for (TreePath tp : treePaths) {
                    if (tree.isTargetDatabaseNode(tp) || !tree.isTargetDatabaseChild(tp))
                        continue;
                    SPObject obj = (SPObject) tp.getLastPathComponent();
                    // only select playpen represented objects.
                    if ((obj instanceof SQLTable || obj instanceof SQLRelationship || obj instanceof SQLColumn) &&
                            !objects.contains(obj)) {
                        objects.add(obj);
                    } else if (obj instanceof SQLRelationship.SQLImportedKey) {
                        objects.add(((SQLRelationship.SQLImportedKey) obj).getRelationship());
                    }
                }
                try {
                    pp.selectObjects(objects);
                } catch (SQLObjectException e) {
                    throw new SQLObjectRuntimeException(e);
                }
            }
        }

        public void itemDeselected(SelectionEvent e) {
            if (pp.isSelectionInProgress()) {
                return;
            }
            try {
                eventDepth++;
                updateDBTree();
            } finally {
                eventDepth--;
            }
        }

        public void itemSelected(SelectionEvent e) {
            if (pp.isSelectionInProgress()) {
                return;
            }
            try {
                eventDepth++;
                updateDBTree();
            } finally {
                eventDepth--;
            }
        }

        public void itemsDeselected(ItemSelectionEvent<SQLTable, SQLColumn> e) {
            if (pp.isSelectionInProgress()) {
                return;
            }
            try {
                eventDepth++;
                updateDBTree();
            } finally {
                eventDepth--;
            }
        }

        public void itemsSelected(ItemSelectionEvent<SQLTable, SQLColumn> e) {
            if (pp.isSelectionInProgress()) {
                return;
            }
            try {
                eventDepth++;
                updateDBTree();
            } finally {
                eventDepth--;
            }
        }

        public void valueChanged(TreeSelectionEvent e) {
            try {
                eventDepth++;
                selectInPlayPen(((JTree) e.getSource()).getSelectionPaths());
            } finally {
                eventDepth--;
            }
        }

        public void mouseClicked(MouseEvent e) {
            // don't care
        }

        public void mouseEntered(MouseEvent e) {
            // don't care
        }

        public void mouseExited(MouseEvent e) {
            // don't care
        }

        public void mousePressed(MouseEvent e) {
            // don't care
        }

        public void mouseReleased(MouseEvent e) {
            if (e.getSource() == pp) {
                eventDepth++;
                try {
                    updateDBTree();
                } finally {
                    eventDepth--;
                }
            }
        }
    }
}
