package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

public class JDBCDriverPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(JDBCDriverPanel.class);

	/**
	 * The settings we're editting
	 */
	protected UserSettings us;

	/**
	 * This view shows the driver JAR files and the JDBC drivers they
	 * contain.
	 */
	protected JTree driverTree;

	/**
	 * This tree model holds the registered JAR files under the root,
	 * and lists the JDBC driver classes as children of each JAR file.
	 */
	protected DefaultTreeModel dtm;

	/**
	 * The file choosed used by the add action.
	 */
	JFileChooser fileChooser;

	protected JButton addButton;
	protected JButton delButton;

	public JDBCDriverPanel(UserSettings us) {
		this.us = us;
		setup();
		revertToUserSettings();
	}

	public void setup() {
		fileChooser = new JFileChooser();

		setLayout(new BorderLayout());
		dtm = new DefaultTreeModel(new DefaultMutableTreeNode("The Root"));
		driverTree = new JTree(dtm);
		driverTree.setRootVisible(false);
		add(driverTree, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(addButton = new JButton(new AddAction()));
		buttonPanel.add(delButton = new JButton(new DelAction()));
		add(buttonPanel, BorderLayout.SOUTH);
	}

	protected void revertToUserSettings() {
		dtm.setRoot(new DefaultMutableTreeNode());
		
		Iterator it = us.getDriverJarList().iterator();
		while (it.hasNext()) {
			String path = (String) it.next();
			addJarFile(new File(path));
		}
	}

	public void addJarFile(File file) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
		try {
			JarFile jf = new JarFile(file);
			JDBCScanClassLoader cl = new JDBCScanClassLoader(jf);
			List driverClasses = cl.scanForDrivers();
			System.out.println("Found drivers: "+driverClasses);
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getPath());
			dtm.insertNodeInto(node, root, root.getChildCount());
			Iterator it = driverClasses.iterator();
			while (it.hasNext()) {
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(it.next());
				dtm.insertNodeInto(child, node, node.getChildCount());
			}
			TreePath path = new TreePath(node.getPath());
			driverTree.expandPath(path);
			driverTree.scrollPathToVisible(path);
		} catch (IOException ex) {
			logger.warn("I/O Error reading JAR file",ex);
			JOptionPane.showMessageDialog(this, "Could not read JAR file \""
										  +file.getPath()+"\"\n"+ex.getMessage());
		}
	}

	/**
	 * Copies the pathnames to all the JAR files in the tree into a
	 * list and then passes that list to
	 * UserSettings.setDriverJarList().
	 */
	public void applyChanges() {
		ArrayList drivers = new ArrayList(dtm.getChildCount(dtm.getRoot()));
		for (int i = 0, n = dtm.getChildCount(dtm.getRoot()); i < n; i++) {
			drivers.add(dtm.getChild(dtm.getRoot(), i));
		}
		us.setDriverJarList(drivers);
	}

	/**
	 * Does nothing.
	 */
	public void discardChanges() {
	}

	protected class AddAction extends AbstractAction {
		public AddAction() {
			super("Add...");
		}

		public void actionPerformed(ActionEvent e) {
			
			fileChooser.addChoosableFileFilter(ASUtils.JAR_ZIP_FILE_FILTER);
			int returnVal = fileChooser.showOpenDialog(JDBCDriverPanel.this);
			if(returnVal == JFileChooser.APPROVE_OPTION) {
				addJarFile(fileChooser.getSelectedFile());
			}
		}
	}

	protected class DelAction extends AbstractAction {
		public DelAction() {
			super("Remove");
		}

		public void actionPerformed(ActionEvent e) {
			TreePath p = driverTree.getSelectionPath();
			if (p != null && p.getPathCount() >= 2) {
				dtm.removeNodeFromParent((MutableTreeNode) p.getPathComponent(1));
			}
		}
	}

	/**
	 * Scans a jar file for instances of java.sql.Driver.
	 */
	protected class JDBCScanClassLoader extends ClassLoader {

		JarFile jf;

		/**
		 * Creates a class loader that uses this class's class loader
		 * as its parent.
		 */
		public JDBCScanClassLoader(JarFile jf) {
			super();
			this.jf = jf;
		}

		/**
		 * Returns a list of Strings naming the subclasses of
		 * java.sql.Driver which exist in this class loader's jar
		 * file.
		 */
		public List scanForDrivers() {
			List drivers = new LinkedList();
			byte[] buf = new byte[0];
			for (Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
				ZipEntry ent = (ZipEntry) entries.nextElement();
				if (ent.getName().endsWith(".class")) {
					try {
						if (ent.getSize() > buf.length) {
							buf = new byte[(int) ent.getSize()];
						}
						InputStream is = jf.getInputStream(ent);
						int len = is.read(buf);
						Class clazz = defineClass(null, buf, 0, len);
						if (java.sql.Driver.class.isAssignableFrom(clazz)) {
							System.out.println("Found jdbc driver "+clazz.getName());
							drivers.add(clazz.getName());
						}
					} catch (IOException ex) {
						logger.warn("I/O Error reading JAR entry \""+ent.getName()+"\"",ex);
						JOptionPane.showMessageDialog(JDBCDriverPanel.this,
													  "I/O Error reading JAR entry \""
													  +ent.getName()+"\"");
					} catch (ClassFormatError ex) {
						logger.warn("JAR entry "+ent.getName()+" ends in .class but is not a class", ex);
					} catch (Throwable ex) {
						logger.warn("Unexpected exception while scanning JAR file "+jf.getName(), ex);
					}
				}
			}
			return drivers;
		}

		/**
		 * Searches this ClassLoader's jar file for the given class.
		 * Throws ClassNotFoundException if the class can't be
		 * located.
		 */
		protected Class findClass(String name)
			throws ClassNotFoundException {
			System.out.println("Looking for class "+name);
			name = name.replace('.', '/');
			name += ".class";
			try {
				ZipEntry ent = jf.getEntry(name);
				if (ent == null) {
					throw new ClassNotFoundException("No class file "+name+" is in my jar file");
				}
				byte[] buf = new byte[(int) ent.getSize()];
				InputStream is = jf.getInputStream(ent);
				int len = is.read(buf);
				return defineClass(name, buf, 0, len);
			} catch (IOException ex) {
				throw new ClassNotFoundException("IO Exception reading class from jar file", ex);
			}
		}
	}
}
