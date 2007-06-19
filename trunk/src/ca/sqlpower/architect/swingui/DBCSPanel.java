package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectDataSourceType;
import ca.sqlpower.architect.DataSourceCollection;
import ca.sqlpower.architect.etl.kettle.KettleOptions;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DBCSPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(DBCSPanel.class);

    private JPanel panel;
    
    private JTabbedPane tabbedPane;
    
	private ArchitectDataSource dbcs;
    
	private JTextField dbNameField;
	private JComboBox dataSourceTypeBox;
	private PlatformSpecificConnectionOptionPanel platformSpecificOptions;
	private JTextField dbUrlField;
	private JTextField dbUserField;
	private JPasswordField dbPassField;

    private JTextField kettleLogin;
    private JPasswordField kettlePassword;

    
	public DBCSPanel(DataSourceCollection dsCollection) {
    
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("General", buildGeneralPanel(dsCollection));
        tabbedPane.addTab("Kettle", buildKettleOptionsPanel());
        
        panel = new JPanel(new BorderLayout());
        panel.add(tabbedPane, BorderLayout.CENTER);
	}

    /**
     * Builds and returns a Swing component that has all the general database
     * settings (the ones that are always required no matter what you want to
     * use this connection for).
     */
    private JPanel buildGeneralPanel(DataSourceCollection dsCollection) {
        List<ArchitectDataSourceType> dataSourceTypes = dsCollection.getDataSourceTypes();
        dataSourceTypes.add(0, new ArchitectDataSourceType());
        dataSourceTypeBox = new JComboBox(dataSourceTypes.toArray());
        dataSourceTypeBox.setRenderer(new ArchitectDataSourceTypeListCellRenderer());
        dataSourceTypeBox.setSelectedIndex(0);
        
        dbNameField = new JTextField();
        dbNameField.setName("dbNameField");
        platformSpecificOptions = new PlatformSpecificConnectionOptionPanel(dbUrlField = new JTextField());

        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, 0:grow"));
        builder.append("Connection &Name", dbNameField);
        builder.append("&Database Type", dataSourceTypeBox);
        builder.append("Connect &Options", platformSpecificOptions.getPanel());
        builder.append("JDBC &URL", dbUrlField);
        builder.append("Use&rname", dbUserField = new JTextField());
        builder.append("&Password", dbPassField = new JPasswordField());
        
        // update fields when user picks new driver
        dataSourceTypeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                ArchitectDataSourceType parentType =
                    (ArchitectDataSourceType) dataSourceTypeBox.getSelectedItem();
                platformSpecificOptions.setTemplate(parentType);
            }
        });
        
        // ensure enough width for the platform specific options
        JPanel p = builder.getPanel();
        p.setPreferredSize(new Dimension(600, 300));

        return p;
    }

    /**
     * Creates a GUI panel for options which are required for interacting
     * with Kettle, and are not already covered on the general pref panel.
     */
    private JPanel buildKettleOptionsPanel() {
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, pref:grow"));
        builder.append("Repository Login &Name", kettleLogin = new JTextField());
        builder.append("Repository &Password", kettlePassword = new JPasswordField());
        return builder.getPanel();
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
        
        kettleLogin.setText(dbcs.get(KettleOptions.KETTLE_REPOS_LOGIN_KEY));
        kettlePassword.setText(dbcs.get(KettleOptions.KETTLE_REPOS_PASSWORD_KEY));
        
        this.dbcs = dbcs;
    }

    /**
     * Returns a reference to the current ArchitectDataSource (that is,
     * the one that will be updated when apply() is called).
     */
    public ArchitectDataSource getDbcs() {
        return dbcs;
    }

    /**
     * Returns the current contents of the Database Name text field (on the
     * "General" tab).  Apparently some users of this class want to do some
     * pre-checking before applyChanges(), and this is the hook that enables
     * the pre-checking.
     */
    public String getDbNameFieldContents() {
        return dbNameField.getText();
    }

    
    // -------------------- ARCHITECT PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current ArchitectDataSource.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public boolean applyChanges() {
        
        logger.debug("Applying changes...");
        
		String name = dbNameField.getText();
		dbcs.setName(name);
		dbcs.setDisplayName(name);
		dbcs.setParentType((ArchitectDataSourceType) dataSourceTypeBox.getSelectedItem());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(new String(dbPassField.getPassword())); // completely defeats the purpose for JPasswordField.getText() being deprecated, but we're saving passwords to the config file so it hardly matters.

        dbcs.put(KettleOptions.KETTLE_REPOS_LOGIN_KEY, kettleLogin.getText());
        dbcs.put(KettleOptions.KETTLE_REPOS_PASSWORD_KEY, new String(kettlePassword.getPassword()));

        return true;
	}

	/**
	 * Does nothing right now, because there is nothing to discard or clean up.
	 */
	public void discardChanges() {
        // nothing to discard
	}

    /**
     * Returns the panel that holds the user interface for the datatbase
     * connection settings.
     */
    public JPanel getPanel() {
        return panel;
    }
}
