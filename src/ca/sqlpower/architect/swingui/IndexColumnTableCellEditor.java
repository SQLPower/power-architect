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

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
/**
 *  Creates an editor for the  
 *
 *  Note: This class only works with a table with a IndexColumnTableModel
 */
public class IndexColumnTableCellEditor extends DefaultCellEditor implements TableCellEditor {
    Component component;
    
    public IndexColumnTableCellEditor(JCheckBox checkBox) {
        super(checkBox);
    }

    public IndexColumnTableCellEditor(JComboBox comboBox) {
        super(comboBox);
    }

    public IndexColumnTableCellEditor(JTextField textField) {
        super(textField);
    }
    
    public IndexColumnTableCellEditor() {
        super(new JComboBox());
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        IndexColumnTableModel tm = (IndexColumnTableModel) table.getModel();
        try {
            SQLColumnComboBoxModel columnComboBoxModel = new SQLColumnComboBoxModel(tm.getTable(), tm.getUsedColumns());
            component = new JComboBox(columnComboBoxModel);
            columnComboBoxModel.setSelectedItem(value);
            return component;
        } catch (ArchitectException e) {
            throw new ArchitectRuntimeException(e);
        }
    }

    public Object getCellEditorValue() {
       return ((JComboBox)component).getModel().getSelectedItem();
    }
    
    @Override
    public Component getComponent() {
        return component;
    }
}
