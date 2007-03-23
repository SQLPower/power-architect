package ca.sqlpower.architect.swingui.action;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectVersion;
import ca.sqlpower.architect.qfa.ArchitectExceptionReportFactory;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.PlayPen;

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ExportPlaypenToPDFAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ExportPlaypenToPDFAction.class);
    
    private static int OUTSIDE_PADDING = 10; 
    private PlayPen pp;

    public ExportPlaypenToPDFAction() {
        // TODO: when we have an icon for this, use one.
        super("Export Playpen", null);
        putValue(SHORT_DESCRIPTION, "Export Playpen to PDF");
    }

    /**
     *  When an action is performed on this it pops up the save dialog
     *  and requests a file to save to. When it gets that it draws the
     *  playpen to a PDF file on a seperate thread.
     */
    public void actionPerformed(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(ASUtils.PDF_FILE_FILTER);
        /* We translate the graphics to (OUTSIDE_PADDING, OUTSIDE_PADDING) 
         * so nothing is drawn right on the edge of the document. So
         * we multiply by 2 so we can accomodate the translate and ensure
         * nothing gets drawn outside of the document size.
         */
        final int width = pp.getBounds().width + 2*OUTSIDE_PADDING;
        final int height = pp.getBounds().height + 2*OUTSIDE_PADDING;
        final Rectangle ppSize = new Rectangle(width, height);
        
        File file = null;
        while (true) {
            int response = chooser.showSaveDialog(null);

            if (response != JFileChooser.APPROVE_OPTION) {
                return;
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
    
        // the first file cannot be made final because we append the .pdf
        final File file2 = new File(file.getPath());
        Runnable exportTask = new Runnable() {

            public void run() {
                OutputStream out = null;
                Document d = null;
                Component c = ArchitectFrame.getMainInstance();
                try {
                    out = new BufferedOutputStream(new FileOutputStream(file2));
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
                    pp.paintComponent(g);
                    g.dispose();
                } catch (Exception ex) {
                    ASUtils.showExceptionDialog(c, 
                            "Could not export the playpen", 
                            ex, 
                            new ArchitectExceptionReportFactory());
                } finally {
                    if (d != null) {
                        try {
                            d.close();
                        } catch (Exception ex) {
                            ASUtils.showExceptionDialog(c,
                                    "Could not close document for exporting playpen", 
                                    ex, 
                                    new ArchitectExceptionReportFactory());
                        }
                    }
                    if (out != null) {
                        try {
                            out.flush();
                            out.close();
                        } catch (IOException ex) {
                            ASUtils.showExceptionDialog(null,
                                "Could not close pdf file for exporting playpen", 
                                ex, 
                                new ArchitectExceptionReportFactory());
                        }
                    }
                }
            }
        };
        
        new Thread(exportTask).start();
    }
    
    public void setPlayPen(PlayPen pp) {
        this.pp = pp;
    }
}
