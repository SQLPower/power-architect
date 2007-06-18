package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.DataSourceCollection;

public class DBCSPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(DBCSPanel.class);

	private ArchitectDataSource dbcs;
	private TextPanel form;

	private JTextField dbNameField;
	private JComboBox dataSourceTypeBox;
	private PlatformSpecificConnectionOptionPanel platformSpecificOptions;
	private JTextField dbUrlField;
	private JTextField dbUserField;
	private JPasswordField dbPassField;
	private JTextField plSchemaField;
	private JTextField plDbTypeField;
	private JTextField odbcDSNField;

	public DBCSPanel(DataSourceCollection dsCollection) {
		setLayout(new BorderLayout());

        List<ArchitectDataSourceType> dataSourceTypes = dsCollection.getDataSourceTypes();
        dataSourceTypes.add(0, new ArchitectDataSourceType());
        dataSourceTypeBox = new JComboBox(dataSourceTypes.toArray());
        dataSourceTypeBox.setRenderer(new ArchitectDataSourceTypeListCellRenderer());
        dataSourceTypeBox.setSelectedIndex(0);
        
		dbNameField = new JTextField();
		dbNameField.setName("dbNameField");
		platformSpecificOptions = new PlatformSpecificConnectionOptionPanel(dbUrlField = new JTextField());

		JComponent[] fields = new JComponent[] {dbNameField,
                                                dataSourceTypeBox,
												platformSpecificOptions.getPanel(),
												dbUrlField,
												dbUserField = new JTextField(),
												dbPassField = new JPasswordField(),
												plSchemaField = new JTextField(),
												plDbTypeField = new JTextField(),
												odbcDSNField = new JTextField()};
		String[] labels = new String[] {"Connection Name",
										"Database Type",
										"Connect Options",
										"JDBC URL",
										"Username",
										"Password",
										"PL Schema Owner",
										"Database Type",
										"ODBC Data Source Name"};

		char[] mnemonics = new char[] {'n', 'd', 'o', 'u', 'r', 'p', 's', 't', 'b'};
		int[] widths = new int[] {40, 40, 40, 40, 40, 40, 40, 40, 40};
		String[] tips = new String[] {"Your nickname for this database",
									  "The database type (set up in user preferences)",
									  "Connection parameters specific to this driver",
									  "Vendor-specific JDBC URL",
									  "Username for this database",
									  "Password for this database",
									  "Qualifier to put before references to PL system tables",
									  "The Power*Loader type code for this database",
									  "The ODBC data source name for this database"};

		// update fields when user picks new driver
        dataSourceTypeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ArchitectDataSourceType parentType
                = (ArchitectDataSourceType) dataSourceTypeBox.getSelectedItem();
            platformSpecificOptions.setTemplate(parentType);
            plDbTypeField.setText(parentType.getPlDbType());
            }
		});


		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}


    // -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current ArchitectDataSource.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public boolean applyChanges() {
		String name = dbNameField.getText();
		dbcs.setName(name);
		dbcs.setDisplayName(name);
		dbcs.setParentType((ArchitectDataSourceType) dataSourceTypeBox.getSelectedItem());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(new String(dbPassField.getPassword())); // completely defeats the purpose for JPasswordField.getText() being deprecated, but we're saving passwords to the config file so it hardly matters.
		dbcs.setPlSchema(plSchemaField.getText());
		dbcs.setPlDbType(plDbTypeField.getText());
		dbcs.setOdbcDsn(odbcDSNField.getText());
		return true;
	}

	/**
	 * Does nothing right now.
	 */
	public void discardChanges() {
        // nothing to discard
	}

	/**
	 * Sets this DBCSPanel's fields to match those of the given dbcs,
	 * and stores a reference to the given dbcs so it can be updated
	 * when the applyChanges() method is called.
	 */
	public void setDbcs(ArchitectDataSource dbcs) {
		dbNameField.setText(dbcs.getName());
        // if this data source has no parent, it is a root data source
        if (dbcs.isParentSet()) {
            System.out.println("A PARENT! setting selected item to: \"" + dbcs.getParentType() + "\"");
            dataSourceTypeBox.setSelectedItem(dbcs.getParentType());
        } else {
            System.out.println("NO PARENT! setting selected item to: \"" + dbcs + "\"");
            dataSourceTypeBox.setSelectedItem(dbcs);
        }
        dbUrlField.setText(dbcs.getUrl());
		dbUserField.setText(dbcs.getUser());
		dbPassField.setText(dbcs.getPass());
		plSchemaField.setText(dbcs.getPlSchema());
		plDbTypeField.setText(dbcs.getPlDbType());
		odbcDSNField.setText(dbcs.getOdbcDsn());
		this.dbcs = dbcs;
	}

	/**
	 * Returns a reference to the current ArchitectDataSource (that is,
	 * the one that will be updated when apply() is called).
	 */
	public ArchitectDataSource getDbcs() {
		return dbcs;
	}


	public JPanel getPanel() {
		return this;
	}
}
