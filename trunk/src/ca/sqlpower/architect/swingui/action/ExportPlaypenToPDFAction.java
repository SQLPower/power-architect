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

import java.awt.Graphics2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPen;
import ca.sqlpower.architect.swingui.PlayPenComponent;
import ca.sqlpower.architect.swingui.PlayPenContentPane;
import ca.sqlpower.swingui.SPSUtils;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ExportPlaypenToPDFAction extends ProgressAction {
    private static final Logger logger = Logger.getLogger(ExportPlaypenToPDFAction.class);

    private static final String FILE_KEY = "FILE_KEY";
    
    private static int OUTSIDE_PADDING = 10; 

    public ExportPlaypenToPDFAction(ArchitectSwingSession session) {
        super(session, "Export Playpen to PDF", "Export Playpen to PDF");
    }

    /**
     *  When an action is performed on this it pops up the save dialog
     *  and requests a file to save to. When it gets that it draws the
     *  playpen to a PDF file on a seperate thread.
     */
    public boolean setup(ActionMonitor monitor, Map<String,Object> properties) {
        monitor.started = true;
        JFileChooser chooser = new JFileChooser(session.getRecentMenu().getMostRecentFile());
        chooser.addChoosableFileFilter(SPSUtils.PDF_FILE_FILTER);
        monitor.setJobSize(playpen.getPlayPenContentPane().getComponentCount());
        
        File file = null;
        while (true) {
            int response = chooser.showSaveDialog(null);

            if (response != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = chooser.getSelectedFile();
            String fileName = file.getName();

            if (!fileName.endsWith(".pdf")) {
                file = new File(file.getPath()+".pdf");
            }

            if (file.exists()) {
                response = JOptionPane.showConfirmDialog(
                        null,
                        "The file\n" + file.getPath() + "\nalready exists. Do you want to overwrite it?",
                        "File Exists", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                break;
            }
        }

        logger.debug("Saving to file: "+file.getName()+" (" +file.getPath()+")");
    
        properties.put(FILE_KEY,file);
        return true;
    }
    
    @Override
    public void cleanUp(ActionMonitor monitor) {
        // TODO might have to cleanup here
    }

    @Override
    public void doStuff(ActionMonitor monitor, Map<String, Object> properties) {
        PlayPen playPen = new PlayPen(session, playpen);
        /* We translate the graphics to (OUTSIDE_PADDING, OUTSIDE_PADDING) 
         * so nothing is drawn right on the edge of the document. So
         * we multiply by 2 so we can accomodate the translate and ensure
         * nothing gets drawn outside of the document size.
         */
        final int width = playPen.getBounds().width + 2*OUTSIDE_PADDING;
        final int height = playPen.getBounds().height + 2*OUTSIDE_PADDING;
        final Rectangle ppSize = new Rectangle(width, height);
        
        OutputStream out = null;
        Document d = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream((File)properties.get(FILE_KEY)));
            d = new Document(ppSize);
            
            d.addTitle("Architect Playpen PDF Export");
            d.addAuthor(System.getProperty("user.name"));
            d.addCreator("Power*Architect version "+ArchitectVersion.APP_VERSION);
            
            PdfWriter writer = PdfWriter.getInstance(d, out);
            d.open();
            PdfContentByte cb = writer.getDirectContent();
            Graphics2D g = cb.createGraphicsShapes(width, height);
            // ensure a margin
            g.translate(OUTSIDE_PADDING, OUTSIDE_PADDING);
            PlayPenContentPane contentPane = playPen.getContentPane();
            int j=0;
            for (int i = contentPane.getComponentCount() - 1; i >= 0; i--) {
                PlayPenComponent ppc = contentPane.getComponent(i);
                g.translate(ppc.getLocation().x, ppc.getLocation().y);
                ppc.paint(g);
                g.translate(-ppc.getLocation().x, -ppc.getLocation().y);
                monitor.setProgress(j);
                j++;
            }
            playPen.paintComponent(g);
            g.dispose();
        } catch (Exception ex) {
            ASUtils.showExceptionDialog(session, 
                    "Could not export the playpen", 
                    ex);
        } finally {
            if (d != null) {
                try {
                    d.close();
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(session,
                            "Could not close document for exporting playpen", 
                            ex);
                }
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    ASUtils.showExceptionDialog(session,
                        "Could not close pdf file for exporting playpen", 
                        ex);
                }
            }
        }
    }

    @Override
    public String getDialogMessage() {
        return "Creating PDF";
    }
    
    @Override
    public String getButtonText() {
        return "Run in Background";
    }
}
