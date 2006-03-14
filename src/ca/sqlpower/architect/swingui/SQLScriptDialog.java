package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.LogWriter;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class SQLScriptDialog extends JDialog implements Monitorable, Runnable {

	private static final Logger logger = Logger.getLogger(SQLScriptDialog.class);
	
	private List<DDLStatement> statements;
	private JProgressBar progressBar = new JProgressBar();

	private Runnable nextProcess;

	private Component parent;
	private String header;
	private int stmtsTried = 0;
	private int stmtsCompleted = 0;
	private boolean finished = false;
	private boolean cancelled = false;
	private boolean hasStarted = false;
	private boolean allIsWell = true; // TODO: consolidate error messages into a single block?
	private JLabel statusLabel; // FIXME: rename to statusLabel
	private ArchitectDataSource targetDataSource;
	
	private JTextPane sqlScriptArea;
	private AbstractDocument sqlDoc;

	private boolean closeParent;

	
	
	public SQLScriptDialog(Frame owner, String title, String header, boolean modal,
			List<DDLStatement> statements, ArchitectDataSource targetDataSource,
			boolean closeParent )
			throws HeadlessException {
		super(owner, title, modal);
		statusLabel = new JLabel();
		parent = owner;
		this.header = header;
		this.statements = statements;
		this.targetDataSource = targetDataSource;
		this.closeParent = closeParent;
		System.out.println ("The list size is :" + statements.size());
		add (buildPanel());
		pack();
		setVisible(true);
		
		

	}

	public SQLScriptDialog(Dialog owner, String title, String header, boolean modal,
			List<DDLStatement> statements, ArchitectDataSource targetDataSource,
			boolean closeParent )
			throws HeadlessException {
		super(owner, title, modal);
		statusLabel = new JLabel();
		parent = owner;
		this.header = header;
		this.statements = statements;
		this.targetDataSource = targetDataSource;
		this.closeParent = closeParent;
		System.out.println ("The list size is :" + statements.size());
		add (buildPanel());
		pack();
		setVisible(true);
	}

	private JPanel buildPanel() {
		FormLayout sqlLayout = new FormLayout(
				"4dlu, min:grow, 4dlu", //columns
				"pref, 6dlu, fill:300dlu:grow,6dlu, 20dlu, 6dlu, 20dlu"); //rows		
		
		CellConstraints cc = new CellConstraints();
		
		sqlDoc = new DefaultStyledDocument();
		
		SimpleAttributeSet att = new SimpleAttributeSet();
		StyleConstants.setForeground(att, Color.black);
		
		for (DDLStatement ddl : statements){
			try {
				sqlDoc.insertString(sqlDoc.getLength(),ddl.getSQLText(),att);				
			} catch(BadLocationException e) {
				ASUtils.showExceptionDialog(
						"Could not create document for results", e);
				logger.error("Could not create document for results", e);
			}
		}
		sqlScriptArea = new JTextPane();
		sqlScriptArea.setMargin(new Insets(6, 10, 4, 6));
		sqlScriptArea.setDocument(sqlDoc);		
		sqlScriptArea.setEditable(false);
		sqlScriptArea.setAutoscrolls(true);
		JScrollPane sp = new JScrollPane(sqlScriptArea);
		
		Action copy = new copyAction(sqlDoc);
		Action execute = null;
		if ( targetDataSource != null ) {
			execute = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					cancelled = false;
					finished = false;
					new Thread(SQLScriptDialog.this).start();
				}			
			};
		}
		
		Action save = new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				
				System.out.println( "SQL_FILE_FILTER:"+ ((FileExtensionFilter) ASUtils.SQL_FILE_FILTER).toString());
				
				SaveDocument sd = new SaveDocument(parent,sqlDoc,
						(FileExtensionFilter) ASUtils.SQL_FILE_FILTER );
				
			}
		};
		CloseAction close = new CloseAction();
		close.setWhatToClose(this);
		
		
		ButtonBarBuilder barBuilder = new ButtonBarBuilder();
		JButton copyButton = new JButton(copy);
		copyButton.setText("Copy");
		barBuilder.addGridded (copyButton);
		barBuilder.addRelatedGap();
		barBuilder.addGlue();
		
		
		JButton executeButton = new JButton(execute);
		executeButton.setText("Execute");
		barBuilder.addGridded(executeButton);		
		barBuilder.addRelatedGap();
		barBuilder.addGlue();
		if ( execute == null ) {
			
			
			executeButton.setEnabled(false);
		}
		
		JButton saveButton = new JButton(save);
		saveButton.setText("Save");
		barBuilder.addGridded(saveButton);
		barBuilder.addRelatedGap();
		barBuilder.addGlue();		
		
		JButton closeButton = new JButton(close);
		closeButton.setText("Close");
		barBuilder.addGridded(closeButton);
		
		PanelBuilder pb;



		JPanel panel = logger.isDebugEnabled() ? new FormDebugPanel(sqlLayout) : new JPanel(sqlLayout);
		pb = new PanelBuilder(sqlLayout, panel);			
		pb.setDefaultDialogBorder();	
		pb.add(new JLabel(header), cc.xy(2, 1));			
        pb.add(sp, cc.xy(2, 3));
    	pb.add(barBuilder.getPanel(), cc.xy(2, 5, "c,c"));
		
		return pb.getPanel();
		
	}
	
	public class copyAction extends AbstractAction{

		AbstractDocument doc;
		public copyAction(AbstractDocument doc)
		{
			this.doc = doc;
		}
		
		public void actionPerformed(ActionEvent e) {
			
			try {
				StringSelection selection = new StringSelection(doc.getText(0,doc.getLength()));
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection,selection);
			} catch (BadLocationException e1) {
				logger.debug("Unable to get the text for copying"+ e1);
			}
			
		}			
	}
	
	
	public class CloseAction extends AbstractAction {	

		Component c;
		
		public void setWhatToClose(Component c){
			this.c = c;				
		}
		public void actionPerformed(ActionEvent e) {
			c.setVisible(false);
			if ( closeParent )
				parent.setVisible(false);
		}						
	}
	
	/**
	 * This method runs on a separate worker thread.
	 */
	public void run() {
		hasStarted = true;
		if (cancelled || finished) return;
		statusLabel.setText("Creating objects in target database...");
		ProgressWatcher pw = new ProgressWatcher(progressBar, this, statusLabel);
		stmtsTried = 0;
		stmtsCompleted = 0;
		SQLDatabase target = new SQLDatabase(targetDataSource);
		
		logger.debug("the Target Database is: " + target.getDataSource());
		
		Connection con;
		Statement stmt;
		
		try {
			con = target.getConnection();
		} catch (ArchitectException ex) {
			allIsWell = false;
			final Exception fex = ex;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog
					(SQLScriptDialog.this,
							"Couldn't connect to target database: "+fex.getMessage()
							+"\nPlease check the connection settings and try again.");
					ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
				}
			});								
			finished = true;
			return;
		} catch (Exception ex) {
			allIsWell = false;
			logger.error("Unexpected exception in DDL generation", ex);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog
					(SQLScriptDialog.this,
							"You have to specify a target database connection"
							+"\nbefore executing this script.");
					ArchitectFrame.getMainInstance().playpen.showDbcsDialog();
				}
			});								
			finished = true;
			return;
		}
		
		try {
			logger.debug("the connection thinks it is: " + con.getMetaData().getURL());
			stmt = con.createStatement();
		} catch (SQLException ex) {
			allIsWell = false;
			final Exception fex = ex;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog
					(SQLScriptDialog.this,
							"Couldn't generate DDL statements: "+fex.getMessage()
							+"\nThe problem was reported by the target database.");
				}
			});								
			finished = true;
			return;
		}

		LogWriter logWriter = null;
		
		try {
			logWriter = new LogWriter(ArchitectSession.getInstance().getUserSettings().getDDLUserSettings().getDDLLogPath());
		} catch (ArchitectException ex) {
			allIsWell = false;				
			final Exception fex = ex;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog
					(SQLScriptDialog.this,
							"A problem with the DDL log file prevented\n"
							+"DDL generation from running:\n\n"
							+fex.getMessage());
				}
			});
			finished = true;
			return; // FIXME: this won't allow the next process to get invoked
		}
		
		try {
			logWriter.info("Starting DDL Generation at " + new java.util.Date(System.currentTimeMillis()));
			logWriter.info("Database Target: " + target.getDataSource());
			logWriter.info("Playpen Dump: " + target.getDataSource());
			
			
			Iterator it = statements.iterator();
			while (it.hasNext() && !finished) {
				DDLStatement ddlStmt = (DDLStatement) it.next();
				try {
					stmtsTried++;
					logWriter.info("executing: " + ddlStmt.getSQLText());		
					stmt.executeUpdate(ddlStmt.getSQLText());
					stmtsCompleted++;
				} catch (SQLException ex) {
					allIsWell = false;						
					final Exception fex = ex;
					final String fsql = ddlStmt.getSQLText();
					final LogWriter fLogWriter = logWriter; 
					logWriter.info("sql statement failed: " + ex.getMessage());
					try {
						SwingUtilities.invokeAndWait(new Runnable() {						
							public void run() {
								JTextArea jta = new JTextArea(fsql,25,40);
								jta.setEditable(false);
								JScrollPane jsp = new JScrollPane(jta);
								JLabel errorLabel = new JLabel("<html>This SQL statement failed: "+fex.getMessage()
										+"<p>Do you want to continue?</html>");
								JPanel jp = new JPanel(new BorderLayout());
								jp.add(jsp,BorderLayout.CENTER);
								jp.add(errorLabel,BorderLayout.SOUTH);
								int decision = JOptionPane.showConfirmDialog
								(SQLScriptDialog.this, jp, "SQL Failure", JOptionPane.YES_NO_OPTION);
								if (decision == JOptionPane.NO_OPTION) {
									fLogWriter.info("Export cancelled by user.");
									cancelJob();
								}
							}
						});
					} catch (InterruptedException ex2) {
						allIsWell = false;
						logger.warn("DDL Worker was interrupted during InvokeAndWait", ex2);
					} catch (InvocationTargetException ex2) {
						allIsWell = false;							
						final Exception fex2 = ex2;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								JOptionPane.showMessageDialog
								(SQLScriptDialog.this, "Worker thread died: "+fex2.getMessage());
							}
						});
					}
					
					if (isCancelled()) {
						finished = true;
						// don't return, we might as well display how many statements ended up being processed...
					}
				} 
			}
			
		} finally {
			// flush and close the LogWriter
			logWriter.info("Successfully executed "+stmtsCompleted+" out of "+stmtsTried+" statements.");
			logWriter.flush();
			logWriter.close();
		}
		
		try {
			stmt.close();
		} catch (SQLException ex) {
			logger.error("SQLException while closing statement", ex);
		}			
		
		// show them what they've won!	
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String message =  "Successfully executed "+stmtsCompleted+" out of "+stmtsTried+" statements.";
				if (stmtsCompleted == 0 && stmtsTried > 0) {
					message += ("\nBetter luck next time!");
				}
				JOptionPane.showMessageDialog(SQLScriptDialog.this, message);
			}
		});
		
		finished = true;
		
		SwingUtilities.invokeLater(new Runnable() {public void run(){runFinished();}});
	}
	
	/**
	 * Displays error messages or invokes the next process in the chain on a new
	 * thread. The run method asks swing to invoke this method on the event dispatch
	 * thread after it's done.
	 */
	public void runFinished() {
		if (allIsWell) {
			if (nextProcess != null) {
				new Thread(nextProcess).start();
			}
		}
	}
	
	/**
	 * @return Returns the nextProcess.
	 */
	public Runnable getNextProcess() {
		return nextProcess;
	}
	/**
	 * @param nextProcess The nextProcess to set.
	 */
	public void setNextProcess(Runnable nextProcess) {
		this.nextProcess = nextProcess;
	}

	
	// ============= Monitorable Interface =============
	
	public Integer getJobSize() throws ArchitectException {			
		if (statements != null) {
			return new Integer(statements.size());
		} else {
			return null;
		}
	}
	
	public int getProgress() throws ArchitectException {
		return stmtsTried;			
	}
	
	public boolean isFinished() throws ArchitectException {
		return finished;
	}
	
	public String getMessage() {
		return null;
	}
	
	public void cancelJob() {
		cancelled = true;
		finished = true;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public boolean hasStarted() {
		return hasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}
	
}
