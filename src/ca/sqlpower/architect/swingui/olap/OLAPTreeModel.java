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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.olap.MondrianModel.CaptionExpression;
import ca.sqlpower.architect.olap.MondrianModel.ExpressionView;
import ca.sqlpower.architect.olap.MondrianModel.KeyExpression;
import ca.sqlpower.architect.olap.MondrianModel.MeasureExpression;
import ca.sqlpower.architect.olap.MondrianModel.NameExpression;
import ca.sqlpower.architect.olap.MondrianModel.OrdinalExpression;
import ca.sqlpower.architect.olap.MondrianModel.ParentExpression;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public class OLAPTreeModel implements TreeModel {

    private static final Logger logger = Logger.getLogger(OLAPTreeModel.class);
    private final Schema schema;
    private final BusinessModelEventHandler modelEventHandler = new BusinessModelEventHandler();
    
    private final List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
    
    /**
     * A list of classes that are not displayed on the tree.
     * 
     * Note: If you do not display a node on the tree you can never see it's
     * children.
     * Note: this does not hide classes that extend from other classes on the
     * list, it only hides the exact class.
     */
    private final List<Class<? extends ExpressionView>> hiddenClasses 
    = Arrays.asList(ExpressionView.class, CaptionExpression.class, 
            KeyExpression.class, MeasureExpression.class, NameExpression.class,
            OrdinalExpression.class, ParentExpression.class);
    
    public OLAPTreeModel(Schema schema) {
        this.schema = schema;
        SQLPowerUtils.listenToHierarchy(schema, modelEventHandler);
    }
    
    public Object getChild(Object parent, int index) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChild("+parent+", "+index+")");
        }
        final List<? extends SPObject> children = ((OLAPObject) parent).getChildren();
        int i = -1;
        // Skip over hidden classes.
        for (SPObject oo : children) {
            // Only count index for classes desired.
            if (!(hiddenClasses.contains(oo.getClass()))) {
                i++;
            }
            if (i == index) {
                if (logger.isDebugEnabled()) {
                    logger.debug("<<< getChild: "+oo);
                }
                return oo;
            }
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChildCount("+parent+")");
        }
        int childCount = 0;
        final List<? extends SPObject> children = ((OLAPObject) parent).getChildren();
        // Skip over hidden classes.
        for (SPObject oo : children) {
            // Only count for classes desired.
            if (!(hiddenClasses.contains(oo.getClass()))) {
                childCount++;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getChildCount: "+childCount);
        }
        return childCount;
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getIndexOfChild("+parent+", "+child+")");
        }
        int index = -1;
        final List<? extends SPObject> children = ((OLAPObject) parent).getChildren();
        // Skip over hidden classes.
        for (SPObject oo : children) {
            // Only include children for classes desired.
            if (!(hiddenClasses.contains(oo.getClass()))) {
                index++;
            }
            if (oo == child) {
                break;
            }
        }
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
    
    private class BusinessModelEventHandler implements SPListener {
        public void propertyChanged(PropertyChangeEvent evt) {
            fireTreeNodeChanged((OLAPObject) evt.getSource());
        }

        public void childAdded(SPChildEvent e) {
            fireTreeNodeAdded(e.getSource(), e.getIndex(), e.getChild());
            SQLPowerUtils.listenToHierarchy(e.getChild(), this);
        }

        public void childRemoved(SPChildEvent e) {
            fireTreeNodeRemoved(e.getSource(), e.getIndex(), e.getChild());
            SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
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
    
    private void fireTreeNodeChanged(OLAPObject node) {
        
        TreeModelEvent e;
        if (node == getRoot()) {
            // special case for root node
            e = new TreeModelEvent(this, new Object[] { getRoot() }, null, null);
        } else {
            SPObject parent = node.getParent();
            int indexOfChild = getIndexOfChild(parent, node);
            e = new TreeModelEvent(this, pathToNode(parent), new int[] { indexOfChild }, new Object[] { node });
        }
        
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesChanged(e);
        }
    }

    private void fireTreeNodeAdded(SPObject parent, int childIndex, SPObject child) {
        TreeModelEvent e = new TreeModelEvent(this, pathToNode(parent), new int[] { childIndex }, new Object[] { child });
        if (logger.isDebugEnabled()) {
            logger.debug("Firing tree node added: " + e);
        }
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesInserted(e);
        }
    }

    private void fireTreeNodeRemoved(SPObject parent, int childIndex, SPObject child) {
        TreeModelEvent e = new TreeModelEvent(this, pathToNode(parent), new int[] { childIndex }, new Object[] { child });
        for (int i = treeModelListeners.size() - 1; i >= 0; i--) {
            treeModelListeners.get(i).treeNodesRemoved(e);
        }
    }

    private TreePath pathToNode(SPObject o) {
        List<SPObject> path = new ArrayList<SPObject>();
        while (o != null) {
            path.add(0, (OLAPObject) o);
            if (o == getRoot()) break;
            o = o.getParent();
        }
        return new TreePath(path.toArray());
    }

}
