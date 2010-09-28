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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.text.Document;

import ca.sqlpower.architect.ddl.critic.CriticismBucket;
import ca.sqlpower.architect.ddl.critic.CriticFix;
import ca.sqlpower.architect.ddl.critic.CriticAndSettings.Severity;
import ca.sqlpower.architect.diff.SQLObjectComparator;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.swingui.table.FancyExportableJTable;

public class CriticSwingUtil {
    
    /**
     * Error icon to go along with criticisms that are flagged to be errors.
     */
    public static final ImageIcon ERROR_ICON = SPSUtils.createIcon("error", "error badge");
    
    /**
     * Warning icon to go along with criticisms that are flagged to be warnings.
     */
    public static final ImageIcon WARNING_ICON = SPSUtils.createIcon("warning", "warning badge");

    private CriticSwingUtil() {
        //utility class
    }

    /**
     * Returns a table that displays all of the critics in the system including
     * letting users be able to apply quick fixes to criticisms.
     * 
     * @param session
     *            The session that contains the critic manager and its settings.
     * @param bucket
     *            The bucket that stores the critics in the system. As this
     *            bucket is updated the table model will update with it.
     */
    public static FancyExportableJTable createCriticTable(ArchitectSwingSession session, CriticismBucket bucket) {
        return createCriticTable(session, bucket, null);
    }

    /**
     * Returns a table that displays all of the critics in the system including
     * letting users be able to apply quick fixes to criticisms.
     * 
     * @param session
     *            The session that contains the critic manager and its settings.
     * @param bucket
     *            The bucket that stores the critics in the system. As this
     *            bucket is updated the table model will update with it.
     * @param searchDoc
     *            A document that is used to retrieve text from to limit the
     *            number of rows displayed by the table to only the rows that
     *            contain this text somewhere in it. If this is null no search
     *            document will be used and the table will always display all of
     *            the results.
     */
    public static FancyExportableJTable createCriticTable(ArchitectSwingSession session, CriticismBucket bucket, Document searchDoc) {
        final CriticismTableModel tableModel = new CriticismTableModel(session, bucket);
        final FancyExportableJTable errorTable = new FancyExportableJTable(tableModel, searchDoc);
        errorTable.setDefaultRenderer(Severity.class, new SeverityTableCellRenderer());
        final QuickFixListCellRenderer renderer = new QuickFixListCellRenderer();
        errorTable.setDefaultRenderer(List.class, renderer);
        errorTable.setDefaultRenderer(Object.class, new CriticismObjectRenderer());
        errorTable.setAlignmentX(JTable.LEFT_ALIGNMENT);
        
        //Sorts the objects by their name. This can be more fancy if we desire in 
        //the future but works as a decent default for now.
        errorTable.getTableModelSortDecorator().setColumnComparator(SPObject.class, 
                new SQLObjectComparator());
        errorTable.addMouseListener(new MouseListener() {
            
            public void mouseReleased(MouseEvent e) {
                final Point point = e.getPoint();
                int row = errorTable.rowAtPoint(point);
                int col = errorTable.columnAtPoint(point);
                Object clickedVal = tableModel.getValueAt(row, col);
                if (clickedVal instanceof List<?>) {
                    List<?> list = (List<?>) clickedVal;
                    final JPopupMenu menu = new JPopupMenu();
                    for (Object o : list) {
                        final CriticFix fix = (CriticFix) o;
                        menu.add(new AbstractAction(fix.getDescription()) {
                            public void actionPerformed(ActionEvent e) {
                                fix.apply();
                            }
                        });
                    }
                    menu.show(errorTable, point.x, point.y);
                }
            }
            
            public void mousePressed(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseClicked(MouseEvent e) {}
        });
        return errorTable;
    }
}
