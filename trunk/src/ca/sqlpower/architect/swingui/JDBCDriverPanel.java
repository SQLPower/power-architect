/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.Monitorable;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;

public class JDBCDriverPanel extends JPanel implements ArchitectPanel {

	private static class DriverTreeCellRenderer extends DefaultTreeCellRenderer implements TreeCellRenderer {
        
        private Icon jarFileIcon =
            new ImageIcon(ClassLoader.getSystemResource("icons/famfamfam/folder_wrench.png"));
        private Icon driverIcon =
            new ImageIcon(ClassLoader.getSystemResource("icons/famfamfam/wrench.png"));
        private Icon jarFileErrorIcon =
            new ImageIcon(ClassLoader.getSystemResource("icons/famfamfam/folder_error.png"));
        private Icon driverErrorIcon =
            new ImageIcon(ClassLoader.getSystemResource("icons/famfamfam/error.png"));
        
	    @Override
	    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            int level = node.getLevel();
            if (level == 0) {
                // root is invisible in the driver tree
            } else if (level == 1) {
                setIcon(jarFileIcon);
                for (int i = 0; i < node.getChildCount(); i++){
                    if(((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject() instanceof Throwable){
                        setIcon(jarFileErrorIcon);
                        break;
                    }
                }
            } else if (level == 2) {
                if (node.getUserObject() instanceof Throwable) {
                    setForeground(Color.RED);
                    setIcon(driverErrorIcon);
                } else {
                    setIcon(driverIcon);
                }
            } else {
                throw new IllegalStateException("This renderer doesn't know how to handle node depth "+level);
            }
            return this;
	    }
    }

	private static final Logger logger = Logger.getLogger(JDBCDriverPanel.class);

	/**
	 * The current data source type (whose JDBC driver search path we're editting).
     * This value will be null when there is no "current" data source type to edit.
	 */
	private SPDataSourceType dataSourceType;

	/**
	 * This view shows the driver JAR files and the JDBC drivers they
	 * contain.
	 */
    private JTree driverTree;

	/**
	 * This tree model holds the registered JAR files under the root,
	 * and lists the JDBC driver classes as children of each JAR file.
	 */
    private DefaultTreeModel dtm;

	/**
	 * The file choosed used by the add action.
	 */
    private JFileChooser fileChooser;

	/**
	 * progress bar stuff
	 */
    private JProgressBar progressBar;
    private JLabel progressLabel;

    private JButton delButton;
    private DefaultMutableTreeNode rootNode;

	public JDBCDriverPanel() {
		fileChooser = new JFileChooser();

		setLayout(new BorderLayout());
		rootNode = new DefaultMutableTreeNode("The Root");
		dtm = new DefaultTreeModel(rootNode);
		driverTree = new JTree(dtm);
		driverTree.setRootVisible(false);
        // Let the user delete multiple driver jars at once
        driverTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		driverTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				delButton.setEnabled(true);
			}
		});
        driverTree.setCellRenderer(new DriverTreeCellRenderer());
        
		add(new JScrollPane(driverTree), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(new JButton(new AddAction()));
		buttonPanel.add(delButton = new JButton(new DelAction()));
		delButton.setEnabled(false);
		add(buttonPanel, BorderLayout.NORTH);

		JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true); //get space for the string
		progressBar.setVisible(false);
		progressPanel.add(progressBar);
		progressLabel = new JLabel("Scanning for JDBC Drivers...");
		progressLabel.setVisible(false);
		progressPanel.add(progressLabel);
        progressPanel.setPreferredSize(new Dimension(300, progressBar.getPreferredSize().height + 20));
		add(progressPanel, BorderLayout.SOUTH);
        
        setPreferredSize(new Dimension(400, 400));
	}

	/**
	 * Copies the pathnames to all the JAR files to
	 * ArchitectSession.addDriverJar().
	 */
	public boolean applyChanges() {
		logger.debug("applyChanges");
        
        List<String> driverList = new ArrayList<String>();
		
		for (int i = 0, n = dtm.getChildCount(dtm.getRoot()); i < n; i++) {
			driverList.add(((DefaultMutableTreeNode) dtm.getChild(dtm.getRoot(), i)).getUserObject().toString());
		}
		return true;
	}

	/**
	 * Does nothing.
	 */
	public void discardChanges() {
        // nothing to discard
	}

    /**
     * Switches to edit the given data source type.
     */
	public void editDsType(SPDataSourceType dst) throws ArchitectException {
        dataSourceType = dst;
	    dtm.setRoot(new DefaultMutableTreeNode());
        if (dst != null) {
            doLoad(dataSourceType.getJdbcJarList());
        }
    }
    
	protected class AddAction extends AbstractAction {
		public AddAction() {
			super("Add JAR...");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				fileChooser.addChoosableFileFilter(ASUtils.JAR_ZIP_FILE_FILTER);
				fileChooser.setMultiSelectionEnabled(true);
				int returnVal = fileChooser.showOpenDialog(JDBCDriverPanel.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					List list = new ArrayList();
					for(int ii=0; ii < files.length;ii++) {
						list.add(files[ii].getAbsolutePath());
					}
                    
                    // always add the files to the data source type. if there are problems,
                    // they will be made visible to the user via the tree UI
					for (int i = 0; i < files.length; i++) {
					    dataSourceType.addJdbcJar(files[i].getAbsolutePath());
					}
                    
					doLoad(list);
				}
			} catch (ArchitectException ex) {
				logger.error("AddAction.actionPerformed() problem.", ex);
			}
		}
	}

	/**
	 * Loads the given List of driver names into the tree, then starts
     * a worker thread that searches for implementations of the JDBC Driver
     * interface in them.  As the worker finds JDBC Drivers in the JARs,
     * it will add them to the tree.
	 */
	private void doLoad(List<String> list) throws ArchitectException {
        logger.debug("about to start a worker", new Exception());
		LoadJDBCDrivers ljd = new LoadJDBCDrivers(list);
		LoadJDBCDriversWorker worker = new LoadJDBCDriversWorker(ljd);
		new ProgressWatcher(progressBar,ljd,progressLabel);
		new Thread(worker).start();
	}

	private class DelAction extends AbstractAction {
		public DelAction() {
			super("Remove JAR");
		}

		public void actionPerformed(ActionEvent e) {
            for (TreePath p : driverTree.getSelectionPaths()) {
                logger.debug(String.format("DelAction: p=%s, pathCount=%d", p, p.getPathCount()));
                if (p != null && p.getPathCount() >= 2) {
                    logger.debug("Removing: " + p.getPathComponent(1));
                    dtm.removeNodeFromParent((MutableTreeNode) p.getPathComponent(1));
                    dataSourceType.removeJdbcJar(p.getPathComponent(1).toString());
                }
            }
			delButton.setEnabled(false);
		}
	}

    private class LoadJDBCDriversWorker implements Runnable {
		LoadJDBCDrivers ljd;
		LoadJDBCDriversWorker (LoadJDBCDrivers ljd) {
			this.ljd = ljd;
		}
		public void run() {
			ljd.execute();
		}
	}

    private class LoadJDBCDrivers implements Monitorable  {

		public boolean hasStarted = false;
		public boolean finished = false;
		private List driverJarList = null;


		private int jarCount = 0; // which member of the JAR file list are we currently processing
		private JarFile jf = null;
		private JDBCScanClassLoader cl = null;

		public LoadJDBCDrivers (List driverJarList) throws ArchitectException {
			this.driverJarList = driverJarList;
			logger.debug("in constructor, setting finished to false...");
			finished = false;
		}

		public Integer getJobSize() throws ArchitectException {
			return new Integer(driverJarList.size() * 1000);
		}

		public int getProgress() throws ArchitectException {
			double fraction = 0.0;
			if (cl != null) {
				fraction = cl.getFraction();
			}
		    int progress = (jarCount - 1) * 1000 + (int) (fraction * 1000.0);
			if (logger.isDebugEnabled()) logger.debug("******************* progress is: " + progress + " of " + getJobSize());
			return progress;
		}

		public boolean isFinished() throws ArchitectException {
			return finished;
		}

		public void setCancelled (boolean cancelled) {
			// job not cancellable, do nothing
		}

		public boolean hasStarted() throws ArchitectException {
			return hasStarted;
		}

		public String getMessage () {
			return null; // no messages returned from this job
		}

		public void execute() {
			hasStarted = true;
			try {
				Iterator it = driverJarList.iterator();
				while (it.hasNext()) {
					// initialize counters
					jarCount++;
					logger.debug("**************** processing file #" + jarCount + " of " + driverJarList.size());
					String path = (String) it.next();
                    File file = SPDataSource.jarSpecToFile(path, getClass().getClassLoader());
					if (file != null) {
                        addJarFile(file);
                    }
				}
				finished = true;
				logger.debug("done loading (normal operation), setting finished to true.");
			} catch ( Exception exp ) {
				logger.error("something went wrong in LoadJDBCDrivers worker thread!",exp);
			} finally {
				finished = true;
				hasStarted = false;
				logger.debug("done loading (error condition), setting finished to true.");
			}
		}

		private void addJarFile(File file) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(file.getPath());
            dtm.insertNodeInto(node, root, root.getChildCount());
			try {
				jf = new JarFile(file);
				cl = new JDBCScanClassLoader(jf);
				List driverClasses = cl.scanForDrivers();
				logger.info("Found drivers: "+driverClasses);
				Iterator it = driverClasses.iterator();
				while (it.hasNext()) {
					DefaultMutableTreeNode child = new DefaultMutableTreeNode(it.next());
					dtm.insertNodeInto(child, node, node.getChildCount());
				}
			} catch (IOException ex) {
				logger.warn("I/O Error reading JAR file",ex);
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(ex);
                dtm.insertNodeInto(child, node, node.getChildCount());
			}
            TreePath path = new TreePath(node.getPath());
            driverTree.expandPath(path);
            driverTree.scrollPathToVisible(path);
		}
	}

	/**
	 * Scans a jar file for instances of java.sql.Driver.
	 */
	private class JDBCScanClassLoader extends ClassLoader {

		JarFile jf;
		List drivers;
		int count = 0;


		/**
		 * Creates a class loader that uses this class's class loader
		 * as its parent.
		 */
		public JDBCScanClassLoader(JarFile jf) {
			super();
			this.jf = jf;
		}

		public synchronized double getFraction() {
			double retval = 0.0;
			if (jf != null) {
				retval = (double)count/(double)jf.size();
			}
			return retval;
		}

		/**
		 * Returns a list of Strings naming the subclasses of
		 * java.sql.Driver which exist in this class loader's jar
		 * file.
		 */
		public List scanForDrivers() {
			drivers = new LinkedList();
			logger.debug("********* " + jf.getName() + " has " + jf.size() + " files.");
			for (Enumeration entries = jf.entries(); entries.hasMoreElements(); ) {
				count++;
				ZipEntry ent = (ZipEntry) entries.nextElement();
				if (ent.getName().endsWith(".class")) {
					try {
						// drop the .class from the name
						String [] s = ent.getName().split("\\.");
						// look for the class using dots instead of slashes
						findClass(s[0].replace('/','.'));
					} catch (ClassFormatError ex) {
						logger.warn("JAR entry "+ent.getName()+" ends in .class but is not a class", ex);
					} catch (NoClassDefFoundError ex) {
						logger.warn("JAR does not contain dependency needed by: " + ent.getName());
					} catch (Throwable ex) {
						logger.warn("Unexpected exception while scanning JAR file "+jf.getName(), ex);
					}
				}
			}
			//jf.close();
			return drivers;
		}

		/**
		 * Searches this ClassLoader's jar file for the given class.
		 *
		 * @throws ClassNotFoundException if the class can't be
		 * located.
		 */
		protected Class findClass(String name)
			throws ClassNotFoundException {
			//logger.debug("Looking for class "+name);
			try {
				ZipEntry ent = jf.getEntry(name.replace('.', '/')+".class");
				if (ent == null) {
					throw new ClassNotFoundException("No class file "+name+" is in my jar file");
				}
				// can we find out here if it was already loaded???
				Class clazz = findLoadedClass(name);
				if (clazz != null) {
					return clazz;
				}
				// haven't seen this before, so go get it...
				InputStream is = jf.getInputStream(ent);
				return readAndCheckClass(is, (int) ent.getSize(), name);
			} catch (IOException ex) {
				throw new ClassNotFoundException("IO Exception reading class from jar file", ex);
			}
		}

		private Class readAndCheckClass(InputStream is, int size, String expectedName)
			throws IOException, ClassFormatError {
			byte[] buf = new byte[size];
			int offs = 0, n;
            
			while ( (n = is.read(buf, offs, size-offs)) >= 0 && offs < size) {
				offs += n;
			}
            final int total = offs;
			if (total != size) {
				logger.warn("Only read "+total+" bytes of class "
							+expectedName+" from JAR file; exptected "+size);
			}
			Class clazz = defineClass(expectedName, buf, 0, total);
			if (java.sql.Driver.class.isAssignableFrom(clazz)) {
				logger.info("Found jdbc driver "+clazz.getName());
				drivers.add(clazz.getName());
			}
			return clazz;
		}

	}

	public JPanel getPanel() {
		return this;
	}
}
