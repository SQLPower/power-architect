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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import ca.sqlpower.architect.ddl.critic.CriticismBucket;
import ca.sqlpower.architect.ddl.critic.CriticSettings.Severity;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.FancyExportableJTable;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * This panel can be placed somewhere on the main Architect frame to display all
 * of the currently known errors in the play pen based on enabled critics.
 */
public class CriticPanel {

    /**
     * Error icon to go along with criticisms that are flagged to be errors.
     */
    private static final ImageIcon ERROR_ICON = SPSUtils.createIcon("error", "error badge");
    
    /**
     * Warning icon to go along with criticisms that are flagged to be warnings.
     */
    private static final ImageIcon WARNING_ICON = SPSUtils.createIcon("warning", "warning badge");
    
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
                return ERROR_ICON;
            } else if (severity == Severity.WARNING) {
                return WARNING_ICON;
            } else {
                return null;
            }
        }
    };
    
    /**
     * The main panel of the critics window.
     */
    private JPanel panel;

    /**
     * The {@link CriticismBucket} that stays around for the life of the panel.
     * The criticisms in the panel can be updated in this bucket to be valid
     * criticisms of the current project.
     */
    private final CriticismBucket<SQLObject> criticismBucket;

    private final ArchitectSwingSession session;

    public CriticPanel(ArchitectSwingSession session) {
        this.session = session;
        criticismBucket = new CriticismBucket<SQLObject>();
        
        CriticismTableModel tableModel = new CriticismTableModel(session.getWorkspace(), 
                criticismBucket);
        FancyExportableJTable table = new FancyExportableJTable(tableModel);
        table.setDefaultRenderer(Severity.class, tableRenderer);
        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
    }        
    
    public void init() {
        ButtonBarBuilder buttonBar = new ButtonBarBuilder();
        buttonBar.addGridded(new JButton(new CriticizeAction(session)));
        
        panel.add(buttonBar.getPanel(), BorderLayout.NORTH);
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public CriticismBucket<SQLObject> getCriticismBucket() {
        return criticismBucket;
    }
    
}
