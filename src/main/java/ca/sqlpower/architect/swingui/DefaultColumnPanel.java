/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Types;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLType;
import ca.sqlpower.swingui.DataEntryPanel;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DefaultColumnPanel extends JPanel implements ActionListener, DataEntryPanel {
    private static final Logger logger = Logger.getLogger(DefaultColumnPanel.class);
    
    private static final Font TITLE_FONT = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 15f);

    private JTextField colName;

    private JComboBox colType;
    
    private JSpinner colPrec;
    
    private JSpinner colScale;
    
    private JCheckBox colInPK;
    
    private JCheckBox colNullable;
    
    private JCheckBox colAutoInc;
    
    private JTextArea colRemarks;
    
    private JTextArea colDefault;
 
    public DefaultColumnPanel(ArchitectSwingSessionContext context) {
        setUp();
        revertToUserSettings();
    }

    public void setUp(){
        FormLayout layout = new FormLayout(
                "30dlu, pref:grow, 20dlu, pref:grow, 30dlu",
               "");
        layout.setColumnGroups(new int[][] { { 2, 4 } } );
        CellConstraints cc = new CellConstraints();
        
        this.setLayout(layout);
        
        int row = 1;
        
        layout.appendRow(RowSpec.decode("10dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(makeTitle(Messages.getString("DefaultColumnPanel.explaination")), cc.xyw(2, row++, 3)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JLabel(Messages.getString("DefaultColumnPanel.name")),cc.xy(2,row));
        add(new JLabel(Messages.getString("DefaultColumnPanel.type")), cc.xy(4, row++)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(colName = new JTextField(), cc.xy(2, row));
        colName.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                colName.requestFocusInWindow();
            }
        });
        colName.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("focus Gained : " + e);
                }
                colName.selectAll();
            }
        });
        
        add(colType = new JComboBox(SQLType.getTypes()), cc.xy(4, row++));
        colType.setSelectedItem(null);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JLabel(Messages.getString("DefaultColumnPanel.precision")), cc.xy(2, row)); //$NON-NLS-1$
        add(new JLabel(Messages.getString("DefaultColumnPanel.scale")), cc.xy(4, row++)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;

        layout.appendRow(RowSpec.decode("p"));
        add(colPrec = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)), cc.xy(2, row));
        SPSUtils.makeJSpinnerSelectAllTextOnFocus(colPrec);
        
        add(colScale = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1)), cc.xy(4, row++));
        SPSUtils.makeJSpinnerSelectAllTextOnFocus(colScale);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(colInPK = new JCheckBox(Messages.getString("DefaultColumnPanel.inPrimaryKey")), cc.xyw(2, row++, 3)); //$NON-NLS-1$
        colInPK.addActionListener(this);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(colNullable = new JCheckBox(Messages.getString("DefaultColumnPanel.allowsNulls")), cc.xyw(2, row++, 3)); //$NON-NLS-1$
        colNullable.addActionListener(this);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(colAutoInc = new JCheckBox(Messages.getString("DefaultColumnPanel.autoIncrement")), cc.xyw(2, row++, 3)); //$NON-NLS-1$
        colAutoInc.addActionListener(this);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JLabel(Messages.getString("DefaultColumnPanel.remarks")), cc.xyw(2, row++, 4)); //$NON-NLS-1$
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JScrollPane(colRemarks = new JTextArea()), cc.xyw(2, row++, 3, "fill, fill"));
        colRemarks.setRows(8);
        colRemarks.setLineWrap(true);
        colRemarks.setWrapStyleWord(true);

        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JLabel(Messages.getString("DefaultColumnPanel.default")),cc.xyw(2, row++, 4));
        
        layout.appendRow(RowSpec.decode("5dlu"));
        row++;
        
        layout.appendRow(RowSpec.decode("p"));
        add(new JScrollPane(colDefault = new JTextArea()),cc.xyw(2, row++, 3));
        colDefault.setLineWrap(false);
        
        layout.appendRow(RowSpec.decode("15dlu"));
        row++;
        
        //TODO only give focus to column name if it's enabled?
        colName.requestFocus();
        colName.selectAll();
        
    }
    
    protected void revertToUserSettings() {
        logger.debug("Reverting to default settings");
        colName.setText(SQLColumn.getDefaultName());
        colPrec.setValue(SQLColumn.getDefaultPrec());
        colType.setSelectedItem(SQLType.getType(SQLColumn.getDefaultType()));
        colScale.setValue(SQLColumn.getDefaultScale());
        colInPK.setSelected(SQLColumn.isDefaultInPK());
        colNullable.setSelected(SQLColumn.isDefaultNullable());
        colAutoInc.setSelected(SQLColumn.isDefaultAutoInc());
        colRemarks.setText(SQLColumn.getDefaultRemarks());
        colDefault.setText(SQLColumn.getDefaultForDefaultValue());
        updateComponents();
     }
    
    public boolean applyChanges() {
        logger.debug("DefaultColumnPanel applyChanges");
        logger.debug(colName.getText().trim().equals(""));
        SQLColumn.setDefaultName(colName.getText());
        if (colType.getSelectedItem() != null) {
            SQLColumn.setDefaultType(((SQLType)colType.getSelectedItem()).getType());
        } else {
            SQLColumn.setDefaultType(Types.VARCHAR);
        }
        SQLColumn.setDefaultPrec((Integer)colPrec.getValue());
        SQLColumn.setDefaultScale((Integer)colScale.getValue());
        SQLColumn.setDefaultInPK(colInPK.isSelected());
        SQLColumn.setDefaultNullable(colNullable.isSelected());
        SQLColumn.setDefaultAutoInc(colAutoInc.isSelected());
        SQLColumn.setDefaultRemarks(colRemarks.getText());
        SQLColumn.setDefaultForDefaultValue(colDefault.getText());
        return true;
    }

    public void discardChanges() {
        revertToUserSettings();
    }

    public JComponent getPanel() {
        return this;
    }

    public boolean hasUnsavedChanges() {
        // TODO return whether this pane has been changed
        return true;
    }

    private Component makeTitle(String string) {
        JLabel label = new JLabel(string);
        label.setFont(TITLE_FONT);
        return label;
    }

    /**
     * Implementation of ActionListener.
     */
    public void actionPerformed(ActionEvent e) {
        logger.debug("action event " + e); //$NON-NLS-1$
        updateComponents();
    }
    

    /**
     * Examines the components and makes sure they're in a consistent state
     * (they are legal with respect to the model).
     */
    private void updateComponents() {
        // allow nulls is free unless column is in PK
        if (colInPK.isSelected()) {
            colNullable.setEnabled(false);
        } else {
            colNullable.setEnabled(true);
        }

        // primary key is free unless column allows nulls
        if (colNullable.isSelected()) {
            colInPK.setEnabled(false);
        } else {
            colInPK.setEnabled(true);
        }

        if (colInPK.isSelected() && colNullable.isSelected()) {
            // this should not be physically possible
            colNullable.setSelected(false);
            colNullable.setEnabled(false);
        }
        if (colAutoInc.isSelected()) {
            colDefault.setText(""); //$NON-NLS-1$
            colDefault.setEnabled(false);
        } else {
            colDefault.setEnabled(true);
        }
    }

}
