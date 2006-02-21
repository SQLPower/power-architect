package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import ca.sqlpower.architect.DeferredLoadable;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.ddl.GenericDDLGenerator;
import ca.sqlpower.architect.swingui.ASUtils.LabelValueBean;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.org.apache.xpath.internal.axes.HasPositionalPredChecker;

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
	private JComboBox sourceCatalogDropdown;
	private JComboBox sourceSchemaDropdown;
	private JButton sourceNewConnButton;

	// target database fields
	private JComboBox targetDatabaseDropdown;
	private JComboBox targetCatalogDropdown;
	private JComboBox targetSchemaDropdown;
	private JButton targetNewConnButton;

	private JProgressBar progressBar;
	
	private JPanel buttonPanel;

	private SQLDatabase sourceDatabase;
	private SQLDatabase targetDatabase;
	private SQLSchema sourceSQLSchema;
	private SQLSchema targetSQLSchema;
	private SQLCatalog sourceSQLCatalog;
	private SQLCatalog targetSQLCatalog;
	private JComboBox sqlTypeDropdown;
	

	private JTextPane outputTextPane;
	private AbstractDocument outputDoc;
	private JRadioButton sourcePlayPenRadio;
	private JRadioButton sourcePhysicalRadio;
	private StartCompareAction startCompareAction;
	private JRadioButton sqlButton; 

	private ButtonGroup operationGroup;
	JRadioButton sourceLikeTargetButton;
	JRadioButton targetLikeSourceButton;
	JRadioButton justCompareButton;
	

	
	public boolean isStartable()
	{

		/**
		 * selected index -1 means when there is nothing selected yet, 
		 * 0 means the special item we added to the first place: 'choose a connection'
		 * in both cases, it's not ready 
		 * 
		 */
		if ( sourcePhysicalRadio.isSelected())
		{
			if (sourceDatabaseDropdown.getSelectedIndex() <= 0)
			{
				return false;
			}
		}
		if (targetDatabaseDropdown.getSelectedIndex() <= 0)
		{
			return false;
		}
		return true;
	}
	
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
		sourcePlayPenRadio = new JRadioButton();
		sourcePlayPenRadio.setName("sourcePlayPenRadio");
		sourcePlayPenRadio.setActionCommand("Project");
		

		sourcePhysicalRadio = new JRadioButton();
		sourcePhysicalRadio.setName("sourcePhysicalRadio");
		sourcePhysicalRadio.setActionCommand("SQL Connection");

		//Group the radio buttons.
		ButtonGroup sourceButtonGroup = new ButtonGroup();
		sourceButtonGroup.add(sourcePlayPenRadio);
		sourceButtonGroup.add(sourcePhysicalRadio);
		// Generate an action so we load the playpen DataSource
		sourcePlayPenRadio.doClick();

		//Register a listener for the radio buttons.
		sourcePlayPenRadio.addActionListener(new SourceOptionListener());
		sourcePhysicalRadio.addActionListener(new SourceOptionListener());

		sourceDatabaseDropdown = new JComboBox();
		sourceDatabaseDropdown.addItem(null);   // the non-selection selection
		for (ArchitectDataSource ds : af.getUserSettings().getConnections()) {
			sourceDatabaseDropdown.addItem(ds);
		}
		
		sourceSchemaDropdown = new JComboBox();
		sourceSchemaDropdown.setName("sourceSchemaDropdown");
		sourceSchemaDropdown.setEnabled(false);
		
		sourceCatalogDropdown = new JComboBox();
		sourceCatalogDropdown.setName("sourceCatalogDropdown");
		sourceCatalogDropdown.setEnabled(false);
		
		
		sourceDatabaseDropdown.setName("sourceDatabaseDropdown");
		sourceDatabaseDropdown.setEnabled(false);
		sourceDatabaseDropdown.setRenderer(dataSourceRenderer);
		
		sourceNewConnButton = new JButton("New...");
		sourceNewConnButton.setName("sourceNewConnButton");
		sourceNewConnButton.setEnabled(false);
		sourceNewConnButton.addActionListener(newConnectionAction);
		

		targetSchemaDropdown = new JComboBox();
		targetSchemaDropdown.setName("targetSchemaDropdown");
		targetSchemaDropdown.setEnabled(false);


		targetCatalogDropdown = new JComboBox();
		targetCatalogDropdown.setName("targetCatalogDropdown");
		targetCatalogDropdown.setEnabled(false);
		
	
		
		
		targetDatabaseDropdown = new JComboBox();
		targetDatabaseDropdown.addItem(null);   // the non-selection selection
		for (ArchitectDataSource ds : af.getUserSettings().getConnections()) {
			targetDatabaseDropdown.addItem(ds);
		}
		
		
		targetDatabaseDropdown.setName("targetDatabaseDropdown");
		targetDatabaseDropdown.setRenderer(dataSourceRenderer);
		

		sourceSchemaDropdown.addActionListener(new SchemaChangeListener(sourceSchemaDropdown,true));
		targetSchemaDropdown.addActionListener(new SchemaChangeListener(targetSchemaDropdown,false));
		sourceCatalogDropdown.addActionListener(new SchemaPopulator(sourceSchemaDropdown,true));
		targetCatalogDropdown.addActionListener(new SchemaPopulator(targetSchemaDropdown,false));
		sourceDatabaseDropdown.addActionListener(new CatalogPopulator(sourceSchemaDropdown,sourceCatalogDropdown,true));
		targetDatabaseDropdown.addActionListener(new CatalogPopulator(targetSchemaDropdown,targetCatalogDropdown,false));
		
		
		targetNewConnButton = new JButton("New...");
		targetNewConnButton.setName("targetNewConnButton");
		targetNewConnButton.setEnabled(true);
		targetNewConnButton.addActionListener(newConnectionAction);

		

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);

		// layout compare methods check box and syntax combox
		sourceLikeTargetButton = new JRadioButton();
		sourceLikeTargetButton.setName("sourceLikeTargetButton");
		sourceLikeTargetButton.setActionCommand("source like target");
		sourceLikeTargetButton.setSelected(true);

		targetLikeSourceButton = new JRadioButton( );
		targetLikeSourceButton.setName("targetLikeSourceButton");
		targetLikeSourceButton.setActionCommand("target like source");
		targetLikeSourceButton.setSelected(false);

		justCompareButton = new JRadioButton();
		justCompareButton.setName("justCompareButton");
		justCompareButton.setActionCommand("compare");
		justCompareButton.setSelected(false);
		justCompareButton.setEnabled(false);

		//Group the radio buttons.
		operationGroup = new ButtonGroup();
		operationGroup.add(sourceLikeTargetButton);
		operationGroup.add(targetLikeSourceButton);
		operationGroup.add(justCompareButton);

		sqlTypeDropdown = new JComboBox(DDLUtils.getDDLTypes());
		sqlTypeDropdown.setName("sqlTypeDropDown");
		OutputChoiceListener listener = new OutputChoiceListener(sqlTypeDropdown,justCompareButton,targetLikeSourceButton);
		sqlButton = new JRadioButton( );
		sqlButton.setName("sqlButton");
		sqlButton.setActionCommand("sqlButton");
		sqlButton.setSelected(true);
		sqlButton.addActionListener(listener);
		
		JRadioButton englishButton = new JRadioButton();
		englishButton.setName("englishButton");
		englishButton.setActionCommand("english");
		englishButton.setSelected(false);
		englishButton.addActionListener(listener);

		//Group the radio buttons.
		ButtonGroup outputGroup = new ButtonGroup();
		outputGroup.add(sqlButton);
		outputGroup.add(englishButton);
	
		
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
		builder.append(sourceNewConnButton, sourceCatalogDropdown, sourceSchemaDropdown);
		
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
		builder.append(targetNewConnButton, targetCatalogDropdown, targetSchemaDropdown);
		
		builder.appendSeparator("Output Format");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(sqlButton);
		
		JPanel ddlTypePanel = new JPanel(new BorderLayout(3,3));
		ddlTypePanel.add(new JLabel("SQL for"), BorderLayout.WEST);
		ddlTypePanel.add(sqlTypeDropdown, BorderLayout.CENTER);  // ddl generator type list
		builder.append(ddlTypePanel);
		
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(englishButton);
		builder.append("English descriptions");
		builder.nextLine();
		
		builder.appendSeparator("Comparison Sense");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(sourceLikeTargetButton);
		builder.append("How to make Source like Target");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(targetLikeSourceButton);
		builder.append("How to make Target like Source");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.nextColumn(2);
		builder.append(justCompareButton);
		builder.append("Show all differences");
		builder.nextLine();
		
		builder.appendSeparator("Status");
		builder.appendRow(builder.getLineGapSpec());
		builder.appendRow("pref");
		builder.nextLine(2);
		builder.add(new JLabel(""), cc.xy(5, builder.getRow()));
		builder.add(progressBar, cc.xyw(7, builder.getRow(), 5));
		
		setLayout(new BorderLayout());
		add(builder.getPanel());
	}
	
	public class OutputChoiceListener implements ActionListener {

		JComboBox cb;
		JRadioButton justCompareButton;
		JRadioButton targetLikeSourceButton;		// default choice
		
		public OutputChoiceListener(JComboBox cb, JRadioButton justCompareButton, JRadioButton targetLikeSourceButton)
		{
			this.cb =cb;
			this.justCompareButton = justCompareButton;
			this.targetLikeSourceButton = targetLikeSourceButton;
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("sqlButton"))
			{
				cb.setEnabled(true);
				if ( justCompareButton.isSelected() )
					targetLikeSourceButton.setSelected(true);
				justCompareButton.setEnabled( false);
			}
			else
			{
				cb.setEnabled(false);
				justCompareButton.setEnabled(true);
			}
			
		}
		
	}
	

	
	public abstract class Populator extends ArchitectSwingWorker implements Lister {
	
		protected SQLDatabase.PopulateProgressMonitor progressMonitor;
		
		public Integer getJobSize() throws ArchitectException {
			if (progressMonitor != null)
			{
				return progressMonitor.getJobSize();
			}
			return null;
		}
		
		public int getProgress() throws ArchitectException {
			if (progressMonitor != null)
			{
				return progressMonitor.getProgress();
			}
			return 0;
		}
		
		public boolean isFinished() throws ArchitectException {
			if (progressMonitor != null)
			{
		
				return progressMonitor.isFinished();
			}
			return true;
		}
	}
	
	public class CatalogPopulator  extends Populator implements ActionListener{
		
		private JComboBox catalogs;
		private JComboBox schemas;
		private JComboBox databases;
		private SQLDatabase db;
		private SQLObject schemaParent;
		private List<SQLObject> children = null;
		private boolean isSource;
		
			
		public CatalogPopulator(JComboBox schemas, JComboBox catalogs, boolean isSource)
		{
			this.catalogs = catalogs;
			this.schemas = schemas;
			this.isSource = isSource;
		}
		
	
			
		public void actionPerformed(ActionEvent e) {
			databases = (JComboBox)e.getSource();
			ArchitectDataSource ds =(ArchitectDataSource) databases.getSelectedItem();
			schemaParent = null;
			children = null;
			Thread t;
			if (ds != null)
			{
				db = new SQLDatabase(ds);
				if ( isSource ) 		sourceDatabase = db;
				else					targetDatabase = db;
				try {
					progressMonitor = db.getProgressMonitor();
				} catch (ArchitectException e1) {
					logger.debug("Error getting progressMonitor",e1);
				}
				t = new Thread(this);
				t.start();
					
				// wait for all listers to finish
				if(((JComboBox)(e.getSource()) ).getSelectedIndex() == 0)
				{
					startCompareAction.setEnabled(false);
				}
			}
			else
			{
				if ( isSource ) {
					sourceSQLCatalog = null;
					sourceSQLSchema = null;
				}
				else {
					targetSQLCatalog = null;
					targetSQLSchema = null;
				}
				
				catalogs.removeAllItems();
				catalogs.setEnabled(false);
			
				schemas.removeAllItems();
				schemas.setEnabled(false);
			}
		}
		
	


		@Override
		public void cleanup() {

			if ( isSource ) {
				sourceSQLCatalog = null;
			}
			else {
				targetSQLCatalog = null;
			}
			catalogs.removeAllItems();
			catalogs.setEnabled(false);
			
			if  (children!= null)
			{
				
				for (SQLObject item : children) {
					catalogs.addItem(item);
				}
				children = null;
			}
			// If it has an item set this enabled
			if (catalogs.getItemCount() > 0) {
				catalogs.setEnabled(true);

				if ( isSource ) {
					sourceSQLCatalog = (SQLCatalog) catalogs.getSelectedItem();
				}
				else {
					targetSQLCatalog = (SQLCatalog) catalogs.getSelectedItem();
				}
				
				if (schemaParent == null)
				{
					schemaParent = (SQLObject) catalogs.getSelectedItem();
				}
			}
			
				new Thread(new Populator(){
					List<SQLObject> schemaChildren;
					@Override
					public void cleanup() {
						schemas.removeAllItems();
						schemas.setEnabled(false);
						
						if ( isSource ) {
							sourceSQLSchema = null;
						}
						else {
							targetSQLSchema = null;
						}

						if (schemaChildren != null)
						{
							for (SQLObject item : schemaChildren) {
								schemas.addItem(item);
							}
							
							// If it has an item set this enabled
							if (schemas.getItemCount() > 0) {
								if ( isSource ) {
									sourceSQLSchema = (SQLSchema) schemas.getSelectedItem();
								}
								else {
									targetSQLSchema = (SQLSchema) schemas.getSelectedItem();
								}
								schemas.setEnabled(true);
							}
						}
						
						startCompareAction.setEnabled(isStartable());
						
					}
					
					@Override
					public void doStuff() throws Exception {
						
						
						ListerProgressBarUpdater progressBarUpdater = new ListerProgressBarUpdater(progressBar,this);
						new javax.swing.Timer(100, progressBarUpdater).start();
						if (schemaParent != null)
						{
							schemaChildren = schemaParent.getChildren();
						}
						
					}
					
				}).start();
			
			
		}


		@Override
		public void doStuff() throws Exception {

			try {
				
				ListerProgressBarUpdater progressBarUpdater = new ListerProgressBarUpdater(progressBar,this);
				new javax.swing.Timer(100, progressBarUpdater).start();

				db.populate();
				if (db.isCatalogContainer())
				{
					children = db.getChildren();
					
				}
				else if ( db.isSchemaContainer() )
				{
					schemaParent = db;
				}

			} catch (ArchitectException e) {
				logger.debug("Unexpected architect exception in ConnectionListener",e);
				
			}
		}
	}

	public class SchemaPopulator  extends Populator implements ActionListener{
		
		private JComboBox schemas;
		private JComboBox catalogs;
		private List<SQLObject> children = null;
		private boolean isSource;
		
		
		public SchemaPopulator(JComboBox schemas, boolean isSource)
		{		
			this.schemas = schemas;
			this.isSource = isSource;
		}
		
	
			
		public void actionPerformed(ActionEvent e) {

			catalogs = (JComboBox)e.getSource();
			if ( isSource ) {
				sourceSQLCatalog = null;
			}
			else {
				targetSQLSchema = null;
			}
			
			if (catalogs != null)
			{
				if ( isSource ) {
					sourceSQLCatalog = (SQLCatalog) catalogs.getSelectedItem();
				}
				else {
					targetSQLCatalog = (SQLCatalog) catalogs.getSelectedItem();
				}
				schemas.removeAllItems();
				schemas.setEnabled(false);
			}
			Thread t = new Thread(this);
			t.start();
				
			startCompareAction.setEnabled(false);
		}
		
	


		@Override
		public void cleanup() {
			schemas.removeAllItems();
			schemas.setEnabled(false);
			if ( isSource ) {
				sourceSQLSchema = null;
			}
			else {
				targetSQLSchema = null;
			}
			
			if  (children!= null)
			{
				for (SQLObject item : children) {
					schemas.addItem(item);
					
				}				
				children = null;
			}
			// If it has an item set this enabled
			if (schemas.getItemCount() > 0) {
				if ( isSource ) {
					sourceSQLSchema = (SQLSchema) schemas.getSelectedItem();
				}
				else {
					targetSQLSchema = (SQLSchema) schemas.getSelectedItem();
				}
				schemas.setEnabled(true);
			}
			startCompareAction.setEnabled(isStartable());
		}


		@Override
		public void doStuff() throws Exception {
			try {
				
				ListerProgressBarUpdater progressBarUpdater = new ListerProgressBarUpdater(progressBar,this);
				new javax.swing.Timer(100, progressBarUpdater).start();

				children = ((SQLObject)catalogs.getSelectedItem()).getChildren();
				
			} catch (ArchitectException e) {
				logger.debug("Unexpected architect exception in ConnectionListener",e);
				
			}
		}
	}


	public class SchemaChangeListener implements ActionListener {

		
		private SQLSchema sch;
		private JComboBox cb;
		private boolean isSource;

		public SchemaChangeListener(JComboBox cb, boolean isSource) {
			this.isSource = isSource;
			this.cb = cb;
		}
			
		public void actionPerformed(ActionEvent e) {
			if ( cb != null )
				if ( isSource )
					sourceSQLSchema = (SQLSchema) cb.getSelectedItem();
				else
					targetSQLSchema = (SQLSchema) cb.getSelectedItem();
		}
	}

	public class SourceOptionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("Project")) {
				sourceDatabase = ArchitectFrame.getMainInstance().getProject().getPlayPen().getDatabase();
				sourceDatabaseDropdown.setEnabled(false);
				sourceNewConnButton.setEnabled(false);
				sourceCatalogDropdown.setEnabled(false);
				sourceSchemaDropdown.setEnabled(false);
			} else {
				if (sourceDatabaseDropdown.getSelectedIndex()>0)
				{
					sourceDatabase = new SQLDatabase((ArchitectDataSource) sourceDatabaseDropdown.getSelectedItem());
				}
				sourceDatabaseDropdown.setEnabled(true);
				sourceNewConnButton.setEnabled(true);
				sourceCatalogDropdown.setEnabled(false);
				sourceSchemaDropdown.setEnabled(false);
			}
		}
	}
	

	public interface Lister  {
		
	
		
		public Integer getJobSize() throws ArchitectException;
		
		public int getProgress() throws ArchitectException;
		
		public boolean isFinished() throws ArchitectException;
		
	
	}



	/**
	 * Intended to be called periodically by a Swing Timer thread.  Whenever the actionPerformed
	 * method is called, it polls the lister for its job size and current progress, then updates
	 * the given progress bar with that information.
	 */
	public class ListerProgressBarUpdater implements ActionListener {
		private JProgressBar bar;
		private Lister lister;
		
		public ListerProgressBarUpdater ( JProgressBar bar, Lister lister ) {
			this.bar = bar;
			this.lister = lister;
		}

		/**
		 * Must be invoked on the Event Dispatch Thread, most likely by a Swing Timer.
		 */
		public void actionPerformed(ActionEvent evt) {

			try {
				Integer max = lister.getJobSize();   // could take noticable time to calculate job size
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
		
		private HashMap sourceTableList;
		private HashMap targetTableList;
		private HashMap diffList;
		private final int A2B = 1;
		private final int B2A = 2;
		private final int ALL = 3;
		
		
		
		private int compareMode;
		
		public StartCompareAction() {
			super("Start");
			sourceTableList = new HashMap();
			targetTableList = new HashMap();
			diffList = new HashMap();
			
			if ( sourceLikeTargetButton.isSelected() ) {
				compareMode = A2B;
			} else if ( targetLikeSourceButton.isSelected() ) {
				compareMode = B2A;
			} else if ( justCompareButton.isSelected() ) {
				compareMode = ALL;
			}
		}
		
		public void actionPerformed(ActionEvent e) {

			startCompareAction.setEnabled(false);
			
		
			try {

				SQLObject o = null;
				if ( sourcePhysicalRadio.isSelected()) {
					if ( sourceSQLSchema != null ) {
						o = sourceSQLSchema;
					}
					else if ( sourceSQLCatalog != null ) {
						o = sourceSQLCatalog;
					}
					else if ( sourceDatabase != null ) {
						o = sourceDatabase;
					}
					
				}
				else {
					o = ArchitectFrame.getMainInstance().playpen.getDatabase();
				}
				
				if ( o == null || o.getChildType() != SQLTable.class || o.getChildCount() == 0 ) {
				} else { 
					for ( SQLTable t : (List<SQLTable>) o.getChildren() )
						sourceTableList.put(t.getName(),t);
				}
				
				o = null;
				if ( targetSQLSchema != null ) {
					o = targetSQLSchema;
				}
				else if ( targetSQLCatalog != null ) {
					o = targetSQLCatalog;
				}
				else if ( targetDatabase != null ) {
					o = targetDatabase;
				}
				
				if ( o == null || o.getChildType() != SQLTable.class || o.getChildCount() == 0 ) {
				} else {
					for ( SQLTable t : (List<SQLTable>) o.getChildren() )
						targetTableList.put(t.getName(),t);
				}
								
				LabelValueBean lvb = null;
				GenericDDLGenerator ddlgen = null;
				if ( sqlTypeDropdown.isEnabled() ) {
					lvb = (LabelValueBean) sqlTypeDropdown.getSelectedItem();
					ddlgen = (GenericDDLGenerator) (((Class)lvb.getValue())).newInstance();
				}
				
				CompareSchemaWorker worker = new CompareSchemaWorker(sourceTableList,targetTableList,diffList);
					
				CompareProgressWatcher watcher = new CompareProgressWatcher(progressBar,worker);
				new javax.swing.Timer(100, watcher).start();
				new Thread(worker).start();

				
				for ( mySSQLObject object : (Collection<mySSQLObject> )(diffList.values()) ) {
					System.out.println("diff:"+object.getObject().getName()+" source?"+object.isFromSource()+"  type:"+object.getClass());
				}

			} catch ( ArchitectException exp) {
				logger.error("SchemaListerProgressWatcher failt2", exp);
			} catch (InstantiationException ie) {
				logger.error("Someone put a non GenericDDLGenerator class into the lvb contained in the source pulldown menu",ie);
			} catch (IllegalAccessException iae) {
				logger.error("Cannot access the classes's constructor ",iae);
			}
			// get the title string for the compareDMFrame
			String compMethod = null;
			if (sqlButton.isSelected())
			{
				compMethod = "SQL";
			}
			else
			{
				compMethod = "english";
			}
			String titleString = "Comparing " + " to " + " using "+compMethod;
			
			CompareDMFrame cf = new CompareDMFrame( new DefaultStyledDocument(), titleString, sourceDatabase);
			cf.pack();
			cf.setVisible(true);
			
		}
	}
	

	public class mySSQLObject {
		private boolean isFromSource;
		private SQLObject object;
		
		public mySSQLObject (boolean source, SQLObject obj) {
			this.isFromSource = source;
			this.object = obj;
		}
		public boolean isFromSource() {
			return isFromSource;
		}
		public void setFromSource(boolean isFromSource) {
			this.isFromSource = isFromSource;
		}
		public SQLObject getObject() {
			return object;
		}
		public void setObject(SQLObject object) {
			this.object = object;
		}
		
	}
	public class CompareSchemaWorker implements Runnable {
		
		private HashMap sourceTableList;
		private HashMap targetTableList;
		private HashMap diffList;
		int	jobSize;
		int	progress;
		boolean finished;
		private GenericDDLGenerator ddlGenerator;
		
		
		//collection of sqltable<sqlobject> sqlcolumn<sqlobject> from<string>
		
		public CompareSchemaWorker (HashMap sourceTableList, HashMap targetTableList, HashMap diffList ) throws ArchitectException {
			
		
			this.sourceTableList = sourceTableList;
			this.targetTableList = targetTableList;
			this.diffList = diffList;
			
			jobSize = targetTableList.size()+sourceTableList.size();
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
			
			try {
				
				for ( SQLTable sourceTable : (Collection<SQLTable> )(sourceTableList.values()) ) {
					SQLTable targetTable = null;
					if ( (targetTable = (SQLTable)targetTableList.get(sourceTable.getName())) != null ) {

						for ( SQLColumn col : (List<SQLColumn>)(sourceTable.getColumns()) ) {
							SQLColumn col2 = targetTable.getColumnByName(col.getName(),false);
							if ( col2 == null )
								diffList.put(col.getName(),new mySSQLObject(true,col));
							else if ( col.getType() != col2.getType() ||
									 col.getPrecision() != col2.getPrecision() ||
									 col.getScale() != col2.getScale() ) {

									diffList.put(col.getName(),new mySSQLObject(true,col));
									diffList.put(col2.getName(),new mySSQLObject(false,col2));
							}
						}
						
						targetTableList.remove(sourceTable.getName());
						progress++;
					}
					else {
						diffList.put( sourceTable.getName(), new mySSQLObject(true,sourceTable));
					}
					progress++;
				}

				for ( SQLTable targetTable : (Collection<SQLTable> )(targetTableList.values()) ) {
					diffList.put( targetTable.getName(), new mySSQLObject(false,targetTable));
					progress++;
				}
				
			} catch (ArchitectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        finally {
	        		finished = true;
	        }
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
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
