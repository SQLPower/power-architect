package ca.sqlpower.architect.swingui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;

public class DatabaseComboBoxListener 
					extends ArchitectSwingWorker 
					implements ActionListener, Lister {

	private static final Logger logger = Logger.getLogger(DatabaseComboBoxListener.class);
	protected SQLDatabase.PopulateProgressMonitor progressMonitor;
	private SQLDatabase database;
	
	
	private JComboBox databaseComboBox;
	private JComboBox catalogComboBox;
	private JComboBox schemaComboBox;
	
	private JProgressBar progressBar;
	private JPanel panel;
	
	
	public DatabaseComboBoxListener(JPanel panel,
									JComboBox databaseComboBox,
									JComboBox catalogComboBox,
									JComboBox schemaComboBox,
									JProgressBar progressBar ) {
		super();

		this.panel = panel;
		this.databaseComboBox = databaseComboBox;
		this.catalogComboBox = catalogComboBox;
		this.schemaComboBox = schemaComboBox;
		this.progressBar = progressBar;
		
	}

	public void actionPerformed(ActionEvent e) {

		catalogComboBox.setEnabled(false);
		schemaComboBox.setEnabled(false);
		catalogComboBox.removeAllItems();
		schemaComboBox.removeAllItems();

		if (databaseComboBox.getSelectedItem() == null) {
			return;
		}
		database = new SQLDatabase((ArchitectDataSource) 
				(databaseComboBox.getSelectedItem()));

		try {
			progressMonitor = database.getProgressMonitor();
		} catch (ArchitectException e1) {
			logger.debug("Error getting progressMonitor", e1);
		}
		new Thread(this).start();
	}

	/**
	 * Populates the database <tt>db</tt> which got set up in
	 * actionPerformed().
	 */
	@Override
	public void doStuff() throws Exception {

		try {
			ListerProgressBarUpdater progressBarUpdater = 
				new ListerProgressBarUpdater(progressBar, this);
			new javax.swing.Timer(100, progressBarUpdater).start();

			database.populate();

		} catch (ArchitectException e) {
			logger.debug(
					"Unexpected architect exception in ConnectionListener", e);
		}
	}

	/**
	 * Does GUI cleanup work on the Swing EDT once the worker is done.
	 * 
	 * <p>
	 * This work involves:
	 * <ul>
	 * <li>Check which child type the database has
	 * <li>Populate the catalog and schema boxes accordingly
	 * <li>Enable or disable the catalog and schema boxes accordingly
	 * </ul>
	 */
	@Override
	public void cleanup() throws ArchitectException {
		setCleanupExceptionMessage("Could not populate catalog dropdown!");

		catalogComboBox.removeAllItems();
		catalogComboBox.setEnabled(false);
		schemaComboBox.removeAllItems();
		schemaComboBox.setEnabled(false);
	
		try {
			if (database.isCatalogContainer()) {

				for (SQLObject o : (List<SQLObject>) database.getChildren()) {
					catalogComboBox.addItem(o);
				}
				catalogComboBox.setEnabled(true);
				System.out.println ("Catalog enabled");
			}

			if (database.isSchemaContainer()) {

				for (SQLObject o : (List<SQLObject>) database.getChildren()) {
					schemaComboBox.addItem(o);
				}
				schemaComboBox.setEnabled(true);
				System.out.println ("Schema enabled");
			}

		} catch (ArchitectException ex) {
			JOptionPane.showMessageDialog(panel,
					"Database Connection Erorr", "Error",
					JOptionPane.ERROR_MESSAGE);
			database = null;
		} finally {
		}

	}
	
	public Integer getJobSize() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.getJobSize();
		}
		return null;
	}

	public int getProgress() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.getProgress();
		}
		return 0;
	}

	public boolean isFinished() throws ArchitectException {
		if (progressMonitor != null) {
			return progressMonitor.isFinished();
		}
		return true;
	}

	public SQLDatabase getDatabase() {
		return database;
	}


}

 

	