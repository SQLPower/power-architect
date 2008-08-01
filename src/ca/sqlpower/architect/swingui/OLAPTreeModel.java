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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eigenbase.xom.DOMWrapper;
import org.eigenbase.xom.ElementDef;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XOMUtil;

import ca.sqlpower.architect.olap.MondrianDef.Cube;
import ca.sqlpower.architect.olap.MondrianDef.CubeDimension;
import ca.sqlpower.architect.olap.MondrianDef.CubeUsages;
import ca.sqlpower.architect.olap.MondrianDef.Dimension;
import ca.sqlpower.architect.olap.MondrianDef.Measure;
import ca.sqlpower.architect.olap.MondrianDef.Schema;
import ca.sqlpower.architect.olap.MondrianDef.VirtualCube;
import ca.sqlpower.architect.olap.MondrianDef.VirtualCubeDimension;
import ca.sqlpower.architect.olap.MondrianDef.VirtualCubeMeasure;

public class OLAPTreeModel  implements TreeModel, PropertyChangeListener, java.io.Serializable{

    private static final Logger logger = Logger.getLogger(OLAPTreeModel.class);
    private final Schema schema;
    
    private final LinkedList<TreeModelListener> treeModelListeners;
    
    public OLAPTreeModel(Schema schema) {
        this.schema = schema;
        treeModelListeners = new LinkedList<TreeModelListener>();
//        listenToHierarchy(this, schema, this);
    }
    
    

    public Object getChild(Object parent, int index) {
        System.err.println("getChild: log debugenabled is " + logger.isDebugEnabled());
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChild("+parent+", "+index+")");
        }
        final Object child;
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensions().size();
            int cubeSize = ((Schema) parent).getCubes().size();
            int vCubeSize = ((Schema) parent).getVirtualCubes().size();
            if (index < dimSize) {
                child = ((Schema) parent).getDimensions().get(index);
            } else if (index < cubeSize + dimSize) {
                child = ((Schema) parent).getCubes().get(index-dimSize);
            } else if (index < vCubeSize + cubeSize + dimSize) {
                child = ((Schema) parent).getVirtualCubes().get(index-dimSize-cubeSize);
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensions().size();
            int measureSize = ((Cube) parent).getMeasures().size();
            if (index < dimSize) {
                child = ((Cube) parent).getDimensions().get(index);
            } else if (index < measureSize + dimSize) {
                child = ((Cube) parent).getMeasures().get(index-dimSize);
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        } else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensions().size();
            int measureSize = ((VirtualCube) parent).getMeasures().size();
            CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
            int cubeSize = (cubeUsage != null) ? cubeUsage.getCubeUsages().size() : 0;
            if (index < dimSize) {
                child = ((VirtualCube) parent).getDimensions().get(index);
            } else if (index < measureSize + dimSize) {
                child = ((VirtualCube) parent).getMeasures().get(index-dimSize);
            } else if (index < cubeSize + measureSize + dimSize) {
                String cubeName = ((VirtualCube) parent).getCubeUsage().getCubeUsages().get(index-dimSize-measureSize).getCubeName();
                child = ((Schema) getRoot()).getCube(cubeName);
            } else {
                throw new IndexOutOfBoundsException();
            } 
        } else {
            throw new IllegalStateException(parent.getClass() + " doesn't do anything yet! (and probably never should)");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getChild: "+child);
        }
        return child;
    }

    public int getChildCount(Object parent) {
        if (logger.isDebugEnabled()) {
            logger.debug(">>> getChildCount("+parent+")");
        }
        int childCount;
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensions().size();
            int cubeSize = ((Schema) parent).getCubes().size();
            int vCubeSize = ((Schema) parent).getVirtualCubes().size();
            childCount = dimSize + cubeSize + vCubeSize;
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensions().size();
            int measureSize = ((Cube) parent).getMeasures().size();
            childCount = dimSize + measureSize;
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        } else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensions().size();
            int measureSize = ((VirtualCube) parent).getMeasures().size();
            CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
            int cubeSize = (cubeUsage != null) ? 
                    cubeUsage.getCubeUsages().size() : 0;
            childCount = dimSize + measureSize + cubeSize; 
        } else {
            throw new IllegalStateException(parent.getClass() + " doesn't do anything yet! (and probably never should)");
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
        int index = -2;
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensions().size();
            int cubeSize = ((Schema) parent).getCubes().size();
            if (child instanceof Dimension) {
                index = findItemIndex(((Schema) parent).getDimensions(), child);
            } else if (child instanceof Cube) {
                index = dimSize + findItemIndex(((Schema) parent).getCubes(), child);
            } else if (child instanceof VirtualCube) {
                index = dimSize + cubeSize + findItemIndex(((Schema) parent).getCubes(), child);
            } else {
                throw new IllegalArgumentException(parent.getClass() + " does not have child type " + child.getClass());
            }
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensions().size();
            if (child instanceof CubeDimension) {
                index = findItemIndex(((Cube) parent).getDimensions(), child);
            } else if (child instanceof Measure) {
                index = dimSize + findItemIndex(((Cube) parent).getMeasures(), child);
            } else {
                throw new IllegalArgumentException(parent.getClass() + " does not have child type " + child.getClass());
            }
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        }
        else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensions().size();
            int measureSize = ((VirtualCube) parent).getMeasures().size();
            if (child instanceof VirtualCubeDimension) {
                index = findItemIndex(((VirtualCube) parent).getDimensions(), child);
            } else if (child instanceof VirtualCubeMeasure) {
                index = dimSize + findItemIndex(((VirtualCube) parent).getMeasures(), child);
            } else if (child instanceof Cube){
                index = -1;
                String cubeName = ((Cube) child).getInstanceName();
                System.out.println("cubeName = " +  cubeName);
                CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
                int cubeUsageCount = (cubeUsage != null) ? cubeUsage.getCubeUsages().size() : 0;
                for (int i = 0; i < cubeUsageCount; i++) {
                    System.out.println(cubeUsage.getCubeUsages().get(i).getCubeName());
                    if (cubeUsage.getCubeUsages().get(i).getCubeName().equals(cubeName)) {
                        index = dimSize + measureSize + i;
                    }
                }
            } else {
                throw new IllegalArgumentException(parent.getClass() + " does not have child type " + child.getClass());
            }
        } else {
            throw new IllegalStateException(parent.getClass() + " doesn't do anything yet! (and probably never should)");
        }
        if (index == -1) {
            throw new RuntimeException("child " + child.getClass() + " object not found in parent " + parent.getClass() + ".");
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("<<< getIndexOfChild: "+index);
        }
        return index;
    }
    
    private int findItemIndex(List<? extends ElementDef> list, Object obj){
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    public Object getRoot() {
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
        boolean retval;
        if (node instanceof CubeDimension || node instanceof Measure || node instanceof VirtualCubeMeasure) {
            retval = true;
        } else {
            retval = false;
        }
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
//        logger.setLevel(Level.DEBUG);
        if (args.length == 0) {
            throw new RuntimeException("Please provide the Mondrian schema filename on the command line");
        }
        File file = new File(args[0]);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);
        StringBuilder xml = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            xml.append(line);
            line = reader.readLine();
        }

        final Parser xmlParser = XOMUtil.createDefaultParser();
        final DOMWrapper def = xmlParser.parse(xml.toString());

        Schema loadedSchema = new Schema(def);
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
    
    private static List<ElementDef> getChildrenOfElementDef(ElementDef ed, OLAPTreeModel treeModel) {
        List<ElementDef> returnList = new ArrayList<ElementDef>();
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
                    Cube cube = ((Schema) treeModel.getRoot()).getCube(cubeName);
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

    public void propertyChange(PropertyChangeEvent evt) {
        System.out.println("Property changed " + evt.getPropertyName());
    }

//    private void fireTreeModelEvent() {
//        TreeModelEvent e = new TreeModelEvent()
//    }
}
