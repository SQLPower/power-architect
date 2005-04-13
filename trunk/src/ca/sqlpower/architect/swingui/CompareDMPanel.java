package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.text.*;
import javax.swing.Timer;


import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;


import java.awt.event.*;
import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.*;



import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;
import ca.sqlpower.architect.etl.*;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.*;
import ca.sqlpower.architect.ddl.*;

public class CompareDMPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(CompareDMPanel.class);
	final String newline = "\n";

	protected Vector sourceConnections;
	protected Vector targetConnections;


	// Left-hand side fields
	protected JComboBox sourceConnectionsBox;
	protected JComboBox sourceSchemaBox;

	// Right-hand side fields
	protected JComboBox targetConnectionsBox;
	protected JComboBox targetSchemaBox;

	protected JProgressBar sourceProgressBar;
	protected JProgressBar targetProgressBar;
	
	JPanel buttonPanel;
	JProgressBar compareProgressBar;

	protected String projName;
	
	SQLDatabase sourceDatabase;
	SQLDatabase targetDatabase;
	SQLSchema sourceSQLSchema;
	SQLSchema targetSQLSchema;

	JTextPane outputTextPane;
	AbstractDocument outputDoc;
	
	
        
        
        
	

	protected StartCompareAction startCompareAction;


	public Action getStartCompareAction() {
		return startCompareAction;
	}

	public JPanel  getButtonPanel() {
		return buttonPanel;
	}

	public CompareDMPanel() {

		ArchitectFrame af = ArchitectFrame.getMainInstance();
		SwingUIProject project = af.getProject();
		projName = new String(project.getName());
		
		
		setLayout(new BorderLayout());



		// layout source database option/combox target combox
		JRadioButton usePlayPaneButton = new JRadioButton("Project ["+projName + "]" );
		usePlayPaneButton.setActionCommand("Project");
		usePlayPaneButton.setSelected(true);

		JRadioButton useSQLConnectionButton = new JRadioButton("");
		useSQLConnectionButton.setActionCommand("SQL Connection");

		//Group the radio buttons.
		ButtonGroup group = new ButtonGroup();
		group.add(usePlayPaneButton);
		group.add(useSQLConnectionButton);

		//Register a listener for the radio buttons.
		usePlayPaneButton.addActionListener(new SourceOptionListener());
		useSQLConnectionButton.addActionListener(new SourceOptionListener());


		sourceConnections = new Vector();
		sourceConnections.add(ASUtils.lvb("(Select Architect Connection)", null));
		Iterator it = af.getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			sourceConnections.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		sourceConnectionsBox = new JComboBox(sourceConnections);
		sourceConnectionsBox.addActionListener(new ConnectionListener());
		sourceConnectionsBox.setEnabled(false);

		sourceSchemaBox = new JComboBox();
		sourceSchemaBox.setEnabled(false);
		sourceSchemaBox.setVisible(false);

		sourceProgressBar = new JProgressBar();
		sourceProgressBar.setVisible(false);
		
		JComponent[] sourceDMFields = new JComponent[] {usePlayPaneButton,useSQLConnectionButton,
													sourceConnectionsBox, sourceSchemaBox, sourceProgressBar};
		String[] sourceDMLabels = new String[] {"Project in Play Pane", "Sql Connection", "Connection Name", "Schema Owner", ""};
		char[] sourceDMMnemonics = new char[] {'p', 's', 't', 'o', 'o'};
		int[] sourceDMWidths = new int[] {18, 18, 18, 18, 18};

		String[] sourceDMTips = new String[] {"Select the Project in Play Pane", "Select a Sql Connection",
										"Source Connection to compare with",
										"Owner (Schema) for tables", "Loading Progress"};



		TextPanel sourceDMForm = new TextPanel(sourceDMFields, sourceDMLabels,
												 sourceDMMnemonics, sourceDMWidths, sourceDMTips);


		targetConnections = new Vector();
		targetConnections.add(ASUtils.lvb("(Select Architect Connection)", null));
		it = af.getUserSettings().getConnections().iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			targetConnections.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		targetConnectionsBox = new JComboBox(targetConnections);
		targetConnectionsBox.addActionListener(new ConnectionListener());

		targetSchemaBox = new JComboBox();
		targetSchemaBox.setEnabled(false);

		targetProgressBar = new JProgressBar();
		targetProgressBar.setVisible(false);
		
		JComponent[] targetDMFields = new JComponent[] {targetConnectionsBox, targetSchemaBox, targetProgressBar };

		String[] targetDMLabels = new String[] {"Target Database Name",
											  "Schema", "" };

		char[] targetDMMnemonics = new char[] {'a', 's', 's'};
		int[] targetDMWidths = new int[] {18, 18, 18 };

		String[] targetDMTips = new String[] {"Target Connection to compare with",
										  "Owner (Schema)", "Loading Progress"};

		TextPanel targetDMForm = new TextPanel(targetDMFields, targetDMLabels,
												targetDMMnemonics, targetDMWidths, targetDMTips);

		JPanel p1 = new JPanel(new GridLayout(1,2));
		p1.add(sourceDMForm);
		p1.add(targetDMForm);

		add(p1, BorderLayout.NORTH);

		
		// layout compare methods check box and syntax combox
		JRadioButton sourceLikeTargetButton = new JRadioButton("How to make source like target" );
		sourceLikeTargetButton.setActionCommand("source like target");
		sourceLikeTargetButton.setSelected(false);

		JRadioButton targetLikeSourceButton = new JRadioButton("How to make target like source" );
		targetLikeSourceButton.setActionCommand("target like source");
		targetLikeSourceButton.setSelected(false);

		JRadioButton justCompareButton = new JRadioButton("Cpmpare source and target" );
		justCompareButton.setActionCommand("compare");
		justCompareButton.setSelected(true);

		//Group the radio buttons.
		ButtonGroup operationGroup = new ButtonGroup();
		operationGroup.add(sourceLikeTargetButton);
		operationGroup.add(targetLikeSourceButton);
		operationGroup.add(justCompareButton);

		JCheckBox sqlSyntax = new JCheckBox("SQL");
		sqlSyntax.setSelected(false);

		JPanel p2 = new JPanel(new FlowLayout());
		p2.add(sourceLikeTargetButton);
		p2.add(targetLikeSourceButton);
		p2.add(justCompareButton);
		p2.add(sqlSyntax);
		
		// outputDoc outputTextPane
		Font font = new Font("Courier New", Font.PLAIN, 12 );

		outputTextPane = new JTextPane();
        outputTextPane.setCaretPosition(0);
        outputTextPane.setMargin(new Insets(5,5,5,5));
        
        /*
        StyledDocument styledDoc = outputTextPane.getStyledDocument();
        if (styledDoc instanceof AbstractDocument) {
            outputDoc = (AbstractDocument)styledDoc;
        } else {
            System.err.println("Text pane's document isn't an AbstractDocument!");
            return;
        }
        */
        
        JScrollPane scrollPane = new JScrollPane(outputTextPane);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        

  		add(p2, BorderLayout.CENTER);
  		
		add(scrollPane, BorderLayout.SOUTH);


		startCompareAction = new StartCompareAction();
		startCompareAction.setEnabled(false);
		
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		compareProgressBar = new JProgressBar();
		compareProgressBar.setVisible(false);
		compareProgressBar.setPreferredSize(new Dimension(400, 15));
		buttonPanel.add(compareProgressBar);
		
		
	}

	public class ConnectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb;
			if (e.getSource() == sourceConnectionsBox) {
				 lvb = (ASUtils.LabelValueBean) sourceConnectionsBox.getSelectedItem();

				sourceSchemaBox.setEnabled(false);
				if (lvb.getValue() == null) {
					sourceSchemaBox.setVisible(false);
	   		    }
	   		    else {
					try {
						SchemaLister sl = new SchemaLister(new SQLDatabase((DBConnectionSpec)lvb.getValue()),true);
						SchemaListerProgressWatcher taskPerformer = new SchemaListerProgressWatcher(sourceProgressBar,sl);
						new javax.swing.Timer(100, taskPerformer).start();
						new Thread(sl).start();
					} catch ( ArchitectException exp) {
						logger.error("SchemaListerProgressWatcher failt2", exp);
					}
				}
			}
			else if (e.getSource() == targetConnectionsBox ) {
				lvb = (ASUtils.LabelValueBean) targetConnectionsBox.getSelectedItem();
				targetSchemaBox.setEnabled(false);
				if (lvb.getValue() == null) {
					targetSchemaBox.setVisible(false);
	   		    }
	   		     else {
					try {
						SchemaLister sl = new SchemaLister(new SQLDatabase((DBConnectionSpec)lvb.getValue()),false);
						SchemaListerProgressWatcher taskPerformer = new SchemaListerProgressWatcher(targetProgressBar,sl);
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
			if ( e.getActionCommand().equals("Project") ) {
				sourceConnectionsBox.setEnabled(false);
				sourceSchemaBox.setEnabled(false);
			}
			else {
				sourceConnectionsBox.setEnabled(true);
				sourceSchemaBox.setEnabled(true);
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
					if ( object instanceof SQLTable ) {
					}
					else if ( object instanceof SQLSchema ) {
						schema.add(object.getShortDisplayName());
					}
				}
			} catch(  ArchitectException e2 ) {
				logger.error("connection failt2", e2);
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					
					if ( sourceInd ) {
						sourceSchemaBox.setEnabled(true);
						sourceSchemaBox.setVisible(true);
						sourceSchemaBox.removeAllItems();
					
						Iterator it = schema.iterator();
						while (it.hasNext()) {
							sourceSchemaBox.addItem((String)it.next());
						}
						sourceDatabase = db;
					}
					else {
						targetSchemaBox.setEnabled(true);
						targetSchemaBox.setVisible(true);
						targetSchemaBox.removeAllItems();
					
						Iterator it = schema.iterator();
						while (it.hasNext()) {
							targetSchemaBox.addItem((String)it.next());
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
				        

				
				sourceSQLSchema = sourceDatabase.getSchemaByName((String)sourceSchemaBox.getSelectedItem());
				targetSQLSchema = targetDatabase.getSchemaByName((String)targetSchemaBox.getSelectedItem());

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
													 				
				CompareProgressWatcher watcher = new CompareProgressWatcher(compareProgressBar,worker);
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
											targetDatabase.getConnectionSpec().getDisplayName() +
											"." + targetSchemaBox.getSelectedItem() + ">>>" + newline,
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
}
