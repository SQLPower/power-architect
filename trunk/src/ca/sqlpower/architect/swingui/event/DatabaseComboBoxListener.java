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
package ca.sqlpower.architect.swingui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.swingui.ArchitectSwingWorker;
import ca.sqlpower.architect.swingui.Lister;
import ca.sqlpower.architect.swingui.ListerProgressBarUpdater;
import ca.sqlpower.sql.SPDataSource;

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
	
	private List <JComponent> enableDisableList;
	private List <JComponent> disableEnableList;
	private List <JComponent> visableInvisableList;
	private List <JComponent> invisableVisableList;
	
	public DatabaseComboBoxListener(JPanel panel,
									JComboBox databaseComboBox,
									JComboBox catalogComboBox,
									JComboBox schemaComboBox,
									JProgressBar progressBar) {
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
		database = new SQLDatabase((SPDataSource) 
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
			progressBarUpdater.setDisableEnableList(disableEnableList);
			progressBarUpdater.setEnableDisableList(enableDisableList);
			progressBarUpdater.setInvisableVisableList(invisableVisableList);
			progressBarUpdater.setVisableInvisableList(visableInvisableList);
			
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
			}

			if (database.isSchemaContainer()) {

				for (SQLObject o : (List<SQLObject>) database.getChildren()) {
					schemaComboBox.addItem(o);
				}
				schemaComboBox.setEnabled(true);
				logger.info("Schema enabled");
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

	/**
	 * enable JComponents in the List when the process start
	 * then disable them after the process is done.  
	 */
	public void setEnableDisableList(List<JComponent> enableDisableList) {
		this.enableDisableList = enableDisableList;
	}


	/**
	 * set JComponents in the List to visable when the process start
	 * then set them back after the process is done.  
	 */
	public void setVisableInvisableList(List<JComponent> visableInvisableList) {
		this.visableInvisableList = visableInvisableList;
	}

	/**
	 * disable JComponents in the List when the process start
	 * then enable them after the process is done.  
	 */
	public void setDisableEnableList(List<JComponent> disableEnableList) {
		this.disableEnableList = disableEnableList;
	}

	/**
	 * set JComponents in the List to invisable when the process start
	 * then set them back after the process is done.  
	 */
	public void setInvisableVisableList(List<JComponent> invisableVisableList) {
		this.invisableVisableList = invisableVisableList;
	}

}

 

	