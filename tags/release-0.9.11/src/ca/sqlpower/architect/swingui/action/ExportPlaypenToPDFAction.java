/*
 * Copyright (c) 2008, SQL Power Group Inc.
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
import ca.sqlpower.util.MonitorableImpl;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ExportPlaypenToPDFAction extends ProgressAction {
    private static final Logger logger = Logger.getLogger(ExportPlaypenToPDFAction.class);

    private static final String FILE_KEY = "FILE_KEY"; //$NON-NLS-1$
    
    private static int OUTSIDE_PADDING = 10; 

    public ExportPlaypenToPDFAction(ArchitectSwingSession session) {
        super(session, Messages.getString("ExportPlaypenToPDFAction.name"), Messages.getString("ExportPlaypenToPDFAction.description")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     *  When an action is performed on this it pops up the save dialog
     *  and requests a file to save to. When it gets that it draws the
     *  playpen to a PDF file on a seperate thread.
     */
    public boolean setup(MonitorableImpl monitor, Map<String,Object> properties) {
        monitor.setStarted(true);
        JFileChooser chooser = new JFileChooser(session.getRecentMenu().getMostRecentFile());
        chooser.addChoosableFileFilter(SPSUtils.PDF_FILE_FILTER);
        monitor.setJobSize(playpen.getPlayPenContentPane().getComponentCount());
        
        File file = null;
        while (true) {
            int response = chooser.showSaveDialog(session.getArchitectFrame());

            if (response != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = chooser.getSelectedFile();
            String fileName = file.getName();

            if (!fileName.endsWith(".pdf")) { //$NON-NLS-1$
                file = new File(file.getPath()+".pdf"); //$NON-NLS-1$
            }

            if (file.exists()) {
                response = JOptionPane.showConfirmDialog(
                        null,
                        Messages.getString("ExportPlaypenToPDFAction.fileAlreadyExists", file.getPath()), //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("ExportPlaypenToPDFAction.fileAlreadyExistsDialogTitle"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$
                if (response == JOptionPane.YES_OPTION) {
                    break;
                }
            } else {
                break;
            }
        }
        
        logger.debug("Saving to file: " + file.getName() + "(" + file.getPath() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
        properties.put(FILE_KEY,file);
        return true;
    }
    
    @Override
    public void cleanUp(MonitorableImpl monitor) {
        // TODO might have to cleanup here
    }

    @Override
    public void doStuff(MonitorableImpl monitor, Map<String, Object> properties) {
        PlayPen playPen = new PlayPen(session, playpen);
        
        // don't need this playpen to be interactive or respond to SQLObject changes
        playpen.destroy();
        
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
            
            d.addTitle(Messages.getString("ExportPlaypenToPDFAction.PdfTitle")); //$NON-NLS-1$
            d.addAuthor(System.getProperty("user.name")); //$NON-NLS-1$
            d.addCreator(Messages.getString("ExportPlaypenToPDFAction.powerArchitectVersion")+ArchitectVersion.APP_VERSION); //$NON-NLS-1$
            
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
                    Messages.getString("ExportPlaypenToPDFAction.couldNotExportPlaypen"),  //$NON-NLS-1$
                    ex);
        } finally {
            if (d != null) {
                try {
                    d.close();
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(session,
                            Messages.getString("ExportPlaypenToPDFAction.couldNotCloseDocument"),  //$NON-NLS-1$
                            ex);
                }
            }
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
                    ASUtils.showExceptionDialog(session,
                        Messages.getString("ExportPlaypenToPDFAction.couldNotClosePdfFile"),  //$NON-NLS-1$
                        ex);
                }
            }
        }
    }

    @Override
    public String getDialogMessage() {
        return Messages.getString("ExportPlaypenToPDFAction.creatingPdf"); //$NON-NLS-1$
    }
    
    @Override
    public String getButtonText() {
        return Messages.getString("ExportPlaypenToPDFAction.runInBackgroundOption"); //$NON-NLS-1$
    }
}
