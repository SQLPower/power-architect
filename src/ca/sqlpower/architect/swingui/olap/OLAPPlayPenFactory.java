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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.event.ItemSelectionEvent;
import ca.sqlpower.architect.swingui.event.ItemSelectionListener;
import ca.sqlpower.architect.swingui.event.PlayPenContentEvent;
import ca.sqlpower.architect.swingui.event.PlayPenContentListener;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleEvent;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.DimensionPane.HierarchySection;

public class OLAPPlayPenFactory {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OLAPPlayPenFactory.class);

    public static PlayPen createPlayPen(ArchitectSwingSession session, OLAPEditSession oSession) {
        if (session == null) {
            throw new NullPointerException("Null session");
        }
        if (oSession == null) {
            throw new NullPointerException("Null oSession");
        }
        
        PlayPen pp = new PlayPen(session);
        OLAPModelListener ppcl = new OLAPModelListener(pp, oSession);
        pp.addPlayPenLifecycleListener(ppcl);
        
        pp.setPopupFactory(new ContextMenuFactory(session, oSession));
        OLAPUtil.listenToHierarchy(oSession.getOlapSession().getSchema(), ppcl, null);
        
        SelectionSynchronizer synchronizer = new SelectionSynchronizer(oSession.getOlapTree(), pp);
        pp.addSelectionListener(synchronizer);
        oSession.getOlapTree().addTreeSelectionListener(synchronizer);
        pp.getContentPane().addPlayPenContentListener(synchronizer);
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

        im.put((KeyStroke) oSession.getCreateMeasureAction().getValue(Action.ACCELERATOR_KEY), "NEW MEASURE"); //$NON-NLS-1$
        am.put("NEW MEASURE", oSession.getCreateMeasureAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateDimensionAction().getValue(Action.ACCELERATOR_KEY), "NEW DIMENSION"); //$NON-NLS-1$
        am.put("NEW DIMENSION", oSession.getCreateDimensionAction()); //$NON-NLS-1$
        
        im.put((KeyStroke) oSession.getCreateHierarchyAction().getValue(Action.ACCELERATOR_KEY), "NEW HIERARCHY"); //$NON-NLS-1$
        am.put("NEW HIERARCHY", oSession.getCreateHierarchyAction()); //$NON-NLS-1$

        im.put((KeyStroke) oSession.getCreateLevelAction().getValue(Action.ACCELERATOR_KEY), "NEW LEVEL"); //$NON-NLS-1$
        am.put("NEW LEVEL", oSession.getCreateLevelAction()); //$NON-NLS-1$
}
    
    /**
     * Sets up scroll wheel actions on the playpen. This is done
     * separately because the wheelMouseMoved event requires the scroll pane be
     * built before it can work. The ZoomIn and ZoomOut actions also need a 
     * playpen before the actions can be created.
     * 
     * @param pp
     *            The playpen to register the mouse wheel actions on.
     * @param oSession
     *            The session pp belongs to, also the session that owns the
     *            actions to register and ScrollPane.
     */
    public static void setupOLAPMouseWheelActions(PlayPen pp, OLAPEditSession oSession) {
        pp.setMouseZoomInAction(oSession.getZoomInAction());
        pp.setMouseZoomOutAction(oSession.getZoomOutAction());
        pp.setScrollPane(oSession.getPPScrollPane());
    }
    /**
     * An instance of this OLAPChildListener will listen to tree model structural
     * changes.
     * <p>
     * It is used for playpen to update its contentpane when business model
     * structure changes.
     */
    private static class OLAPModelListener implements OLAPChildListener, PlayPenLifecycleListener {
        
        private final PlayPen pp;
        private final OLAPEditSession session;

        public OLAPModelListener(PlayPen pp, OLAPEditSession oSession) {
            this.pp = pp;
            session = oSession;
        }
        
        public void olapChildAdded(OLAPChildEvent e) {
            OLAPUtil.listenToHierarchy(e.getChild(), this, null);
            
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, null);
            Iterator<PlayPenComponent> it = pp.getPlayPenComponents().iterator();
            while (it.hasNext()) {
                PlayPenComponent ppc = it.next();
                if (ppc.getModel() == e.getChild()) {
                    ppc.setSelected(false, SelectionEvent.SINGLE_SELECT);
                    it.remove();
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
    }
    
    static class SelectionSynchronizer 
    implements SelectionListener,
            ItemSelectionListener<Cube, OLAPObject>,
            TreeSelectionListener, PlayPenContentListener {

        private int eventDepth = 0;
        private final OLAPTree tree;
        private final PlayPen pp;
        
        public SelectionSynchronizer(OLAPTree tree, PlayPen pp) {
            this.tree = tree;
            this.pp = pp;
        }
        
        /**
         * Synchronizes the olapTree selection with the playpen selections
         * @throws ArchitectException 
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
                    for (PaneSection<? extends Level> sect :((DimensionPane) comp).getSelectedSections()) {
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
                } catch (ArchitectException e) {
                    throw new ArchitectRuntimeException(e);
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

        public void PlayPenComponentAdded(PlayPenContentEvent e) {
            if (e.getPlayPenComponent() instanceof CubePane) {
                ((CubePane) e.getPlayPenComponent()).addItemSelectionListener(this);
            }
        }

        public void PlayPenComponentRemoved(PlayPenContentEvent e) {
            if (e.getPlayPenComponent() instanceof CubePane) {
                ((CubePane) e.getPlayPenComponent()).removeItemSelectionListener(this);
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