/*
 * Created on Nov 28, 2006
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectSession;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.sql.DataMover;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class DataMoverPanel {
    
    /**
     * The panel that holds the UI.
     */
    private JPanel panel;
    
    /**
     * Tree of source objects that you might want to copy.
     */
    private JTree sourceTree;
    
    /**
     * Tree of target objects that you can copy into.
     */
    private JTree destTree;
    
    /**
     * Checkbox for selecting whether or not to wipe out the destination
     * table's contents before loading.
     */
    private JCheckBox truncateDestinationTableBox;
    
    public DataMoverPanel(ArchitectSession session) throws ArchitectException {
        List<SQLDatabase> dblist = new ArrayList<SQLDatabase>();
        for (ArchitectDataSource ds : session.getUserSettings().getConnections()) {
            dblist.add(new SQLDatabase(ds));
        }
        
        sourceTree = new JTree(new DBTreeModel(dblist));
        sourceTree.setRootVisible(false);
        sourceTree.setShowsRootHandles(true);
        sourceTree.setCellRenderer(new DBTreeCellRenderer());
        
        destTree = new JTree(new DBTreeModel(dblist));
        destTree.setRootVisible(false);
        destTree.setShowsRootHandles(true);
        destTree.setCellRenderer(new DBTreeCellRenderer());
        
        PanelBuilder pb = new PanelBuilder(
                new FormLayout(
                        "pref:grow,8dlu,pref:grow",
                        "pref,4dlu,fill:pref:grow,4dlu,pref,4dlu,pref"));
        pb.getLayout().addGroupedColumn(1);
        pb.getLayout().addGroupedColumn(3);
        CellConstraints cc = new CellConstraints();
        
        pb.addLabel("Source", cc.xy(1, 1));
        pb.addLabel("Destination", cc.xy(3, 1));
        
        pb.add(new JScrollPane(sourceTree), cc.xy(1, 3));
        pb.add(new JScrollPane(destTree), cc.xy(3, 3));
        
        pb.add(truncateDestinationTableBox = new JCheckBox("Truncate Destination Table?"), cc.xy(3, 5));

        pb.add(ButtonBarFactory.buildOKCancelBar(
                    new JButton(okAction), new JButton(cancelAction)),
               cc.xyw(1, 7, 3));
        
        pb.setDefaultDialogBorder();
        panel = pb.getPanel();
    }
    
    private Action okAction = new AbstractAction("OK") {
        public void actionPerformed(ActionEvent e) {
            try {
                doDataMove();
            } catch (Exception ex) {
                ASUtils.showExceptionDialog("Failed to move data", ex);
            }
        }
    };

    private Action cancelAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
            try {
                Window w = SwingUtilities.getWindowAncestor(panel);
                if (w != null) w.dispose();
            } catch (Exception ex) {
                ASUtils.showExceptionDialog("Failed to move data", ex);
            }
        }
    };
    
    public JPanel getPanel() {
        return panel;
    }
    
    public void doDataMove() throws SQLException, ArchitectException {
        final TreePath[] sourcePaths = sourceTree.getSelectionPaths();
        int tableCount = 0;
        int rowCount = 0;
        for (TreePath sourcePath : sourcePaths) {
            final SQLTable sourceTable = (SQLTable) sourcePath.getLastPathComponent();
            int thisCount = moveSingleTable(sourceTable);
            if (thisCount == -1) {
                int choice = JOptionPane.showConfirmDialog(panel, "Continue copying remaining tables?");
                if (choice != JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                tableCount += 1;
                rowCount += thisCount;
            }
        }
        JOptionPane.showMessageDialog(panel, "Copied data from "+tableCount+" tables ("+rowCount+" rows in total)");
    }
    
    /**
     * Moves the data from the table identified by 
     * @param sourcePath
     * @return The number of rows moved, or -1 if the user canceled the operation.
     * @throws SQLException
     * @throws ArchitectException
     */
    private int moveSingleTable(final SQLTable sourceTable) throws SQLException, ArchitectException {
        final SQLDatabase sourceDB = getParentDatabase(sourceTable);
        
        final TreePath destPath = destTree.getSelectionPath();
        final SQLObject destObject = (SQLObject) destPath.getLastPathComponent();
        final SQLDatabase destDB = getParentDatabase(destObject);
        
        String destCatalogName = null;
        String destSchemaName = null;
        String destTableName = sourceTable.getName();
        SQLObject tmpSqlObj = destObject;
        while (tmpSqlObj != null) {
            // walk up the ancestors and set table, catalog, and schema name as appropriate
            if (tmpSqlObj instanceof SQLTable) destTableName = tmpSqlObj.getName();
            if (tmpSqlObj instanceof SQLCatalog) destCatalogName = tmpSqlObj.getName();
            if (tmpSqlObj instanceof SQLSchema) destSchemaName = tmpSqlObj.getName();
            tmpSqlObj = tmpSqlObj.getParent();
        }
        
        boolean needToCreate = false;
        SQLTable destTable = destDB.getTableByName(
                destCatalogName, destSchemaName, destTableName);
        if (destTable == null) {
            needToCreate = true;
            destTable = ArchitectUtils.addSimulatedTable(
                destDB, destCatalogName, destSchemaName, destTableName);
        }
        
        Connection sourceCon = null;
        Connection destCon = null;
        try {
            sourceCon = sourceDB.getConnection();
            destCon = destDB.getConnection();
            
            final String sourceQualifiedName = DDLUtils.toQualifiedName(
                    sourceTable.getCatalogName(),
                    sourceTable.getSchemaName(),
                    sourceTable.getName());
            final String destQualifiedName = DDLUtils.toQualifiedName(
                    destCatalogName,
                    destSchemaName,
                    destTableName);

            if (needToCreate) {
                int choice = JOptionPane.showConfirmDialog(panel, "The destination table\n" +
                        destQualifiedName + 
                        "\nDoes not exist.  Create it?");
                if (choice != JOptionPane.YES_OPTION) return -1;
                
                DDLGenerator ddlg = DDLUtils.createDDLGenerator(destDB.getDataSource());
                ddlg.addTable(destTable);
                ddlg.generateDDLStatements(destDB);
                Statement stmt = null;
                try {
                    stmt = destCon.createStatement();
                    for (DDLStatement ddlstmt : ddlg.getDdlStatements()) {
                        stmt.executeUpdate(ddlstmt.getSQLText());
                    }
                } finally {
                    if (stmt != null) stmt.close();
                }
            }
            
            DataMover mover = new DataMover(destCon, sourceCon);
            mover.setCreatingDestinationTable(false);
            mover.setTruncatingDestinationTable(truncateDestinationTableBox.isSelected());
            mover.setDebug(true);  // when true, debug data goes to System.out

            JOptionPane.showMessageDialog(panel,
                    "About to copy\n"+sourceQualifiedName+"\nto\n"+destQualifiedName);
            
            int count = mover.copyTable(destQualifiedName, sourceQualifiedName);
            
            return count;
        } catch (InstantiationException ex) {
            throw new RuntimeException("Couldn't create DDL Generator", ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Couldn't create DDL Generator", ex);
        } finally {
            sourceCon.close();
            destCon.close();
        }
    }
    
    /**
     * Returns the nearest parent of obj that is an instance of SQLDatabase,
     * or null if there is no SQLDatabase ancestor of obj.
     * 
     * @param obj The object to find the parent database of.
     * @return The nearest SQLDatabase ancestor of obj, or null if there is no
     * such ancestor.
     */
    private static SQLDatabase getParentDatabase(SQLObject obj) {
        while (obj != null) {
            if (obj instanceof SQLDatabase) {
                return (SQLDatabase) obj;
            }
            obj = obj.getParent();
        }
        return null;
    }
}
