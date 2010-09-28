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

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.swingui.AbstractNoEditDataEntryPanel;

public class AboutPanel extends AbstractNoEditDataEntryPanel {

	public JLabel content;

	public AboutPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("About", initAboutTab());
        tabs.add("System Properties", new JScrollPane(initSysPropsTab()));

        tabs.setSelectedIndex(0);
        add(tabs);
	}

    private JComponent initAboutTab() {
        JPanel pan = new JPanel();
		pan.setLayout(new FlowLayout());

        // Include the Power*Architect Icon!
        String realPath = "/icons/architect128.png";
        java.net.URL imgURL = ASUtils.class.getResource(realPath);

        if (imgURL != null) {
            ImageIcon imageIcon = new ImageIcon(imgURL, "Architect Logo");
            pan.add(new JLabel(imageIcon));
        }

		content = new JLabel("<html>Power*Architect "+
		                    ArchitectVersion.APP_VERSION+"<br><br>" +
							"Copyright 2003-2007 SQL Power Group Inc.<br>" +
							"</html>");
		pan.add(content);
        return pan;
	}

    /**
     * A JTable model to display the system properties
     */
    private class SystemPropertiesTableModel extends AbstractTableModel {
        private Properties props = System.getProperties();
        private String[] keys;

        SystemPropertiesTableModel() {
            keys = props.keySet().toArray(new String[0]);
            Arrays.sort(keys);
        }

        public int getRowCount() {
            return keys.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int row, int column) {
            switch(column) {
            case 0: return keys[row];
            case 1: return props.get(keys[row]);
            default: throw new IllegalArgumentException("Column count");
            }
        }

        @Override
        public String getColumnName(int column) {
            switch(column) {
            case 0: return "Property";
            case 1: return "Value";
            default: throw new IllegalArgumentException("Column count");
            }
        }
    }

    private JComponent initSysPropsTab() {
        JTable table = new JTable(new SystemPropertiesTableModel());

        // tailor column 1 width
        table.getColumnModel().getColumn(0).setMinWidth(200);

        // Want column 2 wide enough to show CLASSPATH
        table.getColumnModel().getColumn(1).setMinWidth(2000);

        table.setSize(table.getPreferredSize());

        return table;
    }
}