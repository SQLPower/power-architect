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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

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

public class OLAPTreeModel  implements TreeModel, java.io.Serializable{

    private final Schema schema;
    private ArchitectSwingSession session;
    
    protected LinkedList treeModelListeners;
    
    public OLAPTreeModel(Schema schema) {
        this.schema = schema;
        treeModelListeners = new LinkedList();
    }
    
    

    public Object getChild(Object parent, int index) {
        System.out.println("index = " + index);
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensionsEventList().size();
            System.out.println("dimSize = " + dimSize);
            int cubeSize = ((Schema) parent).getCubesEventList().size();
            System.out.println("cubeSize = " + cubeSize);
            int vCubeSize = ((Schema) parent).getVirtualCubesEventList().size();
            System.out.println("vCubeSize = " + vCubeSize);
            if (index < dimSize) {
                return ((Schema) parent).getDimensionsEventList().get(index);
            } else if (index < cubeSize + dimSize) {
                return ((Schema) parent).getCubesEventList().get(index-dimSize);
            } else if (index < vCubeSize + cubeSize + dimSize) {
                return ((Schema) parent).getVirtualCubesEventList().get(index-dimSize-cubeSize);
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensionsEventList().size();
            int measureSize = ((Cube) parent).getMeasuresEventList().size();
            if (index < dimSize) {
                return ((Cube) parent).getDimensionsEventList().get(index);
            } else if (index < measureSize + dimSize) {
                return ((Cube) parent).getMeasuresEventList().get(index-dimSize);
            } else {
                throw new IndexOutOfBoundsException();
            }
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        }
        else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensionsEventList().size();
            int measureSize = ((VirtualCube) parent).getMeasuresEventList().size();
            CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
            int cubeSize = (cubeUsage != null) ? cubeUsage.getCubeUsagesEventList().size() : 0;
            if (index < dimSize) {
                return ((VirtualCube) parent).getDimensionsEventList().get(index);
            } else if (index < measureSize + dimSize) {
                return ((VirtualCube) parent).getMeasuresEventList().get(index-dimSize);
            } else if (index < cubeSize + measureSize + dimSize) {
                String cubeName = ((VirtualCube) parent).getCubeUsage().getCubeUsagesEventList().get(index-dimSize-measureSize).getCubeName();
                return ((Schema) getRoot()).getCube(cubeName);
            } else {
                throw new IndexOutOfBoundsException();
            } 
        } else {
            throw new IllegalStateException(parent.getClass() + " doesn't do anything yet! (and probably never should)");
        }
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensionsEventList().size();
            int cubeSize = ((Schema) parent).getCubesEventList().size();
            int vCubeSize = ((Schema) parent).getVirtualCubesEventList().size();
            return dimSize + cubeSize + vCubeSize;
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensionsEventList().size();
            int measureSize = ((Cube) parent).getMeasuresEventList().size();
            return dimSize + measureSize;
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        }
        else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensionsEventList().size();
            int measureSize = ((VirtualCube) parent).getMeasuresEventList().size();
            CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
            int cubeSize = (cubeUsage != null) ? 
                    cubeUsage.getCubeUsagesEventList().size() : 0;
            return dimSize + measureSize + cubeSize; 
        } else {
            throw new IllegalStateException(parent.getClass() + " doesn't do anything yet! (and probably never should)");
        }
    }

    public int getIndexOfChild(Object parent, Object child) {
        int index = -2;
        if (parent instanceof Schema) {
            int dimSize = ((Schema) parent).getDimensionsEventList().size();
            int cubeSize = ((Schema) parent).getCubesEventList().size();
            if (child instanceof Dimension) {
                index = findItemIndex(((Schema) parent).getDimensionsEventList(), child);
            } else if (child instanceof Cube) {
                index = dimSize + findItemIndex(((Schema) parent).getCubesEventList(), child);
            } else if (child instanceof VirtualCube) {
                index = dimSize + cubeSize + findItemIndex(((Schema) parent).getCubesEventList(), child);
            } else {
                throw new IllegalArgumentException(parent.getClass() + " does not have child type " + child.getClass());
            }
        } else if (parent instanceof Cube) {
            int dimSize = ((Cube) parent).getDimensionsEventList().size();
            if (child instanceof CubeDimension) {
                index = findItemIndex(((Cube) parent).getDimensionsEventList(), child);
            } else if (child instanceof Measure) {
                index = dimSize + findItemIndex(((Cube) parent).getMeasuresEventList(), child);
            } else {
                throw new IllegalArgumentException(parent.getClass() + " does not have child type " + child.getClass());
            }
        } else if (parent instanceof Measure || parent instanceof Dimension) {
            throw new IllegalStateException("Dimensions and Measures do not have children.");
        }
        else if (parent instanceof VirtualCube) {
            int dimSize = ((VirtualCube) parent).getDimensionsEventList().size();
            int measureSize = ((VirtualCube) parent).getMeasuresEventList().size();
            if (child instanceof VirtualCubeDimension) {
                index = findItemIndex(((VirtualCube) parent).getDimensionsEventList(), child);
            } else if (child instanceof VirtualCubeMeasure) {
                index = dimSize + findItemIndex(((VirtualCube) parent).getMeasuresEventList(), child);
            } else if (child instanceof Cube){
                index = -1;
                String cubeName = ((Cube) child).getInstanceName();
                CubeUsages cubeUsage = ((VirtualCube) parent).getCubeUsage();
                int cubeUsageCount = (cubeUsage != null) ? cubeUsage.getCubeUsagesEventList().size() : 0;
                for (int i = 0; i < cubeUsageCount; i++) {
                    if (cubeUsage.getCubeUsagesEventList().get(i).getCubeName().equals(cubeName)) {
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
        return schema;
    }

    public boolean isLeaf(Object node) {
        if (node instanceof CubeDimension || node instanceof Measure || node instanceof VirtualCubeMeasure) {
            return true;
        }
        return false;
    }

    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.add(l);        
    }

    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.remove(l);        
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("model doesn't support editting yet");        
    }
    
    public static void main (String[] args) throws Exception {
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
        JTree tree = new JTree(new OLAPTreeModel(loadedSchema));
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

}
