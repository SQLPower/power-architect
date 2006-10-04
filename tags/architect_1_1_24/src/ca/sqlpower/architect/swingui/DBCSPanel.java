package ca.sqlpower.architect.swingui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectUtils;

public class DBCSPanel extends JPanel implements ArchitectPanel {

	private static final Logger logger = Logger.getLogger(DBCSPanel.class);

	private ArchitectDataSource dbcs;
	private TextPanel form;

	private JTextField dbNameField;
	private JComboBox dbDriverField;
	private JComponent platformSpecificOptions;
	private JTextField dbUrlField;
	private JTextField dbUserField;
	private JPasswordField dbPassField;
	private JTextField plSchemaField;
	private JComboBox plDbTypeField;
	private JTextField odbcDSNField;

	private Map<String,String> jdbcDrivers;
	private Map<String,String> jdbcSystems;

	private JDBCURLUpdater urlUpdater = new JDBCURLUpdater();

	private boolean updatingUrlFromFields = false;
	private boolean updatingFieldsFromUrl = false;

	public DBCSPanel() {
		setLayout(new BorderLayout());

		dbDriverField = new JComboBox(getDriverClasses());
		dbDriverField.insertItemAt("", 0);
		dbNameField = new JTextField();
		dbNameField.setName("dbNameField");
		platformSpecificOptions = new JPanel();
		platformSpecificOptions.setLayout(new PlatformOptionsLayout());
		platformSpecificOptions.setBorder(BorderFactory.createEmptyBorder());
		platformSpecificOptions.add(new JLabel("(No options for current driver)"));

		JComponent[] fields = new JComponent[] {dbNameField,
												dbDriverField,
												platformSpecificOptions,
												dbUrlField = new JTextField(),
												dbUserField = new JTextField(),
												dbPassField = new JPasswordField(),
												plSchemaField = new JTextField(),
												plDbTypeField = new JComboBox(getDriverTypes()),
												odbcDSNField = new JTextField()};
		String[] labels = new String[] {"Connection Name",
										"JDBC Driver",
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
									  "The class name of the JDBC Driver",
									  "Connection parameters specific to this driver",
									  "Vendor-specific JDBC URL",
									  "Username for this database",
									  "Password for this database",
									  "Qualifier to put before references to PL system tables",
									  "The Power*Loader type code for this database",
									  "The ODBC data source name for this database"};

		// update url and type field when user picks new driver
		dbDriverField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	String driverField;
		    	String typeField;
		    	HashMap driverToType = (HashMap)ArchitectUtils.getDriverTypeMap();
		        createFieldsFromTemplate();
		        updateUrlFromFields();

		        driverField = (String) dbDriverField.getSelectedItem();
		        typeField =(String) driverToType.get(driverField);
		        plDbTypeField.setSelectedItem(typeField);

		    }
		});

		dbUrlField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
		        updateFieldsFromUrl();
            }

            public void removeUpdate(DocumentEvent e) {
		        updateFieldsFromUrl();
            }

            public void changedUpdate(DocumentEvent e) {
		        updateFieldsFromUrl();
            }
		});

		form = new TextPanel(fields, labels, mnemonics, widths, tips);
		add(form, BorderLayout.CENTER);
	}

	/** Returns all of the driver class names this dialog knows about. */
	private String[] getDriverClasses() {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return jdbcDrivers.keySet().toArray(new String[0]);
	}

	/** Returns all of the driver system names this dialog knows about. */
	private String[] getDriverTypes() {
		if (jdbcSystems == null) {
			setupDriverSystemMap();
		}
		return jdbcSystems.values().toArray(new String[0]);
	}

	/** Returns the JDBC URL template associated with the named driver. */
	private String getTemplateForDriver(String driverClassName) {
		if (jdbcDrivers == null) {
			setupDriverMap();
		}
		return (String) jdbcDrivers.get(driverClassName);
	}

	/**
	 * Sets up the platformSpecificOptions component to contain labels and
	 * text fields associated with each variable in the current template.
	 */
	private void createFieldsFromTemplate() {
        for (int i = 0; i < platformSpecificOptions.getComponentCount(); i++) {
            Component c = platformSpecificOptions.getComponent(i);
            if (c instanceof JTextField) {
                ((JTextField) c).getDocument().removeDocumentListener(urlUpdater);
            }
        }
        platformSpecificOptions.removeAll();

	    String driverClassName = dbDriverField.getSelectedItem().toString();
	    String template = getTemplateForDriver(driverClassName);
	    if (template != null) {
	        Pattern varPattern = Pattern.compile("<(.*?)>");
	        Matcher varMatcher = varPattern.matcher(template);
	        List<String> templateVars = new ArrayList<String>();
	        while (varMatcher.find()) {
	            templateVars.add(varMatcher.group(1));
	        }
	        logger.debug("Found variables: "+templateVars);

	        for(String var : templateVars) {
	            String def = "";
	            if (var.indexOf(':') != -1) {
	                int i = var.indexOf(':');
	                def = var.substring(i+1);
	                var = var.substring(0, i);
	            }

	            platformSpecificOptions.add(new JLabel(var));
	            JTextField field = new JTextField(def);
	            platformSpecificOptions.add(field);
	            field.getDocument().addDocumentListener(urlUpdater);
	        }


	    } else {
	        platformSpecificOptions.add(new JLabel("Unknown driver class.  Fill in URL manually."));

	    }

	    platformSpecificOptions.revalidate();
	    platformSpecificOptions.repaint();
	}

	/**
	 *
	 */
	private void setupDriverMap() {
		jdbcDrivers = ArchitectUtils.getDriverTemplateMap();
	}

	/**
	 *
	 */
	private void setupDriverSystemMap() {
		jdbcSystems = ArchitectUtils.getDriverTypeMap();
	}

	/**
	 * Copies the values from the platform-specific url fields into the main
	 * url.
	 */
    private void updateUrlFromFields() {
        if (updatingFieldsFromUrl) return;
        String template = getTemplateForDriver(dbDriverField.getSelectedItem().toString());
        if (template == null) return;
        try {
            updatingUrlFromFields = true;
            StringBuffer newUrl = new StringBuffer();
            Pattern p = Pattern.compile("<(.*?)>");
            Matcher m = p.matcher(template);
            while (m.find()) {
                String varName = m.group(1);
                if (varName.indexOf(':') != -1) {
                    varName = varName.substring(0, varName.indexOf(':'));
                }
                String varValue = getPlatformSpecificFieldValue(varName);
                m.appendReplacement(newUrl, varValue);
            }
            m.appendTail(newUrl);
            dbUrlField.setText(newUrl.toString());
        } finally {
            updatingUrlFromFields = false;
        }
    }

    /**
     * Parses the main url against the current template (if possible) and fills in the
     * individual fields with the values it finds.
     */
    private void updateFieldsFromUrl() {
        if (updatingUrlFromFields) return;
        try {
            updatingFieldsFromUrl = true;

            for (int i = 0; i < platformSpecificOptions.getComponentCount(); i++) {
                platformSpecificOptions.getComponent(i).setEnabled(true);
            }

            String template = getTemplateForDriver(dbDriverField.getSelectedItem().toString());
            logger.debug("Updating based on template "+template);
            if (template == null) return;
            String reTemplate = template.replaceAll("<.*?>", "(.*)");
            logger.debug("Regex of template is "+reTemplate);
            Pattern p = Pattern.compile(reTemplate);
            Matcher m = p.matcher(dbUrlField.getText());
            if (m.find()) {
                platformSpecificOptions.setEnabled(true);
                for (int g = 1; g <= m.groupCount(); g++) {
                    ((JTextField) platformSpecificOptions.getComponent(2*g-1)).setText(m.group(g));
                }
            } else {
                for (int i = 0; i < platformSpecificOptions.getComponentCount(); i++) {
                    platformSpecificOptions.getComponent(i).setEnabled(false);
                }
            }
        } finally {
            updatingFieldsFromUrl = false;
        }
    }

	/**
	 * Retrieves the named platform-specific option by looking it up in the
	 * platformSpecificOptions component.
     */
    private String getPlatformSpecificFieldValue(String varName) {
        // we're looking for the contents of the JTextField that comes after a JLabel with the same text as varName
        for (int i = 0; i < platformSpecificOptions.getComponentCount(); i++) {
            if (platformSpecificOptions.getComponent(i) instanceof JLabel
                    && ((JLabel) platformSpecificOptions.getComponent(i)).getText().equals(varName)
                    && platformSpecificOptions.getComponentCount() >= i+1) {
                return ((JTextField) platformSpecificOptions.getComponent(i+1)).getText();
            }
        }
        return "";
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
		dbcs.setDriverClass(dbDriverField.getSelectedItem().toString());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(new String(dbPassField.getPassword())); // completely defeats the purpose for JPasswordField.getText() being deprecated, but we're saving passwords to the config file so it hardly matters.
		dbcs.setPlSchema(plSchemaField.getText());
		dbcs.setPlDbType((String) plDbTypeField.getSelectedItem());
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
		plSchemaField.setText(dbcs.getPlSchema());
		plDbTypeField.setSelectedItem(dbcs.getPlDbType());
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

	private class JDBCURLUpdater implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }

        public void removeUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }

        public void changedUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }
	}

	private static class PlatformOptionsLayout implements LayoutManager {

	    /** The number of pixels to leave before each label except the first one. */
	    int preLabelGap = 10;

	    /** The number of pixels to leave between every component. */
	    int gap = 5;

	    public void addLayoutComponent(String name, Component comp) {
            // nothing to do
        }

        public void removeLayoutComponent(Component comp) {
            // nothing to do
        }

        public Dimension preferredLayoutSize(Container parent) {
            int height = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                height = Math.max(height, c.getPreferredSize().height);
            }
            return new Dimension(parent.getWidth(), height);
        }

        public Dimension minimumLayoutSize(Container parent) {
            int height = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                height = Math.max(height, c.getMinimumSize().height);
            }
            return new Dimension(parent.getWidth(), height);
        }

        public void layoutContainer(Container parent) {

            // compute total width of all labels
            int labelSize = 0;
            int labelCount = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (c instanceof JLabel) {
                    if (i > 0) labelSize += preLabelGap;
                    labelSize += c.getPreferredSize().width;
                    labelCount += 1;
                }
            }

            int gapSize = gap * (parent.getComponentCount() - 1);

            // compute how wide each non-label component should be (if there are any non-labels)
            int nonLabelWidth = 0;
            if (parent.getComponentCount() != labelCount) {
                nonLabelWidth = (parent.getWidth() - labelSize - gapSize) / (parent.getComponentCount() - labelCount);
            }

            // impose a minimum so the non-labels at least show up when we're tight on space
            if (nonLabelWidth < 20) {
                nonLabelWidth = 20;
            }

            // lay out the container
            int x = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);

                if (i > 0) x += gap;

                if (c instanceof JLabel) {
                    if (i > 0) x += preLabelGap;
                    c.setBounds(x, 0, c.getPreferredSize().width, parent.getHeight());
                    x += c.getPreferredSize().width;
                } else {
                    c.setBounds(x, 0, nonLabelWidth, parent.getHeight());
                    x += nonLabelWidth;
                }
            }
        }
	}

	public JPanel getPanel() {
		return this;
	}
}
