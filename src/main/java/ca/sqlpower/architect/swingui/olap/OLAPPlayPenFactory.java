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

package ca.sqlpower.architect.swingui.olap;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPSession;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.undo.OLAPUndoManager;
import ca.sqlpower.architect.swingui.ArchitectSwingProject;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenComponentLocationEdit;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleEvent;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.DimensionPane.HierarchySection;
import ca.sqlpower.object.AbstractSPListener;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.undo.PropertyChangeEdit;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public class OLAPPlayPenFactory {

    private static final Logger logger = Logger.getLogger(OLAPPlayPenFactory.class);

    public static PlayPen createPlayPen(
            ArchitectSwingSession session,
            OLAPEditSession oSession,
            OLAPUndoManager undoManager) {
        
        if (session == null) {
            throw new NullPointerException("Null session");
        }
        if (oSession == null) {
            throw new NullPointerException("Null oSession");
        }
         
        ArchitectSwingProject project = session.getWorkspace();
        OLAPSession olapSession = oSession.getOlapSession();        
        PlayPenContentPane contentPane = project.getOlapContentPane(olapSession);
        PlayPen pp;
        
        if (contentPane != null) {
            pp = new PlayPen(session, contentPane);            
        } else {
            pp = new PlayPen(session, olapSession);
            project.addOLAPContentPane(pp.getContentPane());            
        }
        
        OLAPModelListener ppcl = new OLAPModelListener(pp, oSession);
        pp.addPlayPenLifecycleListener(ppcl);
        
        pp.setPopupFactory(new OLAPContextMenuFactory(session, oSession));
        SQLPowerUtils.listenToHierarchy(oSession.getOlapSession().getSchema(), ppcl);
        
        SelectionSynchronizer synchronizer = new SelectionSynchronizer(oSession.getOlapTree(), pp);
        pp.addSelectionListener(synchronizer);
        oSession.getOlapTree().addTreeSelectionListener(synchronizer);
        pp.getContentPane().addSPListener(synchronizer);
        
        PlayPenUndoAdapter undoAdapter = new PlayPenUndoAdapter(undoManager);        
        pp.getContentPane().addComponentPropertyListener(undoAdapter);
        pp.addUndoEventListener(undoAdapter);
        
        return pp;
    }

    /**
     * Sets up OLAP-specific keyboard actions on the playpen. This is done
     * separately because the OLAP session has to be finished creating the
     * actions before this will work, but it needs a playpen before the actions
     * can be created.
     * 
     * @param pp
     *            The playpen to register the keyboard actions on.
     * @param oSession
     *            The session pp belongs to, also the session that owns the
     *            actions to register.
     */
    static void setupOLAPKeyboardActions(PlayPen pp, OLAPEditSession oSession) {
        pp.setupKeyboardActions();
        
        InputMap im = pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = pp.getActionMap();
        
        if (im == null) {
            throw new NullPointerException("Null input map");
        }
        if (am == null) {
            throw new NullPointerException("Null action map");
        }
        
        
        String KEY_DELETE_SELECTED = "ca.sqlpower.architect.swingui.PlayPen.KEY_DELETE_SELECTED"; //$NON-NLS-1$

        InputMap inputMap = pp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), KEY_DELETE_SELECTED);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), KEY_DELETE_SELECTED);
        pp.getActionMap().put(KEY_DELETE_SELECTED, oSession.getOLAPDeleteSelectedAction());

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) oSession.getZoomToFitAction().getValue(Action.ACCELERATOR_KEY), "ZOOM TO FIT"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM TO FIT", oSession.getZoomToFitAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) oSession.getZoomInAction().getValue(Action.ACCELERATOR_KEY), "ZOOM IN"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM IN", oSession.getZoomInAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) oSession.getZoomOutAction().getValue(Action.ACCELERATOR_KEY), "ZOOM OUT"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM OUT", oSession.getZoomOutAction()); //$NON-NLS-1$

        pp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put((KeyStroke) oSession.getZoomNormalAction().getValue(Action.ACCELERATOR_KEY), "ZOOM RESET"); //$NON-NLS-1$
        pp.getActionMap().put("ZOOM RESET", oSession.getZoomNormalAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateCubeAction().getValue(Action.ACCELERATOR_KEY), "NEW CUBE"); //$NON-NLS-1$
        am.put("NEW CUBE", oSession.getCreateCubeAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateVirtualCubeAction().getValue(Action.ACCELERATOR_KEY), "NEW VIRTUAL CUBE"); //$NON-NLS-1$
        am.put("NEW VIRTUAL CUBE", oSession.getCreateVirtualCubeAction()); //$NON-NLS-1$

        im.put((KeyStroke) oSession.getCreateMeasureAction().getValue(Action.ACCELERATOR_KEY), "NEW MEASURE"); //$NON-NLS-1$
        am.put("NEW MEASURE", oSession.getCreateMeasureAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateDimensionAction().getValue(Action.ACCELERATOR_KEY), "NEW DIMENSION"); //$NON-NLS-1$
        am.put("NEW DIMENSION", oSession.getCreateDimensionAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateDimensionUsageAction().getValue(Action.ACCELERATOR_KEY), "NEW DIMENSION USAGE"); //$NON-NLS-1$
        am.put("NEW DIMENSION USAGE", oSession.getCreateDimensionUsageAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateCubeUsageAction().getValue(Action.ACCELERATOR_KEY), "NEW CUBE USAGE"); //$NON-NLS-1$
        am.put("NEW CUBE USAGE", oSession.getCreateCubeUsageAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateHierarchyAction().getValue(Action.ACCELERATOR_KEY), "NEW HIERARCHY"); //$NON-NLS-1$
        am.put("NEW HIERARCHY", oSession.getCreateHierarchyAction()); //$NON-NLS-1$

        im.put((KeyStroke) oSession.getCreateLevelAction().getValue(Action.ACCELERATOR_KEY), "NEW LEVEL"); //$NON-NLS-1$
        am.put("NEW LEVEL", oSession.getCreateLevelAction()); //$NON-NLS-1$
    }

    /**
     * An instance of this OLAPChildListener will listen to tree model structural
     * changes.
     * <p>
     * It is used for playpen to update its contentpane when business model
     * structure changes.
     */
    private static class OLAPModelListener implements SPListener, PlayPenLifecycleListener {
        
        private final PlayPen pp;
        private final OLAPEditSession session;

        /**
         * Keeps track of the components for business model object that have
         * been removed. This way, the components can be restored when the
         * removal is undone (or the addition is redone).
         */
        private final WeakHashMap<OLAPObject, RemovedComponentInfo> removedPPCs =
            new WeakHashMap<OLAPObject, RemovedComponentInfo>();

        /**
         * Stores the information necessary to revive the GUI part of an
         * OLAPObject that was previously deleted and has come back
         * "from the dead."
         * <p>
         * The components are held in weak references because they themselves
         * have strong references to the OLAPObjects (model) and this class is
         * designed to be the value in a weak hash map. See the WeakHashMap
         * class-level documentation for an explanation of why this is
         * important. At first, it may appear that the PlayPenComponent could be
         * garbage collected before the OLAPObject, but this is not the case:
         * The playpen component is on the OLAPObject's listener list, which is
         * a strong reference from key to value in this map.
         */
        private static class RemovedComponentInfo {
            
            /**
             * The component that was removed.
             */
            WeakReference<PlayPenComponent> weakComponentRef;

            /**
             * The index of the component within the content pane's child
             * list at the time it was removed.
             */
            int index;
     
            RemovedComponentInfo(PlayPenComponent removed, int position) {
                weakComponentRef = new WeakReference<PlayPenComponent>(removed);
                this.index = position;
            }
            
            public PlayPenComponent getComponent() {
                return weakComponentRef.get();
            }
            
            public int getIndex() {
                return index;
            }
        }

        public OLAPModelListener(PlayPen pp, OLAPEditSession oSession) {
            this.pp = pp;
            session = oSession;
        }
        
        public void childAdded(SPChildEvent e) {
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
            RemovedComponentInfo compInfo = removedPPCs.get(e.getChild());
            logger.debug("OLAP Child was added. Previously removed component: " + compInfo);
            if (compInfo != null && pp.getContentPane().isMagicEnabled() && e.getSource().isMagicEnabled()) {
                PlayPenComponent ppc = compInfo.getComponent();
                int oldIndex = compInfo.getIndex();
                pp.getContentPane().addChild(ppc, oldIndex);
            }
        }

        public void childRemoved(SPChildEvent e) {
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
            // Go through the list backwards when removing to eliminate problems.
            if (pp.getContentPane().isMagicEnabled() && e.getSource().isMagicEnabled()) {
                for (int j = pp.getContentPane().getChildren().size() - 1; j >= 0; j--) {
                    PlayPenComponent ppc = pp.getContentPane().getChildren().get(j);
                    if (ppc.getModel() == e.getChild()) {
                        ppc.setSelected(false, SelectionEvent.SINGLE_SELECT);
                        try {
                            pp.getContentPane().removeChild(ppc);
                        } catch (ObjectDependentException ex) {
                            throw new RuntimeException(ex);
                        }
                        removedPPCs.put((OLAPObject) e.getChild(), new RemovedComponentInfo(ppc, j));
                        logger.debug("Put dead component in map: " + e.getChild().getName() + " -> " + ppc + " @ " + j);
                    }
                } 
            }
        }

        /**
         * Clean up after playpen life cycle ends. This can be the case when a
         * temporary playpen is created and then destroyed. This will
         * automatically detach itself from the business model, so that the
         * temporary playpen can be garbage collected.
         */
        public void PlayPenLifeEnding(PlayPenLifecycleEvent e) {
            OLAPUtil.unlistenToHierarchy(session.getOlapSession().getSchema(), this, null);
        }

        public void propertyChanged(PropertyChangeEvent evt) {
            //no-op            
        }

        public void transactionEnded(TransactionEvent e) {
            //no-op            
        }

        public void transactionRollback(TransactionEvent e) {
            //no-op            
        }

        public void transactionStarted(TransactionEvent e) {
            //no-op            
        }
    }
    
    static class PlayPenUndoAdapter extends AbstractSPListener {

        private final OLAPUndoManager undoManager;

        PlayPenUndoAdapter(OLAPUndoManager undoManager) {
            this.undoManager = undoManager;
        }

        public void propertyChanged(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("topLeftCorner")) {
                // this edit will be absorbed by our PlayPenComponentLocationEdit
                PropertyChangeEdit edit = new PropertyChangeEdit(e);
                undoManager.addEdit(edit);
            }
        }

        public void transactionStarted(TransactionEvent e) {
            undoManager.addEdit(new PlayPenComponentLocationEdit());
        }
        
        public void transactionEnded(TransactionEvent e) {
            // the location edit will simply stop absorbing
            // edits because new edits are of the wrong type
        }
        
    }
    
    static class SelectionSynchronizer 
    extends AbstractSPListener implements SelectionListener, 
    ItemSelectionListener<Cube, OLAPObject>, TreeSelectionListener {

        private int eventDepth = 0;
        private final OLAPTree tree;
        private final PlayPen pp;
        
        public SelectionSynchronizer(OLAPTree tree, PlayPen pp) {
            this.tree = tree;
            this.pp = pp;
        }
        
        /**
         * Synchronizes the olapTree selection with the playpen selections
         * @throws SQLObjectException 
         * 
         */
        public void updateOLAPTree() {
            if (eventDepth != 1) return;
            tree.clearSelection();
            List<TreePath> selectionPaths = new ArrayList<TreePath>();
            boolean addedPaths = false;
            // Keep track of the last tree path
            TreePath lastPath = null;
            // finds all the TreePaths to select
            for (PlayPenComponent comp : pp.getSelectedItems()) {
                TreePath tp = tree.getTreePathForNode((OLAPObject) comp.getModel());
                if (!selectionPaths.contains(tp)) {
                    selectionPaths.add(tp);
                    addedPaths = true;
                    lastPath = tp;
                }

                if (comp instanceof VirtualCubePane) {
                    for (OLAPObject oo :((VirtualCubePane) comp).getSelectedItems()) {
                        tp = tree.getTreePathForNode(oo);
                        if (!selectionPaths.contains(tp)) {
                            selectionPaths.add(tp);
                            addedPaths = true;
                            lastPath = tp;
                        }
                    }
                } else if (comp instanceof CubePane) {
                    for (OLAPObject oo :((CubePane) comp).getSelectedItems()) {
                        tp = tree.getTreePathForNode(oo);
                        if (!selectionPaths.contains(tp)) {
                            selectionPaths.add(tp);
                            addedPaths = true;
                            lastPath = tp;
                        }
                    }
                } else if (comp instanceof DimensionPane) {
                    for (OLAPObject oo :((DimensionPane) comp).getSelectedItems()) {
                        tp = tree.getTreePathForNode(oo);
                        if (!selectionPaths.contains(tp)) {
                            selectionPaths.add(tp);
                            addedPaths = true;
                            lastPath = tp;
                        }
                    }
                    for (PaneSection<? extends OLAPObject> sect :((DimensionPane) comp).getSelectedSections()) {
                        Hierarchy hierarchy;
                        if (sect instanceof HierarchySection) {
                            hierarchy = ((HierarchySection) sect).getHierarchy();
                        } else {
                            throw new IllegalArgumentException("Unknown section type " + sect.getClass() + " in a DimensionPane!");
                        }
                        tp = tree.getTreePathForNode(hierarchy);
                        if (!selectionPaths.contains(tp)) {
                            selectionPaths.add(tp);
                            addedPaths = true;
                            lastPath = tp;
                        }
                    }
                } else if (comp instanceof UsageComponent) {
                    tp = tree.getTreePathForNode(((UsageComponent) comp).getModel());
                    if (!selectionPaths.contains(tp)) {
                        selectionPaths.add(tp);
                        addedPaths = true;
                        lastPath = tp;
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
         * Selects the corresponding objects from the give TreePaths on the PlayPen.
         * 
         * @param treePaths TreePaths containing the objects to select.
         */
        private void selectInPlayPen(TreePath[] treePaths) {
            if (eventDepth != 1) return;
            if (treePaths == null) {
                pp.selectNone();
            } else {
                List<OLAPObject> objects = new ArrayList<OLAPObject>();
                for (TreePath tp : treePaths) {
                    OLAPObject obj = (OLAPObject) tp.getLastPathComponent();
                    objects.add(obj);
                }
                try {
                    pp.selectObjects(objects, tree);
                } catch (SQLObjectException e) {
                    throw new SQLObjectRuntimeException(e);
                }
            }
        }

        public void itemDeselected(SelectionEvent e) {
            try {
                eventDepth++;
                updateOLAPTree();
            } finally {
                eventDepth--;
            }
        }

        public void itemSelected(SelectionEvent e) {
            try {
                eventDepth++;
                updateOLAPTree();
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

        public void childAdded(SPChildEvent evt) {
            if (evt.getChild() instanceof CubePane) {
                ((CubePane) evt.getChild()).addItemSelectionListener(this);
            }
        }

        public void childRemoved(SPChildEvent evt) {
            if (evt.getChild() instanceof CubePane) {
                ((CubePane) evt.getChild()).removeItemSelectionListener(this);
            }
        }

        public void itemsDeselected(ItemSelectionEvent<Cube, OLAPObject> e) {
            try {
                eventDepth++;
                updateOLAPTree();
            } finally {
                eventDepth--;
            }
        }

        public void itemsSelected(ItemSelectionEvent<Cube, OLAPObject> e) {
            try {
                eventDepth++;
                updateOLAPTree();
            } finally {
                eventDepth--;
            }
        }
    }
}