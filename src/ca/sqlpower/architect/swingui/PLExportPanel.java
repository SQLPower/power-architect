package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class PLExportPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(PLExportPanel.class);

	protected DBConnectionSpec dbcs;
	protected TextPanel form;
	protected Vector history;
	protected Vector plodbc;
	protected JComboBox historyBox;
	protected JComboBox plODBCSourceName;
	protected JTextField plRepOwner;
	protected JTextField plFolderName;
	protected JTextField plJobId;
	protected JTextField plJobDescription;
	protected JTextField plJobComment;
	protected JTextField plOutputTableOwner;
	protected JCheckBox  runPLEngine;
	protected String     projName;
	private Map jdbcDrivers;
	private JButton newConnButton;
	
	public PLExportPanel() {
		setLayout(new BorderLayout());
		ArchitectFrame af = ArchitectFrame.getMainInstance();
		SwingUIProject project = af.getProject();
		projName = new String(project.getName());
		newConnButton= new JButton("New");
		newConnButton.addActionListener(new NewConnectionListener());

		List connectionHistory = af.prefs.getConnections();
		List plOdbcCon = getPLDBConnection("C:\\workarea\\pltest_april\\");
		history = new Vector();
		history.add(ASUtils.lvb("(Select PL Connection)", null));
		Iterator it = connectionHistory.iterator();
		while (it.hasNext()) {
			DBConnectionSpec spec = (DBConnectionSpec) it.next();
			history.add(ASUtils.lvb(spec.getDisplayName(), spec));
		}
		historyBox = new JComboBox(history);
		historyBox.addActionListener(new HistoryBoxListener());
		plodbc = new Vector();
		plodbc.add(ASUtils.lvb("(Select PL ODBC Connection)", null));
		Iterator itO = plOdbcCon.iterator();
		while (itO.hasNext()) {
			PLdbConn plcon = (PLdbConn) itO.next();
			plodbc.add(ASUtils.lvb(plcon.getLogical(), plcon));
		}
		plODBCSourceName = new JComboBox(plodbc);
		plODBCSourceName.addActionListener(new ODBCSourceListener());
		runPLEngine = new JCheckBox();
		runPLEngine.setEnabled(false);
		plFolderName = new JTextField(projName+"_Folder");
		plJobId      = new JTextField(projName+"_Job");
		JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        connectPanel.add(historyBox);
		connectPanel.add(newConnButton);

		JComponent[] fields = new JComponent[] {connectPanel,
												plODBCSourceName,
												plRepOwner = new JTextField(),
												plFolderName,
												plJobId,
												plJobDescription = new JTextField(),
												plJobComment = new JTextField(),
												plOutputTableOwner = new JTextField(),
												runPLEngine };
		String[] labels = new String[] {"PL Connection",
										"PL ODBC Source Name",
										"PL Repository Owner",
										"Folder Name",
										"Job Id",
										"Job Description",
										"Job Comment",
										"Target Schema Owner",
										"Run Engine"};

		char[] mnemonics = new char[] {'p', 's', 'r', 'f', 'i', 'd', 'c', 'o','e'};
		int[] widths = new int[] {30, 30, 20, 30, 30, 40, 60, 20,10};
		String[] tips = new String[] {"A list of connections you have made in the past",
									  "ODBC Source Name connection for PL",
									  "Owner of PL Repository",
									  "The folder name for transactions",
									  "The Job unique Id",
		                              "The Job Description",
		                              "Comment about the Job",
		                              "Owner(Schema) for output transaction tables",
									  "CheckIt if you want to run the engine"};
		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}

	public class HistoryBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) historyBox.getSelectedItem();
			if (lvb.getValue() != null) {
				dbcs = (DBConnectionSpec) lvb.getValue();
			}
		}
	}

	public class ODBCSourceListener  implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plODBCSourceName.getSelectedItem();
			if (lvb.getValue() == null) {
			    runPLEngine.setSelected(false);
				runPLEngine.setEnabled(false);
				plRepOwner.setText(null);
   		    } else {
				runPLEngine.setEnabled(true);
				PLdbConn pldbcon = (PLdbConn) lvb.getValue();	
				plRepOwner.setText(pldbcon.getPlsOwner());
				
			}
		}
	}

	
	public class NewConnectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final JDialog d = new JDialog(ArchitectFrame.getMainInstance(),
										  "New PL Repository Connection");
				JPanel plr = new JPanel(new BorderLayout(12,12));
				plr.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
				final DBCSPanel dbcsPanel = new DBCSPanel();
				plr.add(dbcsPanel, BorderLayout.CENTER);

				JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

				JButton okButton = new JButton("Ok");
				okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcs = new DBConnectionSpec();
							dbcsPanel.applyChanges(dbcs);
							historyBox.addItem(ASUtils.lvb(dbcs.getDisplayName(), dbcs));
							d.setVisible(false);
							
						}
					});
				buttonPanel.add(okButton);

				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							dbcsPanel.discardChanges();
							d.setVisible(false);
						}
					});
				buttonPanel.add(cancelButton);
				plr.add(buttonPanel, BorderLayout.SOUTH);
				d.setContentPane(plr);
				d.pack();
				d.setVisible(true);
			}
	}
	
	/**
	 * Returns the index of findMe in the DBCS history vector.  0
	 * means not found because 0 is the reserved index for new
	 * connections not in the list.
	 */
	public int findHistoryConnection(DBConnectionSpec findMe) {
		int i = 0;
		Iterator it = history.iterator();
		while (it.hasNext()) {
			ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) it.next();
			if (lvb.getValue() == findMe) {
				return i;
			}
			i++;
		}
		return 0;
	}

	public List getPLDBConnection(String plPath){
		List odbc = new ArrayList();
		PLdbConn plconn =  new PLdbConn();
		String fileIni = plPath+"pl.ini";
		File inputFile = new File(fileIni);
		try {
			FileReader in = new FileReader(inputFile);
			StringBuffer line = new StringBuffer();
			String label[] = new String[]{"Logical=",
										  "Type=",
										  "PL Schema Owner=",
										  "TNS Name=",
										  "Database Name="};
			String       type = "";
			String       plOwner;
			String       dbName;
			int c;
			int jj = 0;
			while ((c = in.read()) != -1) {
				if ( c != 13){
					line.append((char)c);
				} else {
					c = in.read();
					int k;
					if ((k = line.indexOf(label[jj])) >= 0){
					  if (label[jj].equals("Logical=")) {
						  	plconn =  new PLdbConn();
							plconn.setLogical(line.substring(k+8));
							//odbc.add(line.substring(k+8));
							System.out.println(line.substring(k+8));
							jj++;
					  } else {
						  if (label[jj].equals("Type=")){
						  	plconn.setDbType(line.substring(k+5));
							type = line.substring(k+5);
							System.out.println(line.substring(k+5));
							jj++;
						  } else {
							  if (label[jj].equals("PL Schema Owner=")){
						         plconn.setPlsOwner(line.substring(k+16));
								 plOwner = line.substring(k+16);
							     System.out.println(line.substring(k+16));
							   	 if ((type.toUpperCase()).equals("ORACLE")){
								    jj++;
							     } else {
								    jj = jj+2;
							     }
							  } else {
								 if (label[jj].equals("TNS Name=") || label[jj].equals("Database Name=")){
								     if (label[jj].equals("TNS Name=")){
										dbName =  line.substring(k+9);
									 } else {
									    dbName =  line.substring(k+14);
									 }	
									 plconn.setDbName(dbName);
									 odbc.add(plconn);
							         System.out.println(line.substring(k+9));
							         jj = 0;
							     }
							  }	 
					      }
					   }
					} else {
					  // System.out.println("*"+line);
					}
					  //odbc.add(line.substring(k+8));
					  //System.out.println(line.substring(k+8));
					  line.setLength(0);
				}  
            }
        in.close();
    } catch (IOException ie){
		System.out.println( "File PL.ini not found in specified path");
    }    
    return odbc;
}

	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current DBConnectionSpec.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public void applyChanges() {
		String name = plFolderName.getText();
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
	}

	// ---------------- accessors and mutators ----------------

	/**
	 * returns values typed in panel
	 */
	 public String getPlODBCSourceName(){
		 ASUtils.LabelValueBean lvb = (ASUtils.LabelValueBean) plODBCSourceName.getSelectedItem();
		 if (lvb.getValue() != null) {
			 PLdbConn	odbcSource = (PLdbConn) lvb.getValue();
			 return  odbcSource.getLogical();
		 } else {
			  return null;
		 }	  
	 }	 
	 
	 public String getPlRepOwner(){
		 return plRepOwner.getText();
	 }	 
	 
	 public String getPlFolderName(){
		 return plFolderName.getText();
	 }

	 public String getPlJobId(){
		 return plJobId.getText();
	 }
	 
	 public String getPlJobDescription(){
		 return plJobDescription.getText();
	 }
	 
	 public String getPlJobComment(){
		 return plJobComment.getText();
	 }
	 
	 public String getPlOutputTableOwner(){
		 return plOutputTableOwner.getText();
	 }
	 
	 public boolean isSelectedRunPLEngine(){
		 return runPLEngine.isSelected();
	 }
	 
	/**
	 * Returns a reference to the current DBConnectionSpec (that is,
	 * the one that will be updated when apply() is called).
	 */
	public DBConnectionSpec getDbcs() {
		return dbcs;
	}
}
