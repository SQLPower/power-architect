package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;

import ca.sqlpower.sql.DBConnectionSpec;

public class DBCSPanel extends JPanel implements ArchitectPanel {

	protected DBConnectionSpec dbcs;
	protected TextPanel form;

	protected JTextField dbNameField;
	protected JComboBox dbDriverField;
	protected JTextField dbUrlField;
	protected JTextField dbUserField;
	protected JPasswordField dbPassField;

	private Map jdbcDrivers;

	public DBCSPanel(DBConnectionSpec dbcs) {
		setupContents();
		setDbcs(dbcs);
	}

	public DBCSPanel() {
		setupContents();
		DBConnectionSpec dbcs = new DBConnectionSpec();
		dbcs.setName("New connection");
		setDbcs(dbcs);
	}

	/**
	 * Sets up this panel's contents (labels, textfields, etc).
	 */
	protected void setupContents() {
		setLayout(new BorderLayout());

		JComponent[] fields = new JComponent[] {dbNameField = new JTextField(),
												dbDriverField = new JComboBox(getDriverClasses()),
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
		String[] tips = new String[] {"The name of this database", "The class name of the JDBC Driver", "Vendor-specific JDBC URL", "Username for this database", "Password for this database"};
		
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
		jdbcDrivers = new HashMap();
		jdbcDrivers.put("oracle.jdbc.driver.OracleDriver",
						"jdbc:oracle:thin:@<hostname>:1521:<instance>");
		jdbcDrivers.put("com.microsoft.jdbc.sqlserver.SQLServerDriver",
						"jdbc:microsoft:sqlserver://<hostname>:1433;SelectMethod=cursor");
		jdbcDrivers.put("org.postgresql.Driver",
						"jdbc:postgresql://<hostname>:5432");
		jdbcDrivers.put("ibm.sql.DB2Driver",
						"jdbc:db2:<hostname>");
	}

	// -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current DBConnectionSpec.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public void applyChanges() {
		dbcs.setName(dbNameField.getText());
		dbcs.setDisplayName(dbNameField.getText());
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
		dbNameField.setText(dbcs.getDisplayName());
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
