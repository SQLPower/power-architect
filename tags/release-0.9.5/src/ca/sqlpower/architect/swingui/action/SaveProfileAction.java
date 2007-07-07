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
package ca.sqlpower.architect.swingui.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import ca.sqlpower.architect.profile.ColumnProfileResult;
import ca.sqlpower.architect.profile.ProfileCSVFormat;
import ca.sqlpower.architect.profile.ProfileFormat;
import ca.sqlpower.architect.profile.ProfileHTMLFormat;
import ca.sqlpower.architect.profile.ProfilePDFFormat;
import ca.sqlpower.architect.profile.ProfileResult;
import ca.sqlpower.architect.profile.TableProfileResult;
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.table.ProfileJTable;

public class SaveProfileAction extends AbstractAction {

    public class ProfileResultsTree {
        Map<TableProfileResult, Set<ColumnProfileResult>>  resultTree = new TreeMap<TableProfileResult, Set<ColumnProfileResult>>();
        
        public void addTableProfileResult(TableProfileResult tpr){
            if (!resultTree.containsKey(tpr)) {
                resultTree.put(tpr, new HashSet<ColumnProfileResult>());
            }
        }
        
        public void addColumnProfileResult(ColumnProfileResult cpr){
            TableProfileResult tpr = cpr.getParentResult();
            if (!resultTree.containsKey(tpr)) {
                resultTree.put(tpr, new TreeSet<ColumnProfileResult>());
            }
            ((Set<ColumnProfileResult>)resultTree.get(tpr)).add(cpr);
        }
        
        public List<ProfileResult> getDepthFirstList() {
            List<ProfileResult> depthFirstList = new ArrayList<ProfileResult>();
            for (TableProfileResult tpr : resultTree.keySet()) {
                depthFirstList.add(tpr);
                for (ColumnProfileResult cpr : ((Set<ColumnProfileResult>)resultTree.get(tpr))) {
                    depthFirstList.add(cpr);
                }
            }
            return depthFirstList;
        }
    }
    
    /** The set of valid file types for saving the report in */
    private enum SaveableFileType { HTML, PDF, CSV }

    /**
     * The component whose window ancestor will own dialogs created by this action.
     */
    private Component dialogOwner;

    private ProfileJTable viewTable;

    /**
     * Creates a new action which will, when invoked, offer to save profile results
     * in one of several deluxe file formats.
     * 
     * @param dialogOwner The component whose window ancestor will own dialogs created by this action.
     * @param viewTable The (eww) jtable which contains the profile results to be exported.
     * XXX this should be a collection of TableProfileResult objects, not a view component that houses them
     */
    public SaveProfileAction(Component dialogOwner, ProfileJTable viewTable) {
        super("Save...");
        this.dialogOwner = dialogOwner;
        this.viewTable = viewTable;
    }


    public void actionPerformed(ActionEvent e) {

        final ProfileResultsTree objectToSave = new ProfileResultsTree();

        if ( viewTable.getSelectedRowCount() > 1 ) {
            int selectedRows[] = viewTable.getSelectedRows();
            Set<TableProfileResult> selectedTable = new HashSet<TableProfileResult>();
            HashSet<ColumnProfileResult> selectedColumn = new HashSet<ColumnProfileResult>();
            for ( int i=0; i<selectedRows.length; i++ ) {
                int rowid = selectedRows[i];
                ColumnProfileResult result = viewTable.getColumnProfileResultForRow(rowid);
                selectedTable.add(result.getParentResult());
                selectedColumn.add(result);
            }

            boolean fullSelection = true;
            for (TableProfileResult tpr : selectedTable) {
                for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                    if ( !selectedColumn.contains(cpr) ) {
                        fullSelection = false;
                        break;
                    }
                }
            }

            int response = 0;
            if ( !fullSelection ) {
                response = JOptionPane.showOptionDialog(
                        dialogOwner,
                        "You have selected only part of a table.\nDo you want to save only this portion?",
                        "Your selection contains partial table(s)",
                        0,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[] {"Save Partial","Save Entire Table"},
                        "Save Entire Table");
            }

            if (response == 1) { // entire table

                for (TableProfileResult tpr : selectedTable) {
                    for (ColumnProfileResult cpr : tpr.getColumnProfileResults()) {
                        objectToSave.addColumnProfileResult(cpr);
                    }
                }
            } else { // partial table
                for ( int i=0; i<selectedRows.length; i++ ) {
                    int rowid = selectedRows[i];
                    ColumnProfileResult result = viewTable.getColumnProfileResultForRow(rowid);
                    objectToSave.addColumnProfileResult(result);
                }
            }
        } else {
            for (int i = 0; i < viewTable.getRowCount(); i++) {
                ColumnProfileResult result = viewTable.getColumnProfileResultForRow(i);
                objectToSave.addColumnProfileResult(result);
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
            int response = chooser.showSaveDialog(dialogOwner);

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
                        dialogOwner,
                        "The file\n" + file.getPath() + "\nalready exists. Do you want to overwrite it?",
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
                    prf.format(out, objectToSave.getDepthFirstList());
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(dialogOwner,
                        "Could not generate/save report file", ex, new ArchitectExceptionReportFactory());
                } finally {
                    if ( out != null ) {
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException ex) {
                            ASUtils.showExceptionDialog(dialogOwner,
                                "Could not close report file", ex, new ArchitectExceptionReportFactory());
                        }
                    }
                }
            }
        };
        new Thread(saveTask).start();

    }
}
