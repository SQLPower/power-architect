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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLObject;

public class DnDTreePathTransferable implements Transferable, java.io.Serializable {
    
    private static final Logger logger = Logger.getLogger(DnDTreePathTransferable.class);

    public static final DataFlavor TREEPATH_ARRAYLIST_FLAVOR = new DataFlavor
		(ArrayList.class, "List of selected tree paths");
    
    public static final DataFlavor TREE_OBJECT_NAMES = new DataFlavor(String[].class, "List of selected object names"); 
	
	protected ArrayList<int[]> data;
	
	protected String[] objectNames;
	
    private final String userVisibleName;
	
	public DnDTreePathTransferable(ArrayList<int[]> data, String userVisibleName) {  
		this.data = data;
        this.userVisibleName = userVisibleName;
	}
	
	public DnDTreePathTransferable(ArrayList<int[]> data, String userVisibleName, TreePath[] selectedObjects) {  
	    this(data,userVisibleName);
	    objectNames = getObjectNames(selectedObjects);
	}
	    
	
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { TREEPATH_ARRAYLIST_FLAVOR, TREE_OBJECT_NAMES };
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return (flavor.equals(TREEPATH_ARRAYLIST_FLAVOR) || flavor.equals(TREE_OBJECT_NAMES));
	}
	
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException {
	    
		if (flavor == TREEPATH_ARRAYLIST_FLAVOR) {
		    return data;	
		}
		else if(flavor == TREE_OBJECT_NAMES){
		    return objectNames;
		}
		else{
		    throw new IllegalArgumentException("Unsupported flavor "+flavor);
		}
	}
	
	/**
     * Creates an integer array which holds the child indices of each
     * node starting from the root which lead to node "node."
     * <p>
     * This has the distinct disadvantage of complete lack of context. If you
     * drag an item from one dbtree to another playpen, the playpen can't
     * tell that the drop came from a different object tree, and it will do
     * the lookup based on the same set of offsets in its own tree. Sometimes
     * it will be a valid path (but to the wrong item) and sometimes it will
     * be an invalid path (because one of the steps is out of range). Either
     * way, you get the wrong answer.
     *
     * @param node The SQLObject you want the path to
     */
	public static int[] getDnDPathToNode(SQLObject node, SQLObject treeRoot) {
	    try {
	        List<Integer> path = new ArrayList<Integer>();
	        while (node != null) {
	            logger.debug("" + path.size() + ": " + node + " parent="+node.getParent());
	            SQLObject parent = node.getParent();
	            if (parent == null) break;
	            path.add(0, parent.getChildren().indexOf(node));
	            node = parent;
	        }

	        if (node != null) {
	            path.add(0, treeRoot.getChildren().indexOf(node));
	        }
	        
	        int[] retval = new int[path.size()];
	        for (int i = 0; i < retval.length; i++) {
	            retval[i] = path.get(i);
	        }
	        
	        logger.debug("Created path: " + Arrays.toString(retval));
	        

	        return retval;
	    } catch (ArchitectException e) {
	        throw new ArchitectRuntimeException(e);
	    }
    }

	public static SQLObject getNodeForDnDPath(SQLObject root, int[] path) throws ArchitectException {
	    logger.debug("Resolving path " + root + ": " + Arrays.toString(path));
	    SQLObject current = root;
	    for (int i = 0; i < path.length; i++) {
	        current = current.getChild(path[i]);
	    }
	    return current;
	}
	
	/**
	 * Takes the tree paths to store the names of the SQLObjects into an ArrayList which will be used as a flavor.
	 */
	 private String[] getObjectNames(TreePath[] treePaths){
	     String[] nodeNames = new String[treePaths.length];
	     for(int i = 0; i < treePaths.length; i++){
	         nodeNames[i]= ((SQLObject)treePaths[i].getLastPathComponent()).getName();
	     }
	     return nodeNames;
	 }


	@Override
	public String toString() {
	    return userVisibleName;
	}
}

