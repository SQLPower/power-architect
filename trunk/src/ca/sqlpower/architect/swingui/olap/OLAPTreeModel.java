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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.MondrianXMLReader;
import ca.sqlpower.architect.olap.OLAPChildEvent;
import ca.sqlpower.architect.olap.OLAPChildListener;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.OLAPUtil;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;

public class OLAPTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(OLAPTreeModel.class);
    private final Schema schema;
    private final BusinessModelEventHandler modelEventHandler = new BusinessModelEventHandler();
    
    private final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    
    public OLAPTreeModel(Schema schema) {
        this.schema = schema;
        OLAPUtil.listenToHierarchy(schema, modelEventHandler, modelEventHandler);
    }
    
    public Object getChild(Object parent, int index) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChild("+parent+", "+index+")");
        }
        final Object child = ((OLAPObject) parent).getChildren().get(index);
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getChild: "+child);
        }
        return child;
    }

    public int getChildCount(Object parent) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChildCount("+parent+")");
        }
        final int childCount = ((OLAPObject) parent).getChildren().size();
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getChildCount: "+childCount);
        }
        return childCount;
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getIndexOfChild("+parent+", "+child+")");
        }
        int index = ((OLAPObject) parent).getChildren().indexOf(child);
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getIndexOfChild: "+index);
        }
        return index;
    }
    
    public Schema getRoot() {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getRoot()");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getRoot: "+schema);
        }
        return schema;
    }

    public boolean isLeaf(Object node) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> isLeaf("+node+")");
        }
        boolean retval = !((OLAPObject) node).allowsChildren();
        if (logger.isDebugEnabled()) {
            logger.debug("<<< isLeaf: "+retval);
        }
        return retval;
    }

    public void addTreeModelListener(TreeModelListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> addTreeModelListener("+l+")");
        }
        treeModelListeners.add(l);
        logger.debug("<<< addTreeModelListener");
    }

    public void removeTreeModelListener(TreeModelListener l) {
        if (logger.isDebugEnabled()) {
            logger.debug("removeTreeModelListener("+l+")");
        }
        treeModelListeners.remove(l);        
        logger.debug("<<< removeTreeModelListener");
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("model doesn't support editting yet");        
    }
    public static void main (String[] args) throws Exception {
        BasicConfigurator.configure();
//        Logger.getRootLogger().setLevel(Level.DEBUG);
        if (args.length == 0) {
            throw new RuntimeException("Please provide the Mondrian schema filename on the command line");
        }
        File file = new File(args[0]);
        Schema loadedSchema = MondrianXMLReader.parse(file);
        final JTree tree = new JTree(new OLAPTreeModel(loadedSchema));
        tree.setCellRenderer(new OLAPTreeCellRenderer());
        final JFrame f = new JFrame("Test schema tree");
        f.setContentPane(new JScrollPane(tree));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }
    
    /**
     * Adds listener to source's listener list and all of source's
     * children's listener lists recursively.
     */
//    public static void listenToHierarchy(PropertyChangeListener listener, ElementDef source, OLAPTreeModel treeModel) {
//        source.addPropertyChangeListener(listener);
//        if (!(source instanceof CubeDimension || source instanceof Measure || source instanceof VirtualCubeMeasure)) {
//            Iterator<ElementDef> it = getChildrenOfElementDef(source, treeModel).iterator();
//            while (it.hasNext()) {
//                listenToHierarchy(listener, (ElementDef) it.next(), treeModel);
//            }
//        }
//
//    }
    
    private static List<OLAPObject> getChildrenOfOLAPObject(OLAPObject ed, OLAPTreeModel treeModel) {
        List<OLAPObject> returnList = new ArrayList<OLAPObject>();
        if (ed instanceof Schema) {
            returnList.addAll(((Schema) ed).getDimensions());
            returnList.addAll(((Schema) ed).getCubes());
            returnList.addAll(((Schema) ed).getVirtualCubes());
            return returnList;
        } else if (ed instanceof Cube) {
            returnList.addAll(((Cube) ed).getDimensions());
            returnList.addAll(((Cube) ed).getMeasures());
            return returnList;
        } else if (ed instanceof VirtualCube) {
            returnList.addAll(((VirtualCube) ed).getDimensions());
            returnList.addAll(((VirtualCube) ed).getMeasures());
            CubeUsages cubeUsage = ((VirtualCube) ed).getCubeUsage();
            if (cubeUsage != null) {
                for(int i = 0; i < cubeUsage.getCubeUsages().size(); i++) {
                    String cubeName = cubeUsage.getCubeUsages().get(i).getCubeName();
                    Cube cube = OLAPUtil.findCube(treeModel.getRoot(), cubeName);
                    returnList.add(cube);
                }
                return returnList;
            } else {
                return returnList;
            }
        } else {
            return returnList;
        }
    }

    private class BusinessModelEventHandler implements OLAPChildListener, PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            fireTreeNodeChanged((OLAPObject) evt.getSource());
        }

        public void olapChildAdded(OLAPChildEvent e) {
            fireTreeNodeAdded(e.getSource(), e.getIndex(), e.getChild());
            OLAPUtil.listenToHierarchy(e.getChild(), this, this);
        }

        public void olapChildRemoved(OLAPChildEvent e) {
            fireTreeNodeRemoved(e.getSource(), e.getIndex(), e.getChild());
            OLAPUtil.unlistenToHierarchy(e.getChild(), this, this);
        }
    }
    
    private void fireTreeNodeChanged(OLAPObject node) {
        OLAPObject parent = node.getParent();
        
        int indexOfChild;
        if (node == getRoot()) {
            indexOfChild = 0;
        } else {
            indexOfChild = getIndexOfChild(parent, node);
        }
        
        TreeModelEvent e = new TreeModelEvent(this, pathToNode(parent), new int[] { indexOfChild }, new Object[] { node });
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesChanged(e);
        }
    }

    private void fireTreeNodeAdded(OLAPObject parent, int childIndex, OLAPObject child) {
        TreeModelEvent e = new TreeModelEvent(this, pathToNode(parent), new int[] { childIndex }, new Object[] { child });
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesInserted(e);
        }
    }

    private void fireTreeNodeRemoved(OLAPObject parent, int childIndex, OLAPObject child) {
        TreeModelEvent e = new TreeModelEvent(this, pathToNode(parent), new int[] { childIndex }, new Object[] { child });
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesRemoved(e);
        }
    }

    private TreePath pathToNode(OLAPObject o) {
        List<OLAPObject> path = new ArrayList<OLAPObject>();
        while (o != null) {
            path.add(0, o);
            o = o.getParent();
        }
        return new TreePath(path.toArray());
    }

}
