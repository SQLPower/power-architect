package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
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
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.Monitorable;

public class JDBCDriverPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(JDBCDriverPanel.class);

	/**
	 * The current data source type (whose JDBC driver search path we're editting).
     * This value will be null when there is no "current" data source type to edit.
	 */
	private ArchitectDataSourceType dataSourceType;

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
    private javax.swing.Timer timer;
    private boolean doneLoadingJDBC;
    private JLabel progressLabel;

    private JButton addButton;
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
		add(new JScrollPane(driverTree), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(addButton = new JButton(new AddAction()));
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
		add(progressPanel, BorderLayout.SOUTH);
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
	public void editDsType(ArchitectDataSourceType dst) throws ArchitectException {
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
					doLoad(list);
					// If they loaded without any Exceptions, add to list maintained by ArchitectSession
					for (int i = 0; i < files.length; i++) {
						dataSourceType.addJdbcJar(files[i].getAbsolutePath());
					}
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
                    File file = ArchitectUtils.jarSpecToFile(path, getClass().getClassLoader());
					addJarFile(file);
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
			try {
				jf = new JarFile(file);
				cl = new JDBCScanClassLoader(jf);
				List driverClasses = cl.scanForDrivers();
				logger.info("Found drivers: "+driverClasses);
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
				final Exception fex = ex;
				final File ffile = file;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(JDBCDriverPanel.this, "Could not read JAR file \""
													  +ffile.getPath()+"\"\n"+fex.getMessage());
					}
				});
			}
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
			int start = 0, n;
			while ( (n = is.read(buf, start, size-start)) > 0) {
				start += n;
			}
			if ( (start + n) != size ) {
				logger.warn("Only read "+(start+n)+" bytes of class "
							+expectedName+" from JAR file; exptected "+size);
			}
			Class clazz = defineClass(expectedName, buf, 0, start + n);
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
