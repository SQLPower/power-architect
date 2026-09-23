/*
 * Copyright (c) 2026, SQL Power Group Inc.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ddl.DDLParser;
import ca.sqlpower.architect.ddl.DDLParser.DDLParseException;
import ca.sqlpower.architect.ddl.DDLParser.ParsedColumn;
import ca.sqlpower.architect.ddl.DDLParser.ParsedForeignKey;
import ca.sqlpower.architect.ddl.DDLParser.ParsedTable;
import ca.sqlpower.architect.ddl.DDLTableBuilder;
import ca.sqlpower.sqlobject.SQLTable;

/**
 * Dialog that lets the user paste one or more {@code CREATE TABLE} statements,
 * previews what was parsed and adds the resulting tables to the model.
 */
public class CreateTableFromDDLDialog extends JDialog {

    private static final Logger logger = Logger.getLogger(CreateTableFromDDLDialog.class);

    private final ArchitectSwingSession session;

    private final JTextArea inputArea = new JTextArea(18, 60);
    private final JTextArea previewArea = new JTextArea(18, 40);

    private List<ParsedTable> parsedTables;

    public CreateTableFromDDLDialog(ArchitectSwingSession session, Window owner) {
        super(owner, Messages.getString("CreateTableFromDDLDialog.title"), ModalityType.APPLICATION_MODAL); //$NON-NLS-1$
        this.session = session;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, inputArea.getFont().getSize()));
        inputArea.setText("CREATE TABLE example (\n    id INTEGER NOT NULL PRIMARY KEY,\n    name VARCHAR(50)\n);\n"); //$NON-NLS-1$
        previewArea.setEditable(false);
        previewArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, previewArea.getFont().getSize()));

        JPanel inputPanel = new JPanel(new BorderLayout(4, 4));
        inputPanel.add(new JLabel(Messages.getString("CreateTableFromDDLDialog.inputLabel")), BorderLayout.NORTH); //$NON-NLS-1$
        inputPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel previewPanel = new JPanel(new BorderLayout(4, 4));
        previewPanel.add(new JLabel(Messages.getString("CreateTableFromDDLDialog.previewLabel")), BorderLayout.NORTH); //$NON-NLS-1$
        previewPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, previewPanel);
        split.setResizeWeight(0.55);

        JButton parseButton = new JButton(Messages.getString("CreateTableFromDDLDialog.parseButton")); //$NON-NLS-1$
        parseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doParse();
            }
        });
        JButton createButton = new JButton(Messages.getString("CreateTableFromDDLDialog.createButton")); //$NON-NLS-1$
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doCreate();
            }
        });
        JButton closeButton = new JButton(Messages.getString("CreateTableFromDDLDialog.closeButton")); //$NON-NLS-1$
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(parseButton);
        buttonPanel.add(createButton);
        buttonPanel.add(closeButton);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.add(new JLabel(Messages.getString("CreateTableFromDDLDialog.instructions")), BorderLayout.NORTH); //$NON-NLS-1$
        content.add(split, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(content);
        setMinimumSize(new Dimension(800, 500));
    }

    /**
     * Parses the input and fills the preview. Returns true if parsing succeeded.
     */
    private boolean doParse() {
        try {
            parsedTables = new DDLParser(inputArea.getText()).parse();
            previewArea.setText(buildPreview(parsedTables));
            previewArea.setCaretPosition(0);
            if (parsedTables.isEmpty()) {
                previewArea.setText(Messages.getString("CreateTableFromDDLDialog.noTables")); //$NON-NLS-1$
                return false;
            }
            return true;
        } catch (DDLParseException ex) {
            parsedTables = null;
            previewArea.setText(Messages.getString("CreateTableFromDDLDialog.parseError") + ex.getMessage()); //$NON-NLS-1$
            return false;
        }
    }

    private void doCreate() {
        if (!doParse()) {
            JOptionPane.showMessageDialog(this,
                    Messages.getString("CreateTableFromDDLDialog.nothingToCreate"), //$NON-NLS-1$
                    Messages.getString("CreateTableFromDDLDialog.errorTitle"), //$NON-NLS-1$
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            session.getWorkspace().begin(Messages.getString("CreateTableFromDDLDialog.undoName")); //$NON-NLS-1$
            List<SQLTable> tables = new DDLTableBuilder().buildTables(parsedTables);
            Point location = new Point(20, 20);
            for (SQLTable table : tables) {
                session.getTargetDatabase().addChild(table);
                TablePane tp = new TablePane(table, session.getPlayPen().getContentPane());
                session.getPlayPen().addTablePane(tp, location);
                location.translate(40, 40);
            }
            session.getWorkspace().commit();
            dispose();
        } catch (Exception ex) {
            logger.error("Failed to create tables from DDL", ex); //$NON-NLS-1$
            try {
                session.getWorkspace().rollback(Messages.getString("CreateTableFromDDLDialog.undoName")); //$NON-NLS-1$
            } catch (Exception rollbackEx) {
                logger.warn("Rollback failed", rollbackEx); //$NON-NLS-1$
            }
            ASUtils.showExceptionDialog(session,
                    Messages.getString("CreateTableFromDDLDialog.errorCreating"), ex); //$NON-NLS-1$
        }
    }

    private String buildPreview(List<ParsedTable> tables) {
        StringBuilder sb = new StringBuilder();
        for (ParsedTable table : tables) {
            sb.append("TABLE ").append(table.name).append('\n'); //$NON-NLS-1$
            for (ParsedColumn col : table.columns) {
                sb.append("    ").append(col.name).append(' ').append(col.typeName); //$NON-NLS-1$
                if (!col.nullable) sb.append(" NOT NULL"); //$NON-NLS-1$
                if (col.primaryKey) sb.append(" PRIMARY KEY"); //$NON-NLS-1$
                if (col.defaultValue != null) sb.append(" DEFAULT ").append(col.defaultValue); //$NON-NLS-1$
                sb.append('\n');
            }
            for (ParsedForeignKey fk : table.foreignKeys) {
                sb.append("    FOREIGN KEY ").append(fk.columns) //$NON-NLS-1$
                  .append(" REFERENCES ").append(fk.referencedTable) //$NON-NLS-1$
                  .append(' ').append(fk.referencedColumns).append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}