package ca.sqlpower.architect.swingui;

import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.tree.*;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.ArchitectException;

public class DBTreeNode implements MutableTreeNode {
	private static final Logger logger = Logger.getLogger(DBTreeNode.class);
	
	protected static Map userObjectToTreeNodeMap = new HashMap();

	/**
	 * The user object of a DBTreeNode is aways a subclass of
	 * SQLObject.
	 */
	public SQLObject userObject;

	/**
	 * We allow any node in a JTree to be our parent.
	 */
	protected TreeNode parent;

	/**
	 * Makes a DBTreeNode with the given SQLObject as its user object.
	 * If the userObject has a parent SQLObject, this node will return
	 * that SQLObject in its getParent() method.
	 */
	public DBTreeNode(SQLObject userObject) {
		DBTreeNode parent = (DBTreeNode) userObjectToTreeNodeMap.get(userObject.getParent());
		setup(userObject, parent);
	}

	/**
	 * Only use this if this node is not under a SQLObject.  It's ok
	 * to put any SQLObject under any non-SQLObject, but you may not
	 * specify a SQLObject parent because that would be really weird.
	 *
	 * @throws IllegalArgumentException if you are weird. (see above)
	 */
	public DBTreeNode(SQLObject userObject, TreeNode parent) {
		if (parent instanceof SQLObject) {
			throw new IllegalArgumentException
				("Don't use DBTreeNode(SQLObject,Object) to add a node with a "
				 +"SQLObject as its parent; use DBTreeNode(SQLObject) instead.");
		}
		setup(userObject, parent);
	}

	/**
	 * Sets up the required instance variables and inserts this node
	 * into the userObjectToTreeNodeMap.
	 */
	protected void setup(SQLObject userObject, TreeNode parent) {
		this.userObject = userObject;
		userObjectToTreeNodeMap.put(userObject, this);
		this.parent = parent;
	}

	public boolean getAllowsChildren() {
		return userObject.allowsChildren();
	}

	public TreeNode getChildAt(int i) {
		try {
			DBTreeNode child = (DBTreeNode) userObjectToTreeNodeMap.get(userObject.getChild(i));
			if (child == null) {
				// doesn't exist yet; create it
				child = new DBTreeNode(userObject.getChild(i));
			}
			return child;
		} catch (ArchitectException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int getChildCount() {
		try {
			int count = userObject.getChildren().size();
			System.out.println("[33mChild count of "+userObject.getShortDisplayName()+" is "+count+"[0m");
			return count;
		} catch (ArchitectException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/**
	 * @throws ClassCastException if node is not of type <code>DBTreeNode</code>.
	 */
	public int getIndex(TreeNode node) {
		try {
			return userObject.getChildren().indexOf(((DBTreeNode) node).userObject);
		} catch (ArchitectException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return (!userObject.allowsChildren());
	}

	public String toString() {
		return userObject.getShortDisplayName();
	}

	/**
	 * Gets the children list from the userObject.  Doing this here is
	 * the key to the "lazy loading" feature of the tree.
	 */
	public Enumeration children() {
		return new TreeNodeEnumeration(userObject);
	}

	/**
	 * Enumerates the DBTreeNode objects associated with the given
	 * SQLObjects by using the userObjectToTreeNodeMap.
	 */
	public class TreeNodeEnumeration implements Enumeration {
		protected Iterator children;

		public TreeNodeEnumeration(SQLObject parent) {
			try {
				children = parent.getChildren().iterator();
			} catch (ArchitectException e) {
				e.printStackTrace();
				children = Collections.EMPTY_LIST.iterator();
			}
		}

		public boolean hasMoreElements() {
			return children.hasNext();
		}

		/**
		 * Returns the DBTreeNode associated with the next SQLObject
		 * child of the parent given in the constructor.
		 */
		public Object nextElement() {
			Object next = userObjectToTreeNodeMap.get(children.next());
			if (next == null) throw new NullPointerException("Woops, null tree node");
			return next;
		}
	}

	/**
	 * Just returns the user object.
	 */
	public SQLObject getSQLObject() {
		return userObject;
	}

	/**
	 * Satisfies the MutableTreeNode interface.  You will want to use
	 * getSQLObject instead.
	 */
	public Object getUserObject() {
		return userObject;
	}

	public void insert(MutableTreeNode child, int index) {
		if (child instanceof DBTreeNode) {
			try {
				userObject.addChild(index, ((DBTreeNode) child).getSQLObject());
			} catch (ArchitectException e) {
				logger.error("Couldn't add \""+child.toString()+"\" to tree:", e);
				JOptionPane.showMessageDialog(null, "Failed to add child:\n"+e.getMessage());
			}
			child.setParent(this);
		} else {
			throw new IllegalArgumentException("You can't add a non-DBTreeNode to a DBTreeNode");
		}
	}

	public void remove(int index) {
		SQLObject gone = userObject.removeChild(index);
		userObjectToTreeNodeMap.remove(gone);
	}

	public void remove(MutableTreeNode node) {
		SQLObject gone = ((DBTreeNode) node).userObject;
		userObject.removeChild(gone);
		userObjectToTreeNodeMap.remove(gone);
	}

	/**
	 * Removes this node from its parent if possible.
	 *
	 * @throws UnsupportedOperationException if the parent is immutable.
	 */
	public void removeFromParent() throws UnsupportedOperationException {
		if (parent == null) return;
		if (parent instanceof MutableTreeNode) {
			((MutableTreeNode) parent).remove(this);
		} else {
			throw new UnsupportedOperationException("Can't remove this node from immuable TreeNode parent.");
		}
	}

	/**
	 * the following may be incorrect:
	 * Moves this treeNode and its peer SQLObject to the new given
	 * parent.  In many cases this will not be possible (you can't add
	 * a column directly to a database parent, but you can move a
	 * column from one table to another, etc.).
	 */
	public void setParent(MutableTreeNode newParent) {
		//removeFromParent();
		parent = newParent;
		//newParent.add(this);
	}

	/**
	 * Not supported.
	 *
	 * @throws UnsupportedOperationException if you call it.
	 */
	public void setUserObject(Object newSQLObject) {
		throw new UnsupportedOperationException("Not implemented.");
	}
}
