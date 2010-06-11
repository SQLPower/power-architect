/*
 * Copyright (c) 2010, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui.critic;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ca.sqlpower.architect.ddl.critic.Criticism;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.table.FancyExportableJTable;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * This panel can be placed somewhere on the main Architect frame to display all
 * of the currently known errors in the play pen based on enabled critics.
 */
public class CriticPanel {

    /**
     * A cell renderer that can display badges with the criticisms.
     */
    private TableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
        
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, 
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof Severity) {
                return new JLabel(getIcon((Severity) value));
            } else {
                return this.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
        
        private ImageIcon getIcon(Severity severity) {
            if (severity == Severity.ERROR) {
                return CriticSwingUtil.ERROR_ICON;
            } else if (severity == Severity.WARNING) {
                return CriticSwingUtil.WARNING_ICON;
            } else {
                return null;
            }
        }
    };
    
    /**
     * The main panel of the critics window.
     */
    private JPanel panel;

    private final ArchitectSwingSession session;

    private final FancyExportableJTable table;
    
    private final ListSelectionListener selectedObjectsChangedListener = new ListSelectionListener() {
    
        public void valueChanged(ListSelectionEvent e) {
            selectSubjectInPlayPen();
        }
    };
    
    public CriticPanel(ArchitectSwingSession session) {
        this.session = session;
        
        CriticismTableModel tableModel = new CriticismTableModel(session, session.getPlayPen().getCriticismBucket());
        table = new FancyExportableJTable(tableModel);
        table.setDefaultRenderer(Severity.class, tableRenderer);
        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(selectedObjectsChangedListener);
        
    }        
    
    public void init() {
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        buttonBar.addGridded(new JButton(new CriticizeAction(session)));
        
        panel.add(buttonBar.getPanel(), BorderLayout.NORTH);
    }
    
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Selects an object in the play pen that represents the subject of the
     * currently selected criticism in the table.
     */
    private void selectSubjectInPlayPen() {
        List<SQLObject> criticizedObjects = new ArrayList<SQLObject>();
        for (int i : table.getSelectedRows()) {
            Criticism criticism = session.getPlayPen().getCriticismBucket().getCriticisms().get(i);
            if (criticism.getSubject() instanceof SQLObject) {
                criticizedObjects.add((SQLObject) criticism.getSubject());
            }
        }
        try {
            session.getPlayPen().selectObjects(criticizedObjects);
        } catch (SQLObjectException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void selectCriticisms(List<Criticism> selectMe) {
        table.clearSelection();
        for (Criticism criticism : selectMe) {
            int index = session.getPlayPen().getCriticismBucket().getCriticisms().indexOf(criticism);
            table.changeSelection(index, index, true, false);
        }
    }
}
