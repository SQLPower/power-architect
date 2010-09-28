package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileCSVFormat;
import ca.sqlpower.architect.profile.ProfileFormat;
import ca.sqlpower.architect.profile.ProfileHTMLFormat;
import ca.sqlpower.architect.profile.ProfileManager;
import ca.sqlpower.architect.profile.ProfilePDFFormat;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.table.ProfileTable;

public class SaveProfileAction extends AbstractAction {

    /** The set of valid file types for saving the report in */
    private enum SaveableFileType { HTML, PDF, CSV }

    private JDialog parent;
    private ProfileTable viewTable;
    private ProfileManager pm;

    public SaveProfileAction(JDialog parent, ProfileTable viewTable, ProfileManager pm) {
        super("Save");
        this.parent = parent;
        this.viewTable = viewTable;
        this.pm = pm;
    }


    public void actionPerformed(ActionEvent e) {

        final List<ProfileResult> objectToSave = new ArrayList<ProfileResult>();

        if ( viewTable.getSelectedRowCount() > 1 ) {
            int selectedRows[] = viewTable.getSelectedRows();
            Set<SQLTable> selectedTable = new HashSet<SQLTable>();
            Set<SQLColumn> selectedColumn = new HashSet<SQLColumn>();
            for ( int i=0; i<selectedRows.length; i++ ) {
                int rowid = selectedRows[i];
                ColumnProfileResult result = viewTable.getColumnProfileResultForRow(rowid);
                selectedTable.add(result.getProfiledObject().getParentTable());
                selectedColumn.add(result.getProfiledObject());
            }

            boolean fullSelection = true;
            for ( SQLTable t : selectedTable ) {
                try {
                    for ( SQLColumn c : t.getColumns() ) {
                        if ( !selectedColumn.contains(c) ) {
                            fullSelection = false;
                            break;
                        }
                    }
                } catch (ArchitectException e1) {
                    ASUtils.showExceptionDialog(parent,
                            "Could not get column from table",e1);
                }
            }

            int response = JOptionPane.YES_OPTION;
            if ( !fullSelection ) {
                response = JOptionPane.showConfirmDialog(
                        parent,
                        "You have selected partical table, Do you want to save only this portion? No to save the whole table",
                        "Your selection contains partical table(s)",
                        JOptionPane.YES_NO_OPTION );
            }

            if (response == JOptionPane.NO_OPTION) {

                for ( SQLTable t : selectedTable ) {
                    objectToSave.add(pm.getResult(t));
                    try {
                        for ( SQLColumn c : t.getColumns() ) {
                            objectToSave.add(pm.getResult(c));
                        }
                    } catch (ArchitectException e1) {
                        ASUtils.showExceptionDialog(parent,
                                "Could not get column from table",e1);
                    }
                }
            } else {
                for ( int i=0; i<selectedRows.length; i++ ) {
                    int rowid = selectedRows[i];
                    ColumnProfileResult result = viewTable.getColumnProfileResultForRow(rowid);
                    SQLColumn column = result.getProfiledObject();
                    SQLTable table = column.getParentTable();
                    TableProfileResult tpr = (TableProfileResult) pm.getResult(table);
                    if ( !objectToSave.contains(tpr) )
                        objectToSave.add(tpr);
                    objectToSave.add(result);
                }
            }
        } else {
            for ( int i=0; i<viewTable.getRowCount(); i++ ) {
                ColumnProfileResult result = viewTable.getColumnProfileResultForRow(i);
                SQLColumn column = result.getProfiledObject();
                SQLTable table = column.getParentTable();
                TableProfileResult tpr = (TableProfileResult) pm.getResult(table);
                if ( !objectToSave.contains(tpr) )
                    objectToSave.add(tpr);
                objectToSave.add(result);
            }
        }


        JFileChooser chooser = new JFileChooser();

        chooser.addChoosableFileFilter(ASUtils.HTML_FILE_FILTER);
        chooser.addChoosableFileFilter(ASUtils.PDF_FILE_FILTER);
        chooser.addChoosableFileFilter(ASUtils.CSV_FILE_FILTER);
        chooser.removeChoosableFileFilter(chooser.getAcceptAllFileFilter());

        File file = null;
        SaveableFileType type;
        while ( true ) {
            // Ask the user to pick a file
            int response = chooser.showSaveDialog(parent);

            if (response != JFileChooser.APPROVE_OPTION) {
                return;
            }
            file = chooser.getSelectedFile();
            final FileFilter fileFilter = chooser.getFileFilter();
            String fileName = file.getName();
            int x = fileName.lastIndexOf('.');
            boolean gotType = false;
            SaveableFileType ntype = null;
            if (x != -1) {
                // pick file by filename the user typed
                String ext = fileName.substring(x+1);
                try {
                    ntype = SaveableFileType.valueOf(ext.toUpperCase());
                    gotType = true;
                } catch (IllegalArgumentException iex) {
                    gotType = false;
                }
            }

            if (gotType) {
                type = ntype;
            } else {
                // force filename to end with correct extention
                if (fileFilter == ASUtils.HTML_FILE_FILTER) {
                    if (!fileName.endsWith(".html")) {
                        file = new File(file.getPath()+".html");
                    }
                    type = SaveableFileType.HTML;
                } else if (fileFilter == ASUtils.PDF_FILE_FILTER){
                    if (!fileName.endsWith(".pdf")) {
                        file = new File(file.getPath()+".pdf");
                    }
                    type = SaveableFileType.PDF;
                } else if (fileFilter == ASUtils.CSV_FILE_FILTER){
                    if (!fileName.endsWith(".csv")) {
                        file = new File(file.getPath()+".csv");
                    }
                    type = SaveableFileType.CSV;
                } else {
                    throw new IllegalStateException("Unexpected file filter chosen");
                }
            }
            if (file.exists()) {
                response = JOptionPane.showConfirmDialog(
                        parent,
                        "The file\n"+file.getPath()+"\nalready exists. Do you want to overwrite it?",
                        "File Exists", JOptionPane.YES_NO_OPTION);
                if (response != JOptionPane.NO_OPTION) {
                    break;
                }
            } else {
                break;
            }

        }

        // Clone file object for use in inner class, can not make "file" final as we change it to add extension
        final File file2 = new File(file.getPath());
        final SaveableFileType type2 = type;
        Runnable saveTask = new Runnable() {
            public void run() {

                OutputStream out = null;
                try {
                    ProfileFormat prf = null;
                    out = new BufferedOutputStream(new FileOutputStream(file2));
                    switch(type2) {
                    case HTML:
                        final String encoding = "utf-8";
                        prf = new ProfileHTMLFormat(encoding);
                        break;
                    case PDF:
                        prf = new ProfilePDFFormat();
                        break;
                    case CSV:
                        prf = new ProfileCSVFormat();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown type");
                    }
                    prf.format(out, objectToSave, pm);
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(parent,"Could not generate/save report file", ex);
                } finally {
                    if ( out != null ) {
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException ex) {
                            ASUtils.showExceptionDialog(parent,"Could not close report file", ex);
                        }
                    }
                }
            }
        };
        new Thread(saveTask).start();

    }

}
