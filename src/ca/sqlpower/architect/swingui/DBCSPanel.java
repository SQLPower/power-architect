package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DBConnectionSpec;

public class DBCSPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(DBCSPanel.class);

	protected DBConnectionSpec dbcs;
	protected TextPanel form;

	protected JTextField dbNameField;
	protected String dbNameTemp;
	protected JComboBox dbDriverField;
	protected JTextField dbUrlField;
	protected JTextField dbUserField;
	protected JPasswordField dbPassField;

	private Map jdbcDrivers;

	public DBCSPanel() {
		setLayout(new BorderLayout());
		ArchitectFrame af = ArchitectFrame.getMainInstance();

		dbDriverField = new JComboBox(getDriverClasses());
		dbDriverField.insertItemAt("", 0);
		dbNameField = new JTextField();
		JComponent[] fields = new JComponent[] {dbNameField,
												dbDriverField,
												dbUrlField = new JTextField(),
												dbUserField = new JTextField(),
												dbPassField = new JPasswordField()};
		String[] labels = new String[] {"Connection Name",
										"JDBC Driver",
										"JDBC URL",
										"Username",
										"Password"};

		char[] mnemonics = new char[] {'n', 'd', 'u', 'r', 'p'};
		int[] widths = new int[] {30, 30, 40, 20, 20};
		String[] tips = new String[] {"Your nickname for this database",
									  "The class name of the JDBC Driver",
									  "Vendor-specific JDBC URL",
									  "Username for this database",
									  "Password for this database"};
		
		// update url field when user picks new driver
		dbDriverField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String t = getTemplateForDriver(dbDriverField.getSelectedItem().toString());
					if (t == null) t = "jdbc:";
					dbUrlField.setText(t);
				}
			});

		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}

	protected String[] getDriverClasses() {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return (String[]) jdbcDrivers.keySet().toArray(new String[0]);
	}
	
	protected String getTemplateForDriver(String driverClassName) {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return (String) jdbcDrivers.get(driverClassName);
	}
	
	private void setupDriverMap() {
		Map drivers = new HashMap();
		drivers.put("oracle.jdbc.driver.OracleDriver",
					"jdbc:oracle:thin:@<hostname>:1521:<instance>");
		drivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
					"jdbc:microsoft:sqlserver://<hostname>:1433;SelectMethod=cursor");
		drivers.put("org.postgresql.Driver",
					"jdbc:postgresql://<hostname>:5432");
		drivers.put("ibm.sql.DB2Driver",
					"jdbc:db2:<hostname>");
		jdbcDrivers = drivers;
	}

	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current DBConnectionSpec.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public void applyChanges() {
		String name = dbNameField.getText();
		dbcs.setName(name);
		dbcs.setDisplayName(name);
		dbcs.setDriverClass(dbDriverField.getSelectedItem().toString());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(new String(dbPassField.getPassword())); // completely defeats the purpose for JPasswordField.getText() being deprecated, but we're saving passwords to the config file so it hardly matters.
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
	}

	// ---------------- accessors and mutators ----------------

	/**
	 * Sets this DBCSPanel's fields to match those of the given dbcs,
	 * and stores a reference to the given dbcs so it can be updated
	 * when the applyChanges() method is called.
	 */
	public void setDbcs(DBConnectionSpec dbcs) {
		dbNameField.setText(dbcs.getName());
		dbDriverField.removeItemAt(0);
		if (dbcs.getDriverClass() != null) {
			dbDriverField.insertItemAt(dbcs.getDriverClass(), 0);
		} else {
			dbDriverField.insertItemAt("", 0);
		}
		dbDriverField.setSelectedIndex(0);
		dbUrlField.setText(dbcs.getUrl());
		dbUserField.setText(dbcs.getUser());
		dbPassField.setText(dbcs.getPass());
		this.dbcs = dbcs;
	}

	/**
	 * Returns a reference to the current DBConnectionSpec (that is,
	 * the one that will be updated when apply() is called).
	 */
	public DBConnectionSpec getDbcs() {
		return dbcs;
	}
}
