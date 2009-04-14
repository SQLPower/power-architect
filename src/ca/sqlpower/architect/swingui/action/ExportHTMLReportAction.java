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

package ca.sqlpower.architect.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.Messages;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;
import ca.sqlpower.util.XsltTransformation;

/**
 * The xslt stylesheet used is architect2html.xslt. 
 * An xml OutputStream(using a {@link PipedOutputStream}) is generated, based on the
 * current playPen content and is read by a {@link PipedInputStream} which is used as the xml source. <br>
 * The stylesheet and the xml source are passed as parameters to the
 * {@link XsltTransformation} methods to generate an HTML report off the content
 * to a location specified by the user.
 */
public class ExportHTMLReportAction extends AbstractArchitectAction {

    private static final String ENCODING = "UTF-8";
    
    private PipedOutputStream xmlOutputStream;

    private FileOutputStream result;

    public ExportHTMLReportAction(ArchitectSwingSession session) {
        super(session, "Export to HTML", "Generates an HTML report of all the tables in the playpen ");
    }

    public void actionPerformed(ActionEvent e) {
        
        XsltTransformation xsltTransform = new XsltTransformation("/xsltStylesheets/architect2html.xslt");
        PipedInputStream xmlInputStream = new PipedInputStream();
        try {
            xmlOutputStream = new PipedOutputStream(xmlInputStream);
            new Thread(
                    new Runnable(){
                      public void run(){
                          try {
                            session.getProject().save(xmlOutputStream, ENCODING);
                          } catch (IOException e2) {
                              SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),"You got an error", e2);
                          } catch (SQLObjectException e2) {
                              SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),"You got an error", e2);
                          }
                      }
                    }
                  ).start();
            xmlOutputStream.flush();
             
        } catch (IOException e2) {
            SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),"You got an error", e2);
        }
        

        JFileChooser chooser = new JFileChooser(session.getProject().getFile());
        chooser.addChoosableFileFilter(SPSUtils.HTML_FILE_FILTER);
        int response = chooser.showSaveDialog(session.getArchitectFrame());
        chooser.setDialogTitle("Save HTML Report As");
        if (response != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getPath().endsWith(".html")) { //$NON-NLS-1$
            file = new File(file.getPath() + ".html"); //$NON-NLS-1$
        }
        if (file.exists()) {
            response = JOptionPane.showConfirmDialog(session.getArchitectFrame(), Messages.getString(
                    "ExportHTMLReportAction.fileAlreadyExists", file.getPath()), //$NON-NLS-1$
                    Messages.getString("ExportHTMLReportAction.fileAlreadyExistsDialogTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }
        try {
             result = new FileOutputStream(file);
        } catch (FileNotFoundException e2) {
            SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),"You got an error", e2);
        }
    
       
        try {
            xsltTransform.transform(xmlInputStream, result);
            result.close();
            xmlInputStream.close();
            xmlOutputStream.close();
            
            //Opens up the html file in the default browser
            BrowserUtil.launch(file.toURI().toString());
        } catch (Exception e1) {
              SPSUtils.showExceptionDialogNoReport(session.getArchitectFrame(),"You got an error", e1);
        }
    }

}
