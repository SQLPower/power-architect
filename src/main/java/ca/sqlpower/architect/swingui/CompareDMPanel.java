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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.diff.CompareSQL;
import ca.sqlpower.architect.swingui.CompareDMPanel.SourceOrTargetStuff.CatalogPopulator;
import ca.sqlpower.architect.swingui.CompareDMPanel.SourceOrTargetStuff.SchemaPopulator;
import ca.sqlpower.architect.swingui.CompareDMSettings.DatastoreType;
import ca.sqlpower.architect.swingui.CompareDMSettings.SourceOrTargetSettings;
import ca.sqlpower.diff.DiffChunk;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.ConnectionComboBoxModel;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The user interface for setting up the comparison between two databases,
 * whether they are housed in Architect project files, SQL databases, or
 * just the current project in the session.
 * <p>
 * This class should not actually extend JPanel.. it should have a JPanel
 * member instead.
 */
public class CompareDMPanel extends JPanel {

	/**
	 * This listener helps with restoring the selected catalog and schema from the user
	 * settings.
	 */
	private static class RestoreSettingsListener implements ListDataListener {

		private JComboBox box;
		private String selectItemName;

		public RestoreSettingsListener(JComboBox box, String selectItemName) {
			this.box = box;
			this.selectItemName = selectItemName;
		}

		public void intervalAdded(ListDataEvent e) {
			tryToSelectTheItem(e.getIndex0(), e.getIndex1());
		}

		public void intervalRemoved(ListDataEvent e) {
			// don't care
		}

		public void contentsChanged(ListDataEvent e) {
			tryToSelectTheItem(e.getIndex0(), e.getIndex1());
		}

		/**
		 * Searches the combo box list data from index low to high (inclusive) and selects
		 * the first item it finds whose name matches selectItemName.  If a match
		 * if found and selected, this listener is also removed from the list data
		 * listener list (because it's no longer needed).
		 *
		 * @param low The index to start the search at
		 * @param high One past the index to end the search at
		 */
		private void tryToSelectTheItem(int low, int high) {
			if (logger.isDebugEnabled()) {
				logger.debug("Looking for '"+selectItemName+"' from index "+low+" to "+high); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			for (int i = low; i <= high; i++) {
				SQLObject o = (SQLObject) box.getItemAt(i);
				if (o != null && o.getName().equals(selectItemName)) {
					box.setSelectedIndex(i);
					box.getModel().removeListDataListener(this);
					return;
				}
			}
		}
	}

	private static final Logger logger = Logger.getLogger(CompareDMPanel.class);

	private static final String OUTPUT_ENGLISH = "OUTPUT_ENGLISH"; //$NON-NLS-1$

	private static final String OUTPUT_SQL = "OUTPUT_SQL"; //$NON-NLS-1$

	private static final String OUTPUT_LIQUIBASE = "OUTPUT_LIQUIBASE"; //$NON-NLS-1$

	public static final String DBCS_DIALOG_TITLE = Messages.getString("CompareDMPanel.dbcsDialogTitle"); //$NON-NLS-1$

	private JProgressBar progressBar;

	private JPanel buttonPanel;

	private LiquibaseOptionsPanel lbOptions;


    /**
     * The list of all DDL Generators available.  The items stored in this
     * combo box are of type <tt>Class&lt;? extends DDLGenerator&gt;</tt>.
     */
	private JComboBox sqlTypeDropdown;

	private JRadioButton sqlButton;

	private JRadioButton englishButton;

	private JRadioButton liquibaseButton;
    
    private JCheckBox showNoChanges;

	private JCheckBox includeIndexes;

	private JLabel statusLabel;

	/**
	 * The status component that explains why the start compare action is disabled.
	 * <p>
	 * <b>Very important note</b> that you should heed carefully: CompareDMPanel
	 * uses the SQL Power validation API in a non-standard way because it has been
	 * retrofit over an old ad-hoc approach to validation. Do not emulate this approach
	 * to validation in new code!
	 */
	private StatusComponent statusComponent = new StatusComponent();
	
	private StartCompareAction startCompareAction;
	
	private SwapSourceTargetAction swapSourceTargetAction;

	private SourceOrTargetStuff source = new SourceOrTargetStuff();
	
	private SourceOrTargetStuff target = new SourceOrTargetStuff();
	
    /**
     * Since we can create new DB connections from this panel, we need a reference
     * to the session so we can retrieve the datasource collection.
     */
    private ArchitectSwingSession session;
    
    /**
     * The dialog that created and contains this panel
     */
    private JDialog parentDialog;
    
	/**
	 * Contains all of the properties and GUI components that relate to the
	 * source or target system. The idea is, the panel will have two instances
	 * of this class: One for the "source" system, and the other for the
	 * "target" system.
	 *
	 * <p>
	 * Note: this class is not private because the test needs to refer to it. :(
	 */
	public class SourceOrTargetStuff {

		private JComboBox databaseDropdown;

		private JComboBox catalogDropdown;

		private JComboBox schemaDropdown;

		private JButton newConnButton;

		private JButton loadFileButton;

		private JTextField loadFilePath;

		/** The group for the source/target type (playpen, file, or database) */
		private ButtonGroup buttonGroup = new ButtonGroup();

		/**
		 * The label for the playpen radio button. We save a reference to it because
		 * it needs to be updated when the project's filename changes.
		 */
		private final JLabel playPenName = new JLabel();
		
		private JRadioButton playPenRadio;

		private JRadioButton physicalRadio;

		private JRadioButton loadRadio;

		private JDialog newConnectionDialog;

		private JLabel catalogLabel;
		private JLabel schemaLabel;
		
		private SchemaPopulator schemaPop;
		private CatalogPopulator catalogPop;
		
		private boolean isSource;

		/**
		 * The last database returned by getDatabase(). Never access this
		 * directly; always use getDatabase().
		 */
		private SQLDatabase cachedDatabase;

		private Action newConnectionAction = new AbstractAction(Messages.getString("CompareDMPanel.newConnectionActionName")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {

                final DataSourceCollection<JDBCDataSource> plDotIni = session.getDataSources();
                final JDBCDataSource dataSource = new JDBCDataSource(plDotIni);
                Runnable onAccept = new Runnable() {
                    public void run() {
                        plDotIni.addDataSource(dataSource);
                        databaseDropdown.setSelectedItem(dataSource);
                    }
                };
                ASUtils.showDbcsDialog(SPSUtils.getWindowInHierarchy(CompareDMPanel.this), dataSource, onAccept);
			}
		};

		private Action chooseFileAction = new AbstractAction(Messages.getString("CompareDMPanel.chooseFileActionName")) { //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(session.getRecentMenu().getMostRecentFile());
				chooser.addChoosableFileFilter(SPSUtils.ARCHITECT_FILE_FILTER);
				int returnVal = chooser.showOpenDialog(CompareDMPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					loadFilePath.setText(file.getPath());
				}
			}
		};

		/**
		 * Finds all the children of a database and puts them in the GUI.
		 */
		public class CatalogPopulator extends PopulateProgressMonitorableWorker implements
				ActionListener {
		    
		    String catalogSelect;
		    String schemaSelect;
		    
		    private SQLDatabase db;
		    
            public CatalogPopulator(ArchitectSwingSession session) {
                super(session);
                catalogSelect = null;
                schemaSelect = null;
            }
            
            /**
             * If select is a valid catalog of the database this sets a 
             * string that will be used as a default item to select 
             * for the catalog next time the list will be redrawn, (this 
             * will happen only once).
             * 
             *  Else does nothing
             *  
             * @param def The name of the item to select
             */
            public void setDefaultCatalog(String def) {
                this.catalogSelect = def;
            }
            
            /**
             * If select is a valid schema of the database and there is 
             * no catalogs for this database this sets a 
             * string that will be used as a default item to select 
             * for the schema next time the list will be redrawn, (this 
             * will happen only once).
             *  
             *  Else does nothing
             *  
             * @param def The name of the item to select
             */
            public void setDefaultSchema(String def) {
                this.schemaSelect = def;
            }
            
			/**
			 * Checks the datasource selected in the databaseDropdown, and
			 * starts a worker thread to read its contents if it exists.
			 *
			 * <p>
			 * Otherwise, clears out the catalog and schema dropdowns and does
			 * not start a worker thread.
			 */
			public void actionPerformed(ActionEvent e) {
				startCompareAction.setEnabled(false);
				db = getDatabase();
				if (db != null) {
					// disable start button (listers will reenable it when
					// finished)
					if (((JComboBox) (e.getSource())).getSelectedIndex() == 0) {
						startCompareAction.setEnabled(false);
					}
					new Thread(this).start();

				} else {
					catalogDropdown.removeAllItems();
					catalogDropdown.setEnabled(false);

					schemaDropdown.removeAllItems();
					schemaDropdown.setEnabled(false);
				}
			}

			/**
			 * Populates the database <tt>db</tt> which got set up in
			 * actionPerformed().
			 */
			@Override
			public void doStuff() throws Exception {

				try {
					ProgressWatcher.watchProgress(progressBar, this);

					db.populate();

				} catch (SQLObjectException e) {
					logger.debug(
						"Unexpected architect exception in ConnectionListener",	e); //$NON-NLS-1$
                    ASUtils.showExceptionDialogNoReport(CompareDMPanel.this,
                            Messages.getString("CompareDMPanel.unexpectedExceptionInConnectionListener"), e); //$NON-NLS-1$
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
			public void cleanup() throws SQLObjectException {
			    setCleanupExceptionMessage(Messages.getString("CompareDMPanel.couldNotPopulateCatalogDropdown")); //$NON-NLS-1$

				catalogDropdown.removeAllItems();
				catalogDropdown.setEnabled(false);
				catalogLabel.setText(""); //$NON-NLS-1$
				schemaLabel.setText(""); //$NON-NLS-1$

				// This is either a database, a catalog, or null depending on
				// how db is structured
				SQLObject schemaParent;

				if (db.isCatalogContainer()) {
					for (SQLCatalog item : db.getChildren(SQLCatalog.class)) {
						// Note: if you change the way this works, also update the RestoreSettingsListener
						catalogDropdown.addItem(item);
						// did you read the note?
					}

					// check if we need to do schemas
					SQLCatalog cat = (SQLCatalog) catalogDropdown
							.getSelectedItem();
					if ( cat != null && cat.getNativeTerm() !=null )
						catalogLabel.setText(cat.getNativeTerm());
					schemaParent = null;
					if (cat == null) {
						// there are no catalogs (database is completely empty)
						catalogDropdown.setEnabled(false);
					} else {
						// there are catalogs, but they don't contain schemas
						catalogDropdown.setEnabled(true);
					}

				} else if (db.isSchemaContainer()) {
					schemaParent = db;
					catalogDropdown.setEnabled(false);
				} else {
					// database contains tables directly
					schemaParent = null;
					catalogDropdown.setEnabled(false);
				}

				schemaDropdown.removeAllItems();
				schemaDropdown.setEnabled(false);

				if (schemaParent == null) {
					startCompareAction.setEnabled(isStartable());
				} else {
					// need a final reference to this so we can use it in the
					// inner class
					// we only get here if the database is a schema container not
					// a catalog container.

					final SQLObject finalSchemaParent = schemaParent;

					new Thread(new PopulateProgressMonitorableWorker(session) {

						@Override
						public void doStuff() throws Exception {
							ProgressWatcher.watchProgress(progressBar, this);
							// this populates the schema parent (populate is not
							// visible here)
							finalSchemaParent.getChildren();
						}

						/**
						 * Populates the schema dropdown box from the schema
						 * parent that doStuff() populated.
						 *
						 * @throws SQLObjectException
						 */
						@Override
						public void cleanup() throws SQLObjectException {
							setCleanupExceptionMessage(Messages.getString("CompareDMPanel.couldNotPopulateSchemaDropdown")); //$NON-NLS-1$

							for (SQLObject item : finalSchemaParent.getChildren()) {
								schemaDropdown.addItem(item);
							}

							if (schemaDropdown.getItemCount() > 0) {
								schemaDropdown.setEnabled(true);
								if ( ((SQLSchema)(finalSchemaParent.getChild(0))).getNativeTerm() != null )
									schemaLabel.setText(((SQLSchema)
											(finalSchemaParent.getChild(0))).getNativeTerm());
							}

							startCompareAction.setEnabled(isStartable());
							
				             //sets to the default schema, iff catalog is null
			                logger.debug("default schema is: " + schemaSelect); //$NON-NLS-1$
			                if ( CatalogPopulator.this.schemaSelect != null) {      
			                    for (int x = 0; x < schemaDropdown.getItemCount(); x++) {
			                        SQLObject curr = (SQLObject)(schemaDropdown.getItemAt(x));
			                        if (curr != null && curr.getName().equals(schemaSelect)) {
			                            schemaDropdown.setSelectedIndex(x);
			                            break;
			                        }
			                    }
			                }
			                schemaSelect = null;
							
							
						}
					}).start();
				}
				
				//sets to the default catalog
				logger.debug("default catalog selected " + catalogSelect); //$NON-NLS-1$
		        if (catalogSelect != null) {      
		            for (int x = 0; x < catalogDropdown.getItemCount(); x++) {
		                SQLObject curr = (SQLObject)(catalogDropdown.getItemAt(x));
		                if (curr != null && curr.getName().equals(catalogSelect)) {
		                    catalogDropdown.setSelectedIndex(x);
		                    break;
		                }
		            }
		        }
		        catalogSelect = null;
			}

			@Override
            protected boolean isFinishedImpl() {
                if (db != null) {
                    return db.isPopulated();
                }
                return true;
            }
		}

		/**
		 * Finds all the children of a catalog and puts them in the GUI.
		 */
		public class SchemaPopulator extends PopulateProgressMonitorableWorker implements
				ActionListener {
		    
		    String select;
		    boolean populating = false;

			public SchemaPopulator(ArchitectSwingSession session) {
                super(session);
                select = null;
            }

			
            /**
             * If select is a valid schema of the catalog this sets a 
             * string that will be used as a default item to select 
             * for the schema next time the list will be redrawn, (this 
             * will happen only once).
             *  
             *  Else does nothing
             *  
             * @param select The name of the item to select
             */
            public void setDefaultSelect(String select) {
                this.select = select;
            }
			
			
            /**
			 * Clears the schema dropdown, and starts a worker thread to
			 * repopulate it (if possible).
			 */
			public void actionPerformed(ActionEvent e) {
				logger.debug("SCHEMA POPULATOR IS ABOUT TO START..."); //$NON-NLS-1$
				schemaDropdown.removeAllItems();
				schemaDropdown.setEnabled(false);

				SQLCatalog catToPopulate = (SQLCatalog) catalogDropdown
						.getSelectedItem();

				if (catToPopulate != null && !populating) {
				    populating = true;
					startCompareAction.setEnabled(false);
					Thread t = new Thread(this);
					t.start();
				}
			}

			@Override
			public void doStuff() throws SQLObjectException {
				logger.debug("SCHEMA POPULATOR IS STARTED..."); //$NON-NLS-1$
				ProgressWatcher.watchProgress(progressBar, this);
				SQLCatalog catToPopulate = (SQLCatalog) catalogDropdown
						.getSelectedItem();
				catToPopulate.populate(); // this might take a while
			}

			/**
			 * Examines the newly-populated catalog and adds its schemas to the
			 * GUI. If the catalog doesn't contain schemas, cleanup just checks
			 * if the comparison action is startable.
			 *
			 * @throws SQLObjectException
			 */
			@Override
			public void cleanup() throws SQLObjectException {
			    logger.debug("SCHEMA POPULATOR IS ABOUT TO CLEAN UP..."); //$NON-NLS-1$
				schemaLabel.setText(""); //$NON-NLS-1$
				SQLCatalog populatedCat = (SQLCatalog) catalogDropdown
						.getSelectedItem();
						
				if (populatedCat.isSchemaContainer()) {
					for (SQLObject item : populatedCat.getChildren()) {
					    schemaDropdown.addItem(item);
					}

					if (schemaDropdown.getItemCount() > 0) {
						schemaDropdown.setEnabled(true);
						if ( ((SQLSchema)(populatedCat.getChild(0))).getNativeTerm() != null )
							schemaLabel.setText(((SQLSchema)(populatedCat.getChild(0))).getNativeTerm());
					}
				}
				startCompareAction.setEnabled(isStartable());
				
				//sets the default schema
                logger.debug("Default Schema: " + select); //$NON-NLS-1$
                if (select != null) {      
                    for (int x = 0; x < schemaDropdown.getItemCount(); x++) {
                        SQLObject curr = (SQLObject)(schemaDropdown.getItemAt(x));
                        if (curr != null && curr.getName().equals(select)) {
                            schemaDropdown.setSelectedIndex(x);
                            break;
                        }
                    }
                }
                select = null;
                populating = false;
			}
			
			
		}
		    
	    // -------------- Small class for monitoring populate progress -----------------
	    // TODO Document this class!!!!
	    private abstract class PopulateProgressMonitorableWorker extends SPSwingWorker {
	        
	        public PopulateProgressMonitorableWorker(ArchitectSwingSession session) {
                super(session);
                setJobSize(null);
                setProgress(0);
                setMessage(Messages.getString("CompareDMPanel.connectingToDatabase")); //$NON-NLS-1$
            }
	        	        
	    }
		
		public synchronized JDialog getNewConnectionDialog() {
			return newConnectionDialog;
		}

		/**
		 * Updates the playpen name label to reflect the session's current name,
		 * which changes when the user saves their project under a different filename.
		 */
		void updatePlayPenNameLabel() {
		    String newPlaypenName = Messages.getString("CompareDMPanel.currentProject", session.getName()); //$NON-NLS-1$
            playPenName.setText(newPlaypenName);  //$NON-NLS-1$
            logger.debug("Updated playpen name to " + newPlaypenName);  //$NON-NLS-1$
		}
		
		/**
		 * Creates the GUI components associated with this object, and appends
		 * them to the given builder.
		 */
		private void buildPartialUI(DefaultFormBuilder builder,	boolean defaultPlayPen, SchemaPopulator schemaPop, CatalogPopulator catalogPop) {
			String prefix;
			if (defaultPlayPen == true) {
				prefix = "source"; //$NON-NLS-1$
			} else {
				prefix = "target"; //$NON-NLS-1$
			}
			
			this.isSource = defaultPlayPen;
			
			this.schemaPop = schemaPop;
			this.catalogPop = catalogPop;
			
			CellConstraints cc = new CellConstraints();
			
			playPenRadio = new JRadioButton();
			playPenRadio.setName(prefix + "PlayPenRadio"); //$NON-NLS-1$
			physicalRadio = new JRadioButton();
			physicalRadio.setName(prefix + "PhysicalRadio"); //$NON-NLS-1$
			loadRadio = new JRadioButton();
			loadRadio.setName(prefix + "LoadRadio"); //$NON-NLS-1$

			buttonGroup.add(playPenRadio);
			buttonGroup.add(physicalRadio);
			buttonGroup.add(loadRadio);

			schemaDropdown = new JComboBox();
			schemaDropdown.setEnabled(false);
			schemaDropdown.setName(prefix + "SchemaDropdown"); //$NON-NLS-1$

			catalogDropdown = new JComboBox();
			catalogDropdown.setEnabled(false);
			catalogDropdown.setName(prefix + "CatalogDropdown"); //$NON-NLS-1$

			databaseDropdown = new JComboBox();
			databaseDropdown.setName(prefix + "DatabaseDropdown"); //$NON-NLS-1$
			databaseDropdown.setModel(new ConnectionComboBoxModel(session.getDataSources()));
			databaseDropdown.setEnabled(false);
			databaseDropdown.setRenderer(dataSourceRenderer);

			newConnButton = new JButton();
			newConnButton.setName(prefix + "NewConnButton"); //$NON-NLS-1$
			newConnButton.setAction(newConnectionAction);
			newConnectionAction.setEnabled(false);

			loadFilePath = new JTextField();
			loadFilePath.setName(prefix + "LoadFilePath"); //$NON-NLS-1$

			loadFilePath.setEnabled(false);
			loadFilePath.getDocument().addDocumentListener(
					new DocumentListener() {
						public void insertUpdate(DocumentEvent e) {
							startCompareAction.setEnabled(isStartable());
						}

						public void removeUpdate(DocumentEvent e) {
							startCompareAction.setEnabled(isStartable());
						}

						public void changedUpdate(DocumentEvent e) {
							startCompareAction.setEnabled(isStartable());
						}
					});
			loadFileButton = new JButton();
			loadFileButton.setName(prefix + "LoadFileButton"); //$NON-NLS-1$
			loadFileButton.setAction(chooseFileAction);
			chooseFileAction.setEnabled(false);

            catalogDropdown.addActionListener(schemaPop);
            databaseDropdown.addActionListener(catalogPop);
            databaseDropdown.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (!isSource) {
                        return;
                    }
                    JDBCDataSource dataSource = (JDBCDataSource) databaseDropdown.getSelectedItem();  
                    if (dataSource != null) {
                        String generatorClass = dataSource.getParentType().getDDLGeneratorClass();
                        if (generatorClass != null) {
                            try {
                                sqlTypeDropdown.setSelectedItem(Class.forName(generatorClass, true, CompareDMPanel.class.getClassLoader()));
                            } catch (ClassNotFoundException ex) {
                                logger.error("Error when finding the DDLGenerator class for the selected database!", ex); //$NON-NLS-1$
                            }
                        }
                    }
                }
                
            });
            
			ActionListener listener = new OptionGroupListener();
			playPenRadio.addActionListener(listener);
			physicalRadio.addActionListener(listener);
			loadRadio.addActionListener(listener);

			if (defaultPlayPen) {
				playPenRadio.doClick();
			} else {
				physicalRadio.doClick();
			}

			updatePlayPenNameLabel();
			
			JLabel temp;
			
			// now give all our shiny new components to the builder
			builder.append(playPenRadio);
			builder.append(playPenName, 7);
			associate(playPenName, playPenRadio);
			builder.nextLine();

			builder.append(""); // takes up blank space //$NON-NLS-1$
			builder.append(physicalRadio);
			temp = builder.append(Messages.getString("CompareDMPanel.physicalDatabaseLabel")); //$NON-NLS-1$
			associate(temp, physicalRadio);
			builder.append(catalogLabel = new JLabel(Messages.getString("CompareDMPanel.catalogLabel"))); //$NON-NLS-1$
			builder.append(schemaLabel = new JLabel(Messages.getString("CompareDMPanel.schemaLabel"))); //$NON-NLS-1$
			builder.appendRow(builder.getLineGapSpec());
			builder.appendRow("pref"); //$NON-NLS-1$
			builder.nextLine(2);
			
			builder.nextColumn(4);
			builder.append(databaseDropdown);
			builder.append(catalogDropdown, schemaDropdown, newConnButton);
			builder.nextLine();

			builder.append(""); //$NON-NLS-1$
			builder.append(loadRadio);
			temp = builder.append(Messages.getString("CompareDMPanel.fromFileLabel")); //$NON-NLS-1$
			associate(temp, loadRadio);
			builder.nextLine();
			
			builder.append(""); // takes up blank space //$NON-NLS-1$
			builder.add(loadFilePath, cc.xyw(5, builder.getRow(), 5));
			builder.nextColumn(8);
			builder.append(loadFileButton);
			builder.nextLine();

		}

		public boolean isModelWithUUID() {
			return playPenRadio.isSelected() || loadRadio.isSelected();
		}

		/**
		 * Figures out which SQLObject holds the tables we want to compare, and
		 * returns it.
		 *
		 * @throws SQLObjectException
		 * @throws IOException
		 * @throws IOException
		 */
		public SQLObject getObjectToCompare() throws SQLObjectException,
				IOException {
			SQLObject o;
			if (playPenRadio.isSelected()) {
				o = session.getTargetDatabase();
			} else if (physicalRadio.isSelected()) {
				if (schemaDropdown.getSelectedItem() != null) {
					o = (SQLObject) schemaDropdown.getSelectedItem();
				} else if (catalogDropdown.getSelectedItem() != null) {
					o = (SQLObject) catalogDropdown.getSelectedItem();
				} else if (databaseDropdown.getSelectedItem() != null) {
					o = getDatabase();
				} else {
					throw new IllegalStateException(
							Messages.getString("CompareDMPanel.noSchemaCatalogOrDatabaseSelected")); //$NON-NLS-1$
				}

			} else if (loadRadio.isSelected()) {
				File f = new File(loadFilePath.getText());
				InputStream in = new BufferedInputStream(new FileInputStream(f));
                
                // XXX: this will take a non-trivial amount of time, so ideally would be done with a progress bar.
                // we might be able to use OpenProjectAction.loadAsynchronously() for this, but it would need a flag for not showing the GUI
                // or better yet, set o=f, and do the load itself in the compare worker, because this approach would share the progress bar with the comparison activity itself
				ArchitectSwingSession newSession = session.getContext().createSession(in);
				
                o = newSession.getTargetDatabase();
                
			} else {
				throw new IllegalStateException(
						Messages.getString("CompareDMPanel.doNotKnowWhichSourceToCompare")); //$NON-NLS-1$
			}

			return o;
		}

		/**
		 * The public isStartable() method uses this to check source and target
		 * readiness.
		 *
		 * XXX: this is really similar to the getObjectToCompare() method,
		 * except that it doesn't try to load the file (so it runs quicker)
		 */
		private ValidateResult getStartabilityStatus() {
		    ValidateResult result;
		    String sourceOrTarget = isSource() ?
		            Messages.getString("CompareDMPanel.older") : //$NON-NLS-1$
		            Messages.getString("CompareDMPanel.newer"); //$NON-NLS-1$
			if (playPenRadio.isSelected()) {
				result = null;
			} else if (physicalRadio.isSelected()) {
				if (databaseDropdown.getSelectedItem() == null) {
				    result = ValidateResult.createValidateResult(
				            Status.FAIL,
				            Messages.getString("CompareDMPanel.incompleteSelection", sourceOrTarget)); //$NON-NLS-1$
				} else {
				    result = null;
				}
			} else if (loadRadio.isSelected()) {
			    if (new File(loadFilePath.getText()).canRead()) {
			        result = null;
			    } else {
			        result = ValidateResult.createValidateResult(
			                Status.FAIL,
			                Messages.getString("CompareDMPanel.projectFileNotReadable", sourceOrTarget)); //$NON-NLS-1$
			    }
			} else {
				throw new IllegalStateException(
						Messages.getString("CompareDMPanel.noRadioButtonsSelected")); //$NON-NLS-1$
			}
			
			return result;
		}

		/**
		 * Returns the currently selected database. Only creates a new
		 * SQLDatabase instance if necessary.
		 */
		public synchronized SQLDatabase getDatabase() {
			JDBCDataSource ds = (JDBCDataSource) databaseDropdown
					.getSelectedItem();
			if (ds == null) {
				cachedDatabase = null;
			} else if (cachedDatabase == null
					|| !cachedDatabase.getDataSource().equals(ds)) {
				cachedDatabase = new SQLDatabase(ds);
			}
			return cachedDatabase;
		}

		/**
		 * This listener is used to enable/disable JComponents when one of the
		 * database choosing options is choosen (for both source and target
		 * selections).
		 */
		public class OptionGroupListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableDisablePhysicalComps();

				boolean enableLoadComps = e.getSource() == loadRadio;
				loadFilePath.setEnabled(enableLoadComps);
				loadFileButton.setEnabled(enableLoadComps);
			}
		}

		/**
		 * For the special case of enabling and disabling the Physical database
		 * Dropdown Components.
		 */
		void enableDisablePhysicalComps() {
			boolean enable = physicalRadio.isSelected();

			databaseDropdown.setEnabled(enable);

			if (enable && catalogDropdown.getItemCount() > 0) {
				catalogDropdown.setEnabled(true);
			} else {
				catalogDropdown.setEnabled(false);
			}

			if (enable && schemaDropdown.getItemCount() > 0) {
				schemaDropdown.setEnabled(true);
			} else {
				schemaDropdown.setEnabled(false);
			}

			newConnectionAction.setEnabled(enable);
		}

        boolean isSource() {
            return isSource;
        }

	}

	/**
	 * Renders list cells which have a value that is an SPDataSource.
	 */
	private ListCellRenderer dataSourceRenderer = new DataSourceRenderer();

	/**
	 * Returns true iff the comparison process can start given the current state
	 * of the GUI form.
	 */
	private boolean isStartable() {
		logger.debug("isStartable is checking..."); //$NON-NLS-1$
		ValidateResult result = null;
		if (sqlButton.isSelected() && sqlTypeDropdown.getSelectedItem() == null) {
		    result = ValidateResult.createValidateResult(Status.FAIL, Messages.getString("CompareDMPanel.chooseSQLDialect")); //$NON-NLS-1$
		}
		
		if (result == null) {
		    result = source.getStartabilityStatus();
		}
		
		if (result == null) {
		    result = target.getStartabilityStatus();
		}

		statusComponent.setResult(result);
		
	    return result == null;
	}

	public Action getStartCompareAction() {
		return startCompareAction;
	}
	
	public Action getSwapSourceTargetAction() {
	    return swapSourceTargetAction;
	}

	public JPanel getButtonPanel() {
		return buttonPanel;
	}

	public CompareDMPanel(ArchitectSwingSession session, JDialog ownerDialog) {
        this.session = session;
        this.parentDialog = ownerDialog;
		buildUI(target.new SchemaPopulator(session),target.new CatalogPopulator(session),
		        source.new SchemaPopulator(session),source.new CatalogPopulator(session));
		startCompareAction.setEnabled(isStartable());
		addAncestorListener(playpenNameRefreshHandler);
	}
	
	

	private void buildUI(SchemaPopulator targetSchemaPop, CatalogPopulator targetCatalogPop, 
	        SchemaPopulator sourceSchemaPop, CatalogPopulator sourceCatalogPop) {

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);

		sqlTypeDropdown = new JComboBox(DDLUtils.getDDLTypes(session.getDataSources()));
        sqlTypeDropdown.setRenderer(new DDLGeneratorListCellRenderer());
		sqlTypeDropdown.setName("sqlTypeDropDown"); //$NON-NLS-1$
		OutputChoiceListener listener = new OutputChoiceListener(sqlTypeDropdown);
        sqlTypeDropdown.setEnabled(false);
        sqlTypeDropdown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startCompareAction.setEnabled(isStartable());
            }
        });
		sqlButton = new JRadioButton();
		sqlButton.setName(OUTPUT_SQL);
		sqlButton.setActionCommand(OUTPUT_SQL);
		sqlButton.setSelected(false);
		sqlButton.addActionListener(listener);

		englishButton = new JRadioButton();
		englishButton.setName("englishButton"); //$NON-NLS-1$
		englishButton.setActionCommand(OUTPUT_ENGLISH);
		englishButton.setSelected(true);
		englishButton.addActionListener(listener);

		liquibaseButton = new JRadioButton();
		liquibaseButton.setName(OUTPUT_LIQUIBASE); //$NON-NLS-1$
		liquibaseButton.setActionCommand(OUTPUT_LIQUIBASE);
		liquibaseButton.setSelected(false);
		liquibaseButton.addActionListener(listener);
		
        showNoChanges = new JCheckBox();
        showNoChanges.setName("showNoChanges"); //$NON-NLS-1$

		includeIndexes = new JCheckBox(Messages.getString("CompareDMPanel.includeIndexes")); //$NON-NLS-1$
		includeIndexes.setName("includeIndexes"); //$NON-NLS-1$
		includeIndexes.setSelected(false);
		
		// Group the radio buttons.
		ButtonGroup outputGroup = new ButtonGroup();
		outputGroup.add(sqlButton);
		outputGroup.add(englishButton);
		outputGroup.add(liquibaseButton);

		startCompareAction = new StartCompareAction();
		startCompareAction.setEnabled(false);
		
		swapSourceTargetAction = new SwapSourceTargetAction();
		swapSourceTargetAction.setEnabled(true);

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		FormLayout formLayout = new FormLayout("20dlu, 2dlu, pref, 4dlu," + // 1-4 //$NON-NLS-1$
				"0:grow, 4dlu, 0:grow, 4dlu," + // 5-8 //$NON-NLS-1$
				"0:grow, 4dlu, pref", // 9-11 //$NON-NLS-1$
				""); //$NON-NLS-1$
		formLayout.setColumnGroups(new int[][] { { 5, 7, 9, } });
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel()
				: new JPanel();
		DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, panel);
		builder.setDefaultDialogBorder();

		CellConstraints cc = new CellConstraints();

		builder.append(statusComponent, 11);
		builder.nextLine();
		
		builder.appendSeparator(Messages.getString("CompareDMPanel.olderSeparator")); //$NON-NLS-1$
		builder.nextLine();
		builder.append(""); // takes up blank space //$NON-NLS-1$

		source.buildPartialUI(builder, true, sourceSchemaPop, sourceCatalogPop);

		builder.appendSeparator(Messages.getString("CompareDMPanel.newerSeparator")); //$NON-NLS-1$
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(2);
		builder.append(""); //$NON-NLS-1$

		target.buildPartialUI(builder, false, targetSchemaPop, targetCatalogPop);

		ActionListener radioButtonActionEnabler = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startCompareAction.setEnabled(isStartable());
			}
		};
		source.playPenRadio.addActionListener(radioButtonActionEnabler);
		source.physicalRadio.addActionListener(radioButtonActionEnabler);
		source.loadRadio.addActionListener(radioButtonActionEnabler);

		target.playPenRadio.addActionListener(radioButtonActionEnabler);
		target.physicalRadio.addActionListener(radioButtonActionEnabler);
		target.loadRadio.addActionListener(radioButtonActionEnabler);

		JLabel temp;
		
		builder.appendSeparator(Messages.getString("CompareDMPanel.outpurFormatSeparator")); //$NON-NLS-1$
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(sqlButton);

		JPanel ddlTypePanel = new JPanel(new BorderLayout(3, 3));
		ddlTypePanel.add(temp = new JLabel(Messages.getString("CompareDMPanel.sqlFor")), BorderLayout.WEST); //$NON-NLS-1$
		associate(temp, sqlButton);
		ddlTypePanel.add(sqlTypeDropdown, BorderLayout.CENTER); // ddl generator
																// type list
        ddlTypePanel.add(temp = new JLabel(Messages.getString("CompareDMPanel.makeOlderLookLikeNewer")), BorderLayout.EAST); //$NON-NLS-1$
        associate(temp, sqlButton);
		builder.append(ddlTypePanel, 3);

		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(liquibaseButton);
		temp = builder.append(Messages.getString("CompareDMPanel.liqubaseScript")); //$NON-NLS-1$
		associate(temp, liquibaseButton);

		this.lbOptions = new LiquibaseOptionsPanel();

		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(1);
		builder.nextColumn(2);
		lbOptions.getPanel().setVisible(false);
		builder.append(lbOptions.getPanel(), 9);
		
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(englishButton);
		temp = builder.append(Messages.getString("CompareDMPanel.englishDescriptions")); //$NON-NLS-1$
		associate(temp, englishButton);
		
        builder.appendRow(builder.getLineGapSpec());
        builder.appendRow("pref"); //$NON-NLS-1$
        builder.nextLine(2);
        builder.nextColumn(2);
		builder.append(showNoChanges);
        temp = builder.append(Messages.getString("CompareDMPanel.suppressSimilarities")); //$NON-NLS-1$
        associate(temp, showNoChanges);
		builder.append(includeIndexes);
        builder.nextLine();

		builder.appendSeparator(Messages.getString("CompareDMPanel.status")); //$NON-NLS-1$
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref"); //$NON-NLS-1$
		builder.nextLine(2);
		statusLabel = new JLabel(""); //$NON-NLS-1$
		builder.add(statusLabel, cc.xy(5, builder.getRow()));
		builder.add(progressBar, cc.xyw(7, builder.getRow(), 5));

		setLayout(new BorderLayout());
		add(builder.getPanel());
		setPreferredSize(new Dimension(800,600));
		try {
			restoreSettingsFromProject();
		} catch (SQLObjectException e) {
			logger.warn("Failed to save user CompareDM preferences!", e); //$NON-NLS-1$
		}
	}

	/**
	 * Updates the playpen name in the source and target sections whenever this
	 * panel is shown.
	 */
	private AncestorListener playpenNameRefreshHandler = new AncestorListener() {

        public void ancestorAdded(AncestorEvent event) {
            source.updatePlayPenNameLabel();
            target.updatePlayPenNameLabel();
        }

        public void ancestorMoved(AncestorEvent event) { /* don't care */ }
        public void ancestorRemoved(AncestorEvent event) { /* don't care */ }
	};
	
	/**
	 * Handles disabling and enabling the "DDL Type" dropdown box and 
     * the no-change suppression checkbox.
	 */
	public class OutputChoiceListener implements ActionListener {

		JComboBox cb;

		public OutputChoiceListener(JComboBox cb) {
			this.cb = cb;
		}

		public void actionPerformed(ActionEvent e) {
			boolean wasVisible = lbOptions.getPanel().isVisible();
			if (e.getActionCommand().equals(OUTPUT_SQL)) {
				cb.setEnabled(true);
                showNoChanges.setEnabled(false);
				lbOptions.getPanel().setVisible(false);
			} else if (e.getActionCommand().equals(OUTPUT_LIQUIBASE)) {
				cb.setEnabled(false);
                showNoChanges.setEnabled(false);
				lbOptions.getPanel().setVisible(true);
			} else {
				cb.setEnabled(false);
				lbOptions.getPanel().setVisible(false);
                showNoChanges.setEnabled(true);
			}
			startCompareAction.setEnabled(isStartable());
			if (wasVisible != lbOptions.getPanel().isVisible()) {
				if (CompareDMPanel.this.isVisible())  {
					CompareDMPanel.this.doLayout();
				}
			}
		}

	}

	public class StartCompareAction extends AbstractAction   {

		private Collection<SQLTable> sourceTables;

		private Collection<SQLTable> targetTables;

        private SPSwingWorker compareWorker;

		public SPSwingWorker getCompareWorker() {
            return compareWorker;
        }

        public StartCompareAction() {
			super(Messages.getString("CompareDMPanel.startCompareActionName")); //$NON-NLS-1$
		}

		public void actionPerformed(ActionEvent e) {
			startCompareAction.setEnabled(false);
			sqlButton.setEnabled(false);
			englishButton.setEnabled(false);
			liquibaseButton.setEnabled(false);
			if (sqlButton.isSelected()) {
			    sqlTypeDropdown.setEnabled(false);
			} else {
			    showNoChanges.setEnabled(false);
			}

			copySettingsToProject();

			// XXX: should do most or all of this work in a worker thread

			final CompareSQL sourceComp;
			final CompareSQL targetComp;
			final SQLObject left;
			final SQLObject right;
			try {
				left = source.getObjectToCompare();
				if (left.getAllowedChildTypes().contains(SQLTable.class)) {
					sourceTables = left.getChildren(SQLTable.class);
				} else {
					sourceTables = new ArrayList<SQLTable>();
				}

				right = target.getObjectToCompare();
				if (right.getAllowedChildTypes().contains(SQLTable.class)) {
					targetTables = right.getChildren(SQLTable.class);
				} else {
					targetTables = new ArrayList<SQLTable>();
				}

				boolean useUUID = source.isModelWithUUID() && target.isModelWithUUID();
				sourceComp = new CompareSQL(sourceTables, targetTables, 
				        session.getCompareDMSettings().getSuppressSimilarities(),
						useUUID);
                sourceComp.setCompareIndices(includeIndexes.isSelected());
				targetComp = new CompareSQL(targetTables, sourceTables, 
				        session.getCompareDMSettings().getSuppressSimilarities(),
						useUUID);
                targetComp.setCompareIndices(includeIndexes.isSelected());

			} catch (SQLObjectException ex) {
			    reenableGUIComponents();
			    ASUtils.showExceptionDialog(session,
			            Messages.getString("CompareDMPanel.couldNotBeginDiffProcess"), ex); //$NON-NLS-1$
			    return;
			} catch (IOException ex) {
			    reenableGUIComponents();
			    ASUtils.showExceptionDialogNoReport(CompareDMPanel.this, Messages.getString("CompareDMPanel.couldNotReadFile"), ex); //$NON-NLS-1$
				logger.error("Could not read file", ex); //$NON-NLS-1$
				return;
			} catch (RuntimeException ex) {
			    reenableGUIComponents();
			    throw new RuntimeException(ex);
			}
			
			compareWorker = new SPSwingWorker(session) {

				private List<DiffChunk<SQLObject>> diff;
				private List<DiffChunk<SQLObject>> diff1;

				private String message;
				
				/**
			     * Checks if this engine has been cancelled by another thread.  If so,
			     * throws a CancellationException.
			     *
			     * @throws CancellationException if this engine has been cancelled
			     */
			    protected void checkCancelled() {
			        if (isCancelled()) {
			            throw new CancellationException("User-requested abort");
			        }
			    }
				
				public void doStuff() throws SQLObjectException {
	                try {
	                    if (source.physicalRadio.isSelected()) {
	                    message = "Refreshing older database";
	                    logger.debug(message);
	                    source.getDatabase().refresh();
	                    checkCancelled();
	                }
	                if (target.physicalRadio.isSelected()) {
	                    message = "Refreshing newer database";
                        logger.debug(message);
	                    target.getDatabase().refresh();
	                    checkCancelled();
	                }
	                setJobSize(sourceComp.getJobSize() + targetComp.getJobSize());
	                logger.debug("Generating TableDiffs for source");
	                diff = sourceComp.generateTableDiffs(session);
	                checkCancelled();
	                logger.debug("Generating TableDiffs for target");
	                diff1 = targetComp.generateTableDiffs(session);
	                checkCancelled();
					message = "Finished";
					logger.debug("Finished Compare");
	                } catch (CancellationException e) {
	                    reenableGUIComponents();
	                    setFinished(true);
	                } catch (RuntimeException ex) {
	                    reenableGUIComponents();
	                    throw new RuntimeException(ex);
	                }
				}

				public void cleanup() {
				    try {
                        if (getDoStuffException() != null) {
                            Throwable exc = getDoStuffException();
                            logger.error("Error in doStuff()", exc); //$NON-NLS-1$
                            ASUtils.showExceptionDialog(session,
                                    Messages.getString("CompareDMPanel.databaseComparisonFailed"), exc); //$NON-NLS-1$
                            reenableGUIComponents();
                            return;
                        }
                        logger.debug("cleanup starts"); //$NON-NLS-1$
                        CompareDMFormatter dmFormat = new CompareDMFormatter(session, parentDialog, session.getCompareDMSettings());                   
                        checkCancelled();
                        switch (session.getCompareDMSettings().getOutputFormat()) {
                        case SQL:
                        case LIQUIBASE:
                            dmFormat.formatForSQLOutput(diff, diff1, left, right);
                            break;
                        case ENGLISH:
                            dmFormat.formatForEnglishOutput(diff, diff1, left, right);
                            break;
                        default:
                            throw new IllegalStateException("Don't know what type of output to make");                        
                        }
                        logger.debug("cleanup finished"); //$NON-NLS-1$
                    } catch (CancellationException e) {
                        setFinished(true);
                    } finally {
                        reenableGUIComponents();
                    }
				}

				@Override
                protected String getMessageImpl() {
                    if (sourceComp.hasStarted() && !sourceComp.isFinished()) {
                        return sourceComp.getMessage();
                    } else if (targetComp.hasStarted() && !targetComp.isFinished()) {
                        return targetComp.getMessage();
                    } else {
                        return message;
                    }
                }

                @Override
                protected int getProgressImpl() {
                    return sourceComp.getProgress() + targetComp.getProgress();
                }

			};

			ProgressWatcher pw = new ProgressWatcher(progressBar, compareWorker, statusLabel);
			pw.setHideLabelWhenFinished(true);
			pw.start();
			new Thread(compareWorker).start();
		}
		
		private void reenableGUIComponents() {
		    sqlButton.setEnabled(true);
            englishButton.setEnabled(true);
			liquibaseButton.setEnabled(true);
            if (sqlButton.isSelected()) {
                sqlTypeDropdown.setEnabled(true);
            } else {
                showNoChanges.setEnabled(true);
            }
            startCompareAction.setEnabled(isStartable());
		}

	}

	public SourceOrTargetStuff getSourceStuff() {
		return source;
	}

	public void copySettingsToProject() {
		CompareDMSettings s = session.getCompareDMSettings();
		s.setSaveFlag(true);
		if (englishButton.isSelected()) {
			s.setOutputFormat(CompareDMSettings.OutputFormat.ENGLISH);
		} else if (sqlButton.isSelected()) {
			s.setOutputFormat(CompareDMSettings.OutputFormat.SQL);
		} else if (liquibaseButton.isSelected()) {
			s.setOutputFormat(CompareDMSettings.OutputFormat.LIQUIBASE);
			s.setLiquibaseSettings(lbOptions.getLiquibaseSettings());
		}
		s.setSuppressSimilarities(showNoChanges.isSelected());
        
        Class<? extends DDLGenerator> selectedGenerator = 
            (Class<? extends DDLGenerator>) sqlTypeDropdown.getSelectedItem();
        s.setDdlGenerator(selectedGenerator);
        
		SourceOrTargetSettings sourceSetting = s.getSourceSettings();
		copySourceOrTargetSettingsToProject(sourceSetting,source);
        s.setSourceStuff(source);

		SourceOrTargetSettings targetSetting = s.getTargetSettings();
		copySourceOrTargetSettingsToProject(targetSetting,target);
        s.setTargetStuff(target);
		
		s.setLiquibaseSettings(lbOptions.getLiquibaseSettings());
	}

	public void copySourceOrTargetSettingsToProject(SourceOrTargetSettings setting,
													SourceOrTargetStuff stuff) {

		if ( stuff.databaseDropdown.getItemCount() > 0 &&
			 stuff.databaseDropdown.getSelectedIndex() >= 0 &&
			 stuff.databaseDropdown.getSelectedItem() != null )
			setting.setConnectName( ((SPDataSource)stuff.databaseDropdown.getSelectedItem()).getName() );
		else
			setting.setConnectName( null );


		if ( stuff.catalogDropdown.getItemCount() > 0 &&
				 stuff.catalogDropdown.getSelectedIndex() >= 0 &&
				 stuff.catalogDropdown.getSelectedItem() != null )
			setting.setCatalogObject( stuff.catalogDropdown.getSelectedItem() );
		else setting.setCatalog(null);

		if ( stuff.schemaDropdown.getItemCount() > 0 &&
				 stuff.schemaDropdown.getSelectedIndex() >= 0 &&
				 stuff.schemaDropdown.getSelectedItem() != null )
			setting.setSchemaObject( stuff.schemaDropdown.getSelectedItem() );
		else
			setting.setSchema(null);

		setting.setFilePath(stuff.loadFilePath.getText());

		if ( stuff.loadRadio.isSelected() )
			setting.setDatastoreType(CompareDMSettings.DatastoreType.FILE);
		if ( stuff.physicalRadio.isSelected() )
			setting.setDatastoreType(CompareDMSettings.DatastoreType.DATABASE);
		if ( stuff.playPenRadio.isSelected() )
			setting.setDatastoreType(CompareDMSettings.DatastoreType.PROJECT);
	}

	private void restoreSettingsFromProject() throws SQLObjectException {
		CompareDMSettings s = session.getCompareDMSettings();

		restoreSourceOrTargetSettingsFromProject(source,s.getSourceSettings());
		restoreSourceOrTargetSettingsFromProject(target,s.getTargetSettings());
		lbOptions.restoreSettings(s.getLiquibaseSettings());

		if ( s.getOutputFormat() == CompareDMSettings.OutputFormat.ENGLISH )
			englishButton.doClick();

		if ( s.getOutputFormat() == CompareDMSettings.OutputFormat.SQL)
			sqlButton.doClick();

		if ( s.getOutputFormat() == CompareDMSettings.OutputFormat.LIQUIBASE)
			liquibaseButton.doClick();

        showNoChanges.setSelected(s.getSuppressSimilarities());

        sqlTypeDropdown.setSelectedItem(s.getDdlGenerator());
	}


	private void restoreSourceOrTargetSettingsFromProject(SourceOrTargetStuff stuff,
			SourceOrTargetSettings set) throws SQLObjectException {

		DatastoreType rbs = set.getDatastoreType();
		if ( rbs == CompareDMSettings.DatastoreType.PROJECT )
			stuff.playPenRadio.doClick();
		else if ( rbs == CompareDMSettings.DatastoreType.DATABASE )
			stuff.physicalRadio.doClick();
		else if ( rbs == CompareDMSettings.DatastoreType.FILE )
			stuff.loadRadio.doClick();

		List<? extends SPDataSource> lds = session.getContext().getConnections();
		for (SPDataSource ds : lds){
			if (ds.getDisplayName().equals(set.getConnectName())){
				stuff.databaseDropdown.setSelectedItem(ds);
				if (set.getCatalog() != null) {
					stuff.catalogDropdown.getModel().addListDataListener(
							new RestoreSettingsListener(stuff.catalogDropdown, set.getCatalog()));
				}
				if (set.getSchema() != null) {
					stuff.schemaDropdown.getModel().addListDataListener(
							new RestoreSettingsListener(stuff.schemaDropdown, set.getSchema()));
				}
				if ( stuff.catalogDropdown.getItemCount() == 0 &&
					 stuff.schemaDropdown.getItemCount() > 0 &&
					 set.getSchema() != null &&
					set.getSchema().length() > 0 ) {
					for ( int j=0; j<stuff.schemaDropdown.getItemCount(); j++ ) {
						SQLObject o2 = (SQLObject)stuff.schemaDropdown.getItemAt(j);
						if ( o2.getName().equals(set.getSchema())) {
							stuff.schemaDropdown.setSelectedIndex(j);
							break;
						}
					}
				}
				break;
			}
		}
		if (set.getFilePath() != null)
			stuff.loadFilePath.setText(set.getFilePath());
	}

	public SourceOrTargetStuff getTargetStuff() {
		return target;
	}
	
	
	/**
	 * Sets the values for the database, schema and catalog in the panel 
	 * for the source set, and set the target to look in the play pen
	 */
	public void compareCurrentWithOrig(SQLSchema schema, SQLCatalog catalog, SQLDatabase db) {
	    
	    
	    //catalog may be null for some dbs (at least in Oracle)
	    if (catalog != null) {
	        source.catalogPop.setDefaultCatalog(catalog.getName());
	    }
	    
	    //schema can be null in a MYSQL Database
	    if (schema != null) {
    	    //this needs to be set because if there is no catalog 
    	    //then the catalog populator is responsible for the schemas
    	    source.catalogPop.setDefaultSchema(schema.getName());
    	    source.schemaPop.setDefaultSelect(schema.getName());
	    }
	    
	    source.physicalRadio.doClick();
	    
        //selects the correct data base, this only looks at 
	    for (int x = 1; x < source.databaseDropdown.getItemCount(); x++) {
            SPDataSource curr = (SPDataSource)(source.databaseDropdown.getItemAt(x));
            if (curr != null && curr.getName().equals(db.getName())) {
                source.databaseDropdown.setSelectedIndex(x);
                break;
            }
        }
        
        target.playPenRadio.doClick();    
	    
	}	
	
	/**
	 *  A simple action to swap the settings for older and newer. 
	 */
	public class SwapSourceTargetAction extends AbstractAction   {
	    public SwapSourceTargetAction() {
	        super(Messages.getString("CompareDMPanel.swapSourceTargetActionName")); //$NON-NLS-1$
	    }

        public void actionPerformed(ActionEvent e) {
            boolean sourcePlayPen = source.playPenRadio.isSelected();
            boolean sourcePhysical = source.physicalRadio.isSelected();
            String sourceLoadFilePath = source.loadFilePath.getText();
            String targetLoadFilePath = target.loadFilePath.getText();
            
            SPDataSource soDBObj = null;
            SQLObject soCatObj  = null; 
            SQLObject soSchemaObj = null;
            
            SPDataSource taDBObj = null; 
            SQLObject taCatObj  = null; 
            SQLObject taSchemaObj = null;
            
            //gets the data from the drop down menus
            //the objects are only loaded as needed because changing an option 
            //in the DB drop down menu if it is disabled will still enable
            //the catalog and or schema dropdowns
            if (source.physicalRadio.isSelected()) {
                soDBObj = (SPDataSource)source.databaseDropdown.getSelectedItem();
                soCatObj  = (SQLObject)source.catalogDropdown.getSelectedItem();
                soSchemaObj = (SQLObject)source.schemaDropdown.getSelectedItem();
            }
            
            if (target.physicalRadio.isSelected()) {
                taDBObj = (SPDataSource)target.databaseDropdown.getSelectedItem();
                taCatObj  = (SQLObject)target.catalogDropdown.getSelectedItem();
                taSchemaObj = (SQLObject)target.schemaDropdown.getSelectedItem();
                
            }
                        
            target.loadFilePath.setText(sourceLoadFilePath);
            source.loadFilePath.setText(targetLoadFilePath);
            
            //select the db connection in the other list
            //then set the defaults for the catalog and schema so they
            //will be updated by the db connection dropdown change event
            if (soDBObj != null) {
                for (int x = 1; x < target.databaseDropdown.getItemCount(); x++) {
                    if (target.databaseDropdown.getItemAt(x).equals(soDBObj)) {
                        target.databaseDropdown.setSelectedIndex(x);
                    }
                }
            }
            if (soCatObj != null) {
                target.catalogPop.setDefaultCatalog(soCatObj.getName());
            }
            if (soSchemaObj != null) {
                target.catalogPop.setDefaultSchema(soSchemaObj.getName());
                target.schemaPop.setDefaultSelect(soSchemaObj.getName());
            }

            if (taDBObj != null) {
                for (int x = 1; x < source.databaseDropdown.getItemCount(); x++) {
                    if (source.databaseDropdown.getItemAt(x).equals(taDBObj)) {
                        source.databaseDropdown.setSelectedIndex(x);
                    }
                }
            }
            if (taCatObj != null) {
                source.catalogPop.setDefaultCatalog(taCatObj.getName());
            }
            if (taSchemaObj != null) {
                source.catalogPop.setDefaultSchema(taSchemaObj.getName());
                source.schemaPop.setDefaultSelect(taSchemaObj.getName());
            }
            
            if (target.playPenRadio.isSelected()) {
                source.playPenRadio.doClick();
            } else if (target.physicalRadio.isSelected()) {
                source.physicalRadio.doClick();
            } else {
                source.loadRadio.doClick();
            }
            
            if (sourcePlayPen) {
                target.playPenRadio.doClick();
            } else if (sourcePhysical) {
                target.physicalRadio.doClick();
            } else {
                target.loadRadio.doClick();
            }
            
        }
	}
	
    /**
     * Associates the given label with the given button, so that mouse
     * events on the label behave as if they were clicks on the button. We
     * need this because of the funny way the radio buttons and checkboxes
     * on this panel are laid out.
     * 
     * @param l
     *            The label that should be clickable like the button.
     * @param b
     *            The button that should receive the label's clicks.
     */
    private void associate(final JLabel l, final AbstractButton b) {
        l.addMouseListener(new MouseListener() {
            private void retarget(MouseEvent e) {
                MouseEvent newEvent = new MouseEvent(
                        b,
                        e.getID(),
                        e.getWhen(),
                        e.getModifiers(),
                        0,
                        0,
                        e.getClickCount(),
                        e.isPopupTrigger(),
                        e.getButton());
                b.dispatchEvent(newEvent);
            }
            public void mouseReleased(MouseEvent e) { retarget(e); }
            public void mousePressed(MouseEvent e)  { retarget(e); }
            public void mouseExited(MouseEvent e)   { retarget(e); }
            public void mouseEntered(MouseEvent e)  { retarget(e); }
            public void mouseClicked(MouseEvent e)  { retarget(e); }
        });
    }

}