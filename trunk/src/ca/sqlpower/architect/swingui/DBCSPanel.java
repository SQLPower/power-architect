/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.sql.SPDataSourceType;
import ca.sqlpower.swingui.DataEntryPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DBCSPanel implements DataEntryPanel {

	private static final Logger logger = Logger.getLogger(DBCSPanel.class);

    /**
     * The panel that holds the GUI.
     */
    private final JPanel panel;
    
    /**
     * The data source we're editing.
     */
	private final SPDataSource dbcs;
    
	private JTextField dbNameField;
	private JComboBox dataSourceTypeBox;
	private PlatformSpecificConnectionOptionPanel platformSpecificOptions;
	private JTextField dbUrlField;
	private JTextField dbUserField;
	private JPasswordField dbPassField;

	public DBCSPanel(SPDataSource ds) {
	    this.dbcs = ds;
	    panel = buildGeneralPanel(ds);
        
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
        
	}

    /**
     * Builds and returns a Swing component that has all the general database
     * settings (the ones that are always required no matter what you want to
     * use this connection for).
     */
    private JPanel buildGeneralPanel(SPDataSource ds) {
        DataSourceCollection dsCollection = ds.getParentCollection();
        List<SPDataSourceType> dataSourceTypes = dsCollection.getDataSourceTypes();
        dataSourceTypes.add(0, new SPDataSourceType());
        dataSourceTypeBox = new JComboBox(dataSourceTypes.toArray());
        dataSourceTypeBox.setRenderer(new ArchitectDataSourceTypeListCellRenderer());
        dataSourceTypeBox.setSelectedIndex(0);
        
        dbNameField = new JTextField();
        dbNameField.setName("dbNameField");
        platformSpecificOptions = new PlatformSpecificConnectionOptionPanel(dbUrlField = new JTextField());

        //we know this should be set to pref but one of the components seems to be updating the
        //preferred size
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("pref, 4dlu, 0:grow")); 
        builder.append("Connection &Name", dbNameField);
        builder.append("&Database Type", dataSourceTypeBox);
        builder.append("Connect &Options", platformSpecificOptions.getPanel());
        builder.append("JDBC &URL", dbUrlField);
        builder.append("Use&rname", dbUserField = new JTextField());
        builder.append("&Password", dbPassField = new JPasswordField());
        
        dataSourceTypeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                SPDataSourceType parentType =
                    (SPDataSourceType) dataSourceTypeBox.getSelectedItem();
                platformSpecificOptions.setTemplate(parentType);
            }
        });
        
        // ensure enough width for the platform specific options
        JPanel p = builder.getPanel();
        p.setPreferredSize(new Dimension(600, 300));

        return p;
    }
    
    /**
     * Provides access to the combo box of data source types in this panel.
     * Some outside classes that need to collaborate with this panel need
     * to know when the user has selected a different data source type,
     * and if you've got one, you can use this method to get the combo box
     * and add an ItemListener to it.
     */
    public JComboBox getDataSourceTypeBox() {
        return dataSourceTypeBox;
    }
    
    /**
     * Returns a reference to the data source this panel is editing (that is,
     * the one that will be updated when apply() is called).
     */
    public SPDataSource getDbcs() {
        return dbcs;
    }

    
    // -------------------- DATE ENTRY PANEL INTERFACE -----------------------

	/**
	 * Copies the properties displayed in the various fields back into
	 * the current SPDataSource.  You still need to call getDbcs()
	 * and save the connection spec yourself.
	 */
	public boolean applyChanges() {
        
        dbNameField.setText(dbNameField.getText().trim());
        
        if ("".equals(dbNameField.getText())) {
            JOptionPane.showMessageDialog(panel,
                    "A connection name must have at least 1 character that is not whitespace");
            return false;
        }
        
        SPDataSource existingDSWithThisName = dbcs.getParentCollection().getDataSource(dbNameField.getText());
        if (existingDSWithThisName != null && existingDSWithThisName != dbcs) {
            JOptionPane.showMessageDialog(panel, "A connection with the name \"" +
                    dbNameField.getText() + "\" already exists");
            return false;
        }
        
        logger.debug("Applying changes...");
        
		String name = dbNameField.getText();
		dbcs.setName(name);
		dbcs.setDisplayName(name);
		dbcs.setParentType((SPDataSourceType) dataSourceTypeBox.getSelectedItem());
		dbcs.setUrl(dbUrlField.getText());
		dbcs.setUser(dbUserField.getText());
		dbcs.setPass(new String(dbPassField.getPassword())); // completely defeats the purpose for JPasswordField.getText() being deprecated, but we're saving passwords to the config file so it hardly matters.

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
