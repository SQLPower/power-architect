package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.OracleDDLGenerator;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CompareDMPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(CompareDMPanel.class);
	
	private static final String newline = System.getProperty("line.separator");

	public static final String DBCS_DIALOG_TITLE = "New Database Connection";

	/**
	 * Renders list cells which have a value that is an ArchitectDataSource.
	 */
	private ListCellRenderer dataSourceRenderer = new DefaultListCellRenderer() {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			ArchitectDataSource ds = (ArchitectDataSource) value;
			String label;
			if (ds == null) {
				label = "(Choose a Connection)";
			} else {
				label = ds.getName();
			}
			return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
		}
	};

	private JDialog sourceNewConnectionDialog;
	private JDialog targetNewConnectionDialog;
	
	private Action newConnectionAction = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			final boolean isSource = (e.getSource() == sourceNewConnButton ? true : false);
			if (isSource) {
				if (getSourceNewConnectionDialog() != null) {
					getSourceNewConnectionDialog().requestFocus();
					return;
				}
			} else {
				if (getTargetNewConnectionDialog() != null) {
					getTargetNewConnectionDialog().requestFocus();
					return;
				}
			}
			final DBCSPanel dbcsPanel = new DBCSPanel();
			dbcsPanel.setDbcs(new ArchitectDataSource());
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.applyChanges();
					ArchitectDataSource newDS = dbcsPanel.getDbcs();
					if (isSource) {
						sourceDatabaseDropdown.addItem(newDS);
						sourceDatabaseDropdown.setSelectedItem(newDS);
						getSourceNewConnectionDialog().dispose();
						setSourceNewConnectionDialog(null);
					} else {
						targetDatabaseDropdown.addItem(newDS);
						targetDatabaseDropdown.setSelectedItem(newDS);
						getTargetNewConnectionDialog().dispose();
						setTargetNewConnectionDialog(null);
					}
				}
			});
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dbcsPanel.discardChanges();
					if (isSource) {
						getSourceNewConnectionDialog().dispose();
						setSourceNewConnectionDialog(null);
					} else {
						getTargetNewConnectionDialog().dispose();
						setTargetNewConnectionDialog(null);
					}
				}
			});
			
			JDialog d = ASUtils.createOkCancelDialog(
					dbcsPanel,
					SwingUtilities.getWindowAncestor(CompareDMPanel.this), 
					DBCS_DIALOG_TITLE,
					okButton, cancelButton);
			
			if (isSource) {
				setSourceNewConnectionDialog(d);
			} else {
				setTargetNewConnectionDialog(d);
			}
			d.setVisible(true);
		}
	};
	
	// source database fields
	private JComboBox sourceDatabaseDropdown;
	private JComboBox sourceCalatalogDropdown;
	private JComboBox sourceSchemaDropdown;
	private JButton sourceNewConnButton;

	// target database fields
	private JComboBox targetDatabaseDropdown;
	private JComboBox targetCatalogsBox;
	private JComboBox targetSchemasBox;
	private JButton newTargetConnButton;

	private JProgressBar progressBar;
	
	private JPanel buttonPanel;

	private SQLDatabase sourceDatabase;
	private SQLDatabase targetDatabase;
	private SQLSchema sourceSQLSchema;
	private SQLSchema targetSQLSchema;

	private JTextPane outputTextPane;
	private AbstractDocument outputDoc;
	
	private StartCompareAction startCompareAction;

	public Action getStartCompareAction() {
		return startCompareAction;
	}

	public JPanel  getButtonPanel() {
		return buttonPanel;
	}

	public CompareDMPanel() {
		buildUI();
	}
	
	private void buildUI() {
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		SwingUIProject project = af.getProject();

		// layout source database option/combox target combox
		JRadioButton sourcePlayPenRadio = new JRadioButton();
		sourcePlayPenRadio.setName("sourcePlayPenRadio");
		sourcePlayPenRadio.setActionCommand("Project");
		sourcePlayPenRadio.setSelected(true);

		JRadioButton sourcePhysicalRadio = new JRadioButton();
		sourcePhysicalRadio.setName("sourcePhysicalRadio");
		sourcePhysicalRadio.setActionCommand("SQL Connection");

		//Group the radio buttons.
		ButtonGroup sourceButtonGroup = new ButtonGroup();
		sourceButtonGroup.add(sourcePlayPenRadio);
		sourceButtonGroup.add(sourcePhysicalRadio);

		//Register a listener for the radio buttons.
		sourcePlayPenRadio.addActionListener(new SourceOptionListener());
		sourcePhysicalRadio.addActionListener(new SourceOptionListener());

		sourceDatabaseDropdown = new JComboBox();
		sourceDatabaseDropdown.addItem(null);   // the non-selection selection
		for (ArchitectDataSource ds : af.getUserSettings().getConnections()) {
			sourceDatabaseDropdown.addItem(ds);
		}
		sourceDatabaseDropdown.setName("sourceDatabaseDropdown");
		sourceDatabaseDropdown.addActionListener(new ConnectionListener());
		sourceDatabaseDropdown.setEnabled(false);
		sourceDatabaseDropdown.setRenderer(dataSourceRenderer);
		
		sourceNewConnButton = new JButton("New...");
		sourceNewConnButton.setName("sourceNewConnButton");
		sourceNewConnButton.setEnabled(false);
		sourceNewConnButton.addActionListener(newConnectionAction);
		
		sourceCalatalogDropdown = new JComboBox();
		sourceCalatalogDropdown.setName("sourceCalatalogDropdown");
		sourceCalatalogDropdown.setEnabled(false);

		sourceSchemaDropdown = new JComboBox();
		sourceSchemaDropdown.setName("sourceSchemaDropdown");
		sourceSchemaDropdown.setEnabled(false);

		targetDatabaseDropdown = new JComboBox();
		targetDatabaseDropdown.addItem(null);   // the non-selection selection
		for (ArchitectDataSource ds : af.getUserSettings().getConnections()) {
			targetDatabaseDropdown.addItem(ds);
		}
		targetDatabaseDropdown.setName("targetDatabaseDropdown");
		targetDatabaseDropdown.setRenderer(dataSourceRenderer);
		targetDatabaseDropdown.addActionListener(new ConnectionListener());

		newTargetConnButton = new JButton("New...");

		targetCatalogsBox = new JComboBox();
		targetSchemasBox = new JComboBox();

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		// layout compare methods check box and syntax combox
		JRadioButton sourceLikeTargetButton = new JRadioButton("How to make source like target" );
		sourceLikeTargetButton.setActionCommand("source like target");
		sourceLikeTargetButton.setSelected(false);

		JRadioButton targetLikeSourceButton = new JRadioButton("How to make target like source" );
		targetLikeSourceButton.setActionCommand("target like source");
		targetLikeSourceButton.setSelected(false);

		JRadioButton justCompareButton = new JRadioButton("Compare source and target" );
		justCompareButton.setActionCommand("compare");
		justCompareButton.setSelected(true);

		//Group the radio buttons.
		ButtonGroup operationGroup = new ButtonGroup();
		operationGroup.add(sourceLikeTargetButton);
		operationGroup.add(targetLikeSourceButton);
		operationGroup.add(justCompareButton);

		JCheckBox sqlSyntax = new JCheckBox("SQL");
		sqlSyntax.setSelected(false);
		
		// outputDoc outputTextPane

		outputTextPane = new JTextPane();
        outputTextPane.setCaretPosition(0);
        outputTextPane.setMargin(new Insets(5,5,5,5));

        JScrollPane scrollPane = new JScrollPane(outputTextPane);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        
		startCompareAction = new StartCompareAction();
		startCompareAction.setEnabled(false);
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	
		FormLayout formLayout = new FormLayout("20dlu, 2dlu, pref, 4dlu," +
				"pref:grow, 2dlu, pref, 4dlu," +
				"pref:grow, 4dlu, pref:grow",
				"");
		formLayout.setColumnGroups(new int[][] {{5,9,11}});
		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel() : new JPanel();
		DefaultFormBuilder builder = new DefaultFormBuilder(formLayout, panel);
		builder.setDefaultDialogBorder();
		
		CellConstraints cc = new CellConstraints();
		
		builder.appendSeparator("Compare Source");
		builder.nextLine();
		builder.append(""); // takes up blank space
		builder.append(sourcePlayPenRadio);
		builder.append("Project ["+project.getName()+"]");
		builder.nextLine();
		
		builder.append(""); // takes up blank space
		builder.append(sourcePhysicalRadio);
		builder.append("Physical Database");
		builder.nextColumn(2);
		builder.append("Catalog");
		builder.append("Schema");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(4);
		builder.append(sourceDatabaseDropdown);
		builder.append(sourceNewConnButton, sourceCalatalogDropdown, sourceSchemaDropdown);
		
		builder.appendSeparator("With Target");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(4);
		builder.append("Physical Database");
		builder.nextColumn(2);
		builder.append("Catalog");
		builder.append("Schema");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(4);
		builder.append(targetDatabaseDropdown);
		builder.append(newTargetConnButton, targetCatalogsBox, targetSchemasBox);
		
		builder.appendSeparator("Output Format");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(new JRadioButton());
		
		JPanel ddlTypePanel = new JPanel(new BorderLayout(3,3));
		ddlTypePanel.add(new JLabel("SQL for"), BorderLayout.WEST);
		ddlTypePanel.add(new JComboBox(), BorderLayout.CENTER);  // ddl generator type list
		builder.append(ddlTypePanel);
		
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(new JRadioButton());
		builder.append("English descriptions");
		builder.nextLine();
		
		builder.appendSeparator("Comparison Sense");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(new JRadioButton());
		builder.append("How to make Source like Target");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(new JRadioButton());
		builder.append("How to make Target like Source");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(new JRadioButton());
		builder.append("Show all differences");
		builder.nextLine();
		
		builder.appendSeparator("Status");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.add(new JLabel("Twiddling my thumbs"), cc.xy(5, builder.getRow()));
		builder.add(progressBar, cc.xyw(7, builder.getRow(), 5));
		
		setLayout(new BorderLayout());
		add(builder.getPanel());
	}

	public class ConnectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == sourceDatabaseDropdown) {
				ArchitectDataSource ds = (ArchitectDataSource) sourceDatabaseDropdown.getSelectedItem();

				sourceSchemaDropdown.setEnabled(false);
				if (ds != null) {
					try {
						SchemaLister sl = new SchemaLister(new SQLDatabase(ds), true);
						SchemaListerProgressWatcher taskPerformer = new SchemaListerProgressWatcher(progressBar,sl);
						new javax.swing.Timer(100, taskPerformer).start();
						new Thread(sl).start();
					} catch ( ArchitectException exp) {
						logger.error("SchemaListerProgressWatcher failt2", exp);
					}
				}
			}
			else if (e.getSource() == targetDatabaseDropdown ) {
				ArchitectDataSource ds = (ArchitectDataSource) targetDatabaseDropdown.getSelectedItem();
				
				targetSchemasBox.setEnabled(false);
				if (ds != null) {
					try {
						SchemaLister sl = new SchemaLister(new SQLDatabase(ds), false);
						SchemaListerProgressWatcher taskPerformer = new SchemaListerProgressWatcher(progressBar,sl);
						new javax.swing.Timer(100, taskPerformer).start();
						new Thread(sl).start();
					} catch ( ArchitectException exp) {
						logger.error("SchemaListerProgressWatcher failt2", exp);
					}
				}
			}
			else {
				logger.error("Recieved action event from unknown source: "+e);
			}
		}
	}



	public class SourceOptionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Project")) {
				sourceDatabaseDropdown.setEnabled(false);
				sourceNewConnButton.setEnabled(false);
				sourceCalatalogDropdown.setEnabled(false);
				sourceSchemaDropdown.setEnabled(false);
			} else {
				sourceDatabaseDropdown.setEnabled(true);
				sourceNewConnButton.setEnabled(true);
				sourceCalatalogDropdown.setEnabled(true);
				sourceSchemaDropdown.setEnabled(true);
			}
		}
	}

	public class SchemaLister implements Runnable {
		
		private SQLDatabase db;
		private List schema;
		private boolean sourceInd;
		SQLDatabase.PopulateProgressMonitor progressMonitor;
		
		public SchemaLister (SQLDatabase db, boolean sourceInd ) throws ArchitectException {
			this.db = db;
			this.sourceInd = sourceInd;
			progressMonitor = db.getProgressMonitor();
		}
		
		public Integer getJobSize() throws ArchitectException {
			return progressMonitor.getJobSize();
		}
		
		public int getProgress() throws ArchitectException {
			return progressMonitor.getProgress();
		}
		
		public boolean isFinished() throws ArchitectException {
			return progressMonitor.isFinished();
		}
		
		
		public void run() {

			try {

				List dbObject = db.getChildren();
				Iterator it = dbObject.iterator();
				schema = new LinkedList();

				while (it.hasNext()) {
					SQLObject object = (SQLObject) it.next();
					if ( object instanceof SQLSchema ) {
						schema.add(object.getShortDisplayName());
					}
				}
			} catch(  ArchitectException e2 ) {
				logger.error("connection failt2", e2);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					if ( sourceInd ) {
						sourceSchemaDropdown.setEnabled(true);
						sourceSchemaDropdown.setVisible(true);
						sourceSchemaDropdown.removeAllItems();
					
						Iterator it = schema.iterator();
						while (it.hasNext()) {
							sourceSchemaDropdown.addItem((String)it.next());
						}
						sourceDatabase = db;
					}
					else {
						targetSchemasBox.setEnabled(true);
						targetSchemasBox.setVisible(true);
						targetSchemasBox.removeAllItems();
					
						Iterator it = schema.iterator();
						while (it.hasNext()) {
							targetSchemasBox.addItem((String)it.next());
						}
						targetDatabase = db;
					}
					
					startCompareAction.enableIfPossible();
				}
			});	
		}
	}


	public class SchemaListerProgressWatcher implements ActionListener {
		private JProgressBar bar;
		private SchemaLister lister;
		
		public SchemaListerProgressWatcher ( JProgressBar bar, SchemaLister lister ) {
			this.bar = bar;
			this.lister = lister;
		}

		public void actionPerformed(ActionEvent evt) {

			try {
				Integer max = lister.getJobSize();
				bar.setVisible(true);
				if ( max != null ) {
					bar.setMaximum(max.intValue());
					bar.setValue(lister.getProgress());
					bar.setIndeterminate(false);
				}
				else {
					bar.setIndeterminate(true);
				}
				
				if ( lister.isFinished() ) {
					bar.setVisible(false);
					((javax.swing.Timer)evt.getSource()).stop();
				}
			} catch ( ArchitectException e ) {
				logger.error("getProgress failt", e);
			}
		}
	}  		




	public class StartCompareAction extends AbstractAction {
		
		public StartCompareAction() {
			super("Start");
		}

		public void enableIfPossible() {
			if ( sourceDatabase != null && targetDatabase != null ) {
				setEnabled(true);
			}
		}
		
		public void actionPerformed(ActionEvent e) {

			try {
				
				startCompareAction.setEnabled(false);
				
				SimpleAttributeSet attrsMsg = new SimpleAttributeSet();
		        StyleConstants.setFontFamily(attrsMsg, "Courier New");
		        StyleConstants.setFontSize(attrsMsg, 12);
		        StyleConstants.setForeground(attrsMsg, Color.orange);
				        

				
				sourceSQLSchema = sourceDatabase.getSchemaByName((String)sourceSchemaDropdown.getSelectedItem());
				targetSQLSchema = targetDatabase.getSchemaByName((String)targetSchemasBox.getSelectedItem());

				CompareSchemaWorker worker = new CompareSchemaWorker(sourceSQLSchema,targetSQLSchema);
				
				StyledDocument styledDoc = outputTextPane.getStyledDocument();
		        if (styledDoc instanceof AbstractDocument) {
		            outputDoc = (AbstractDocument)styledDoc;
		        } else {
		            System.err.println("Text pane's document isn't an AbstractDocument!");
		            return;
		        }

				outputDoc.insertString(outputDoc.getLength(),
						"Please wait..." + worker.getJobSize() + newline, attrsMsg );
													 				
				CompareProgressWatcher watcher = new CompareProgressWatcher(progressBar,worker);
				new javax.swing.Timer(100, watcher).start();
				new Thread(worker).start();

			} catch ( ArchitectException exp) {
				logger.error("SchemaListerProgressWatcher failt2", exp);
			} catch (BadLocationException ble) {
	            System.err.println("Couldn't insert styled text.");
	        }
		}
	}
	

	
	public class CompareSchemaWorker implements Runnable {
		
		private SQLSchema source;
		private SQLSchema target;
		int	jobSize;
		int	progress;
		boolean finished;
		
		public CompareSchemaWorker (SQLSchema source, SQLSchema target ) throws ArchitectException {
			this.source = source;
			this.target = target;
			jobSize = source.getChildren().size();
			progress = 0;
			finished = false;
		}

		public int getJobSize() throws ArchitectException {
			return jobSize;
		}
		
		public int getProgress() {
			return progress;
		}
		
		public boolean isFinished() throws ArchitectException {
			return finished;
		}
		
		public void run() {
			// FIXME: this should not be hardwired to oracle!
			OracleDDLGenerator od = new OracleDDLGenerator();

			final DefaultStyledDocument output = new DefaultStyledDocument();
            
			
			SimpleAttributeSet attrsSource = new SimpleAttributeSet();
			SimpleAttributeSet attrsTarget = new SimpleAttributeSet();
			SimpleAttributeSet attrsSame = new SimpleAttributeSet();
			SimpleAttributeSet attrsMsg = new SimpleAttributeSet();

	        StyleConstants.setFontFamily(attrsSource, "Courier New");
	        StyleConstants.setFontSize(attrsSource, 12);
	        StyleConstants.setForeground(attrsSource, Color.red);

	        StyleConstants.setFontFamily(attrsTarget, "Courier New");
	        StyleConstants.setFontSize(attrsTarget, 12);
	        StyleConstants.setForeground(attrsTarget, Color.green);

	        StyleConstants.setFontFamily(attrsSame, "Courier New");
	        StyleConstants.setFontSize(attrsSame, 12);
	        StyleConstants.setForeground(attrsSame, Color.black);
	        
	        StyleConstants.setFontFamily(attrsMsg, "Courier New");
	        StyleConstants.setFontSize(attrsMsg, 12);
	        StyleConstants.setForeground(attrsMsg, Color.orange);
	        
			try {

				List sourceTablesList = source.getChildren();
				List targetTablesList = target.getChildren();
				
				Iterator it = sourceTablesList.iterator();
				while (it.hasNext()) {
					SQLObject table = (SQLObject) it.next();
					if ( table instanceof SQLTable ) {

						SQLTable targetTable = targetSQLSchema.getTableByName( table.getName() );
						if ( targetTable == null ) {
							
							output.insertString(output.getLength(),
											"<<<TABLE NOT FOUND IN " + 
											targetDatabase.getDataSource().getDisplayName() +
											"." + targetSchemasBox.getSelectedItem() + ">>>" + newline,
											 attrsMsg );

							output.insertString(output.getLength(),
											table.toString() +
											newline, attrsSource );
						}
						else {

							output.insertString(output.getLength(),
											newline + table.toString() + newline, attrsSame );

							Iterator it2 = (((SQLTable)table).getColumns()).iterator();
							while (it2.hasNext()) {
								SQLObject column = (SQLObject) it2.next();
								if ( column instanceof SQLColumn ) {
									SQLColumn targetColumn = targetTable.getColumnByName( ((SQLColumn)column).getColumnName() );
									if ( targetColumn == null ) {

										output.insertString(output.getLength(),
											" <<<NEW COLUMN>>>" + newline, attrsMsg );
										output.insertString(output.getLength(),
													"      " + ((SQLColumn)column).toString() + newline,
													attrsSource );
									}
									else {
										
										
													
										if ( ((SQLColumn)column).getType() != targetColumn.getType() ) {

											output.insertString(output.getLength(),
												" <<<DIFFERENT DATA TYPE>>>" + newline, attrsMsg );
											output.insertString(output.getLength(),
													"      " + ((SQLColumn)column).getName(), attrsSame );

											output.insertString(output.getLength(),
												"    " +
												ca.sqlpower.architect.swingui.SQLType.getTypeName(((SQLColumn)column).getType())
												+"("+((SQLColumn)column).getScale()+")"
												+ newline,
												attrsSource );
										}
										else if ( ((SQLColumn)column).getScale() != targetColumn.getScale() ) {

											output.insertString(output.getLength(),
												" <<<DIFFERENT SCALE>>>" + newline, attrsMsg );
											output.insertString(output.getLength(),
													"      " + ((SQLColumn)column).getName() + "   " +
													ca.sqlpower.architect.swingui.SQLType.getTypeName(((SQLColumn)column).getType()) ,
													attrsSame );
											output.insertString(output.getLength(),
												"  ("+((SQLColumn)column).getScale()+")" + newline,
												attrsSource );
										}
										else {
											output.insertString(output.getLength(),
												"  " +
												ca.sqlpower.architect.swingui.SQLType.getTypeName(((SQLColumn)column).getType())
												+"("+((SQLColumn)column).getScale()+")"
												+ newline,
												attrsSame );
										} // compare column data type/scale
									} // column
								} // is column
							} // while table children
						} // table exist
					} // is table
					
					progress++;
				} // schema children, while loop
			} catch ( ArchitectException exp ) {
				System.err.println(exp.toString());
			} catch (BadLocationException ble) {
	            System.err.println("Couldn't insert styled text.");
	        }
	        finally {
	        	finished = true;
	        }
			
			
			
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					outputTextPane.setDocument(output);
				}
			});
		}
	}
	
	
	public class CompareProgressWatcher implements ActionListener {
		private JProgressBar bar;
		private CompareSchemaWorker worker;
		
		public CompareProgressWatcher ( JProgressBar bar, CompareSchemaWorker worker ) {
			this.bar = bar;
			this.worker = worker;
		}

		public void actionPerformed(ActionEvent evt) {
			try {
				int max = worker.getJobSize();
				bar.setVisible(true);
				bar.setMaximum(max);
				bar.setValue(worker.getProgress());
				bar.setIndeterminate(false);
				if ( worker.isFinished() ) {
					bar.setVisible(false);
					((javax.swing.Timer)evt.getSource()).stop();
					startCompareAction.setEnabled(true);
				}
			} catch ( ArchitectException e ) {
				logger.error("getProgress2 failt", e);
			}
		}
	
	}
	
	public synchronized JDialog getSourceNewConnectionDialog() {
		return sourceNewConnectionDialog;
	}
	
	private synchronized void setSourceNewConnectionDialog(JDialog d) {
		sourceNewConnectionDialog = d;
	}

	public synchronized JDialog getTargetNewConnectionDialog() {
		return targetNewConnectionDialog;
	}

	private synchronized void setTargetNewConnectionDialog(JDialog d) {
		targetNewConnectionDialog = d;
	}

	/**
	 * Just for testing the form layout without running the whole Architect.
	 * 
	 * <p>The frame it makes is EXIT_ON_CLOSE, so you should never use this
	 * in a real app.
	 */
	public static void main(String[] args) {
		final JFrame f = new JFrame("Testing compare dm panel");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				f.add(new CompareDMPanel());
				f.pack();
				f.setVisible(true);
			};
		});
	}
}
