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
package ca.sqlpower.architect.swingui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Iterator;

import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

import ca.sqlpower.swingui.DataEntryPanel;

/**
 * The PrintDialogFrame lets the user specify multi-page printouts by
 * scaling the work area to any size.
 *
 * <p>Functional requirements:
 * <ul>
 *  <li>work area can scale to any size (large or small)
 *  <li>real-time preview of what the printout will look like, whether or not it spans pages
 * </ul>
 */
public class PrintPanel extends JPanel implements DataEntryPanel, Pageable, Printable, ChangeListener {
	private static final Logger logger = Logger.getLogger(PrintPanel.class);

	/**
	 * This is the playpen we're printing.
	 */
	private PlayPen pp;

	private JComboBox printerBox;

	private PrinterJob job;
	private PrintRequestAttributeSet jobAttributes;
	private PageFormat pageFormat;
	private JLabel pageFormatLabel;
	private JButton pageFormatButton;
	private JLabel zoomLabel;
	private JSlider zoomSlider;
	private JLabel pageCountLabel;
	private JCheckBox printPageNumbersBox;
	private JSpinner numOfCopies;
	
	private PrintPreviewPanel previewPanel;
	
	private int pagesAcross;
	private int pagesDown;

	private double zoom;
    
    private final ArchitectSwingSession session;

	public PrintPanel(ArchitectSwingSession session) {
		super();
		setOpaque(true);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.session = session;
		this.pp = new PlayPen(session, session.getPlayPen());
		
        // don't need this playpen to be interactive or respond to SQLObject changes
        pp.destroy();

		add(new PrintPreviewPanel());
		
		job = PrinterJob.getPrinterJob();
		jobAttributes = new HashPrintRequestAttributeSet();
		
		pageFormat = new PageFormat();
		pageFormat.setPaper(new Paper());
		pageFormat.getPaper().setImageableArea(50, 50, pageFormat.getWidth()-(50*2), pageFormat.getHeight()-(50*2));

		JPanel formPanel = new JPanel(new FormLayout());
		formPanel.add(new JLabel(Messages.getString("PrintPanel.printerLabel"))); //$NON-NLS-1$
		formPanel.add(printerBox = new JComboBox(PrinterJob.lookupPrintServices()));
		printerBox.setSelectedItem(getPreferredPrinter(session));				

		formPanel.add(new JLabel(Messages.getString("PrintPanel.pageFormateLabel"))); //$NON-NLS-1$
		String pf = paperToPrintable(pageFormat);
		formPanel.add(pageFormatLabel = new JLabel(pf.toString()));
		
		formPanel.add(new JLabel(Messages.getString("PrintPanel.numCopiesLabel"))); //$NON-NLS-1$
		numOfCopies = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
		formPanel.add(numOfCopies);
		
		formPanel.add(new JLabel(Messages.getString("PrintPanel.changePageFormatLabel"))); //$NON-NLS-1$
		formPanel.add(pageFormatButton = new JButton(Messages.getString("PrintPanel.changePageFormatLabel"))); //$NON-NLS-1$
		pageFormatButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setPageFormat(job.pageDialog(pageFormat));
				}
		});
		formPanel.add(zoomLabel = new JLabel(Messages.getString("PrintPanel.scalingLabel"))); //$NON-NLS-1$
		formPanel.add(zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 300, 100));
		
		formPanel.add(new JLabel("")); //$NON-NLS-1$
		formPanel.add(printPageNumbersBox = new JCheckBox(Messages.getString("PrintPanel.printPageNumbersOption"))); //$NON-NLS-1$
		printPageNumbersBox.setSelected(true);
		
		setZoom(1.0);
		zoomSlider.addChangeListener(this);
		pageCountLabel = new JLabel(Messages.getString("PrintPanel.pageCountLabel", String.valueOf(getNumberOfPages()))); //$NON-NLS-1$
		formPanel.add(pageCountLabel);
		add(formPanel);		
	}

	/**
	 * Convert a PageFormat to a nice String; for some reason the Swing
	 * Page Format dialog can use names but PageFormat cannot.
	 * XXX find out how to get these from the dialog.
	 */
	public static String paperToPrintable(PageFormat pageFormat) {
		StringBuffer pf = new StringBuffer();
		Paper paper = pageFormat.getPaper();
		pf.append(String.format("%.1f", paper.getWidth()/72)); //$NON-NLS-1$
		pf.append('x');
		pf.append(String.format("%.1f", paper.getHeight()/72)); //$NON-NLS-1$
		pf.append('-');
		switch(pageFormat.getOrientation()){
		case PageFormat.PORTRAIT: pf.append("(portrait)"); break; //$NON-NLS-1$
		case PageFormat.LANDSCAPE: pf.append("(landscape)"); break; //$NON-NLS-1$
		case PageFormat.REVERSE_LANDSCAPE: pf.append("(rev. landscape)"); break; //$NON-NLS-1$
		default: pf.append("(?)"); //$NON-NLS-1$
		}
		return pf.toString();
	}

	/**
	 * Called to determine what this user last printed from.
	 */
	PrintService getPreferredPrinter(ArchitectSwingSession session) {
		String defaultPrinterName = session.getUserSettings().getPrintUserSettings().getDefaultPrinterName();
		PrintService psRetVal = null;
		Iterator it = Arrays.asList( PrinterJob.lookupPrintServices() ).iterator();
		while (it.hasNext() && psRetVal == null) {
			PrintService ps = (PrintService) it.next();
			if (ps.getName().equals(defaultPrinterName)) {
				psRetVal = ps;
			}
		}
		// if there's no match, give the default printer...
		if (psRetVal == null) {
			psRetVal = PrinterJob.getPrinterJob().getPrintService();
		}
		return psRetVal;
	}

	public void setPageFormat(PageFormat pf) {
		PageFormat oldPF = pageFormat;
		if ( pf != null ) {
			pageFormat = pf;
			if (pf != oldPF) {
				validateLayout();
				pageFormatLabel.setText(paperToPrintable(pageFormat));
				firePropertyChange("pageFormat", oldPF, pageFormat); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Calculates the the number of pages across and pages down
	 * required for the current print settings.  It depends on the
	 * zoom setting, current page format, and playpen size.
	 */
	public void validateLayout() {
		// widths are in points (1/72 inch units)
		Dimension ppSize = pp.getPreferredSize();
		double ppWidth = ppSize.width;
		double ppHeight = ppSize.height;

		double paperWidth = pageFormat.getImageableWidth();
		double paperHeight = pageFormat.getImageableHeight();
		
		pagesAcross = (int) Math.ceil(zoom * ppWidth / paperWidth);
		pagesDown = (int) Math.ceil(zoom * ppHeight / paperHeight);
		pageCountLabel.setText(Messages.getString("PrintPanel.pageCountLabel", String.valueOf(getNumberOfPages()))); //$NON-NLS-1$
	}

	public void setZoom(double newZoom) {
		double oldZoom = zoom;
		zoom = newZoom;
		zoomLabel.setText(Messages.getString("PrintPanel.zoomLabel", String.valueOf((int)(newZoom*100.0)))); //$NON-NLS-1$
		firePropertyChange("zoom", oldZoom, zoom); //$NON-NLS-1$
	}

	// ---- change listener interface (for the slider) ----
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == zoomSlider) {
			setZoom((double) zoomSlider.getValue() / 100.0);
		}
	}

	// --- pageable interface ---
	public int getNumberOfPages() {
		return pagesAcross*pagesDown;
	}

	public PageFormat getPageFormat(int pageIndex)
		throws IndexOutOfBoundsException {
		return pageFormat;
	}

	public Printable getPrintable(int pageIndex)
		throws IndexOutOfBoundsException {
		return this;
	}

	// --- printable interface ---

	/**
	 * Renders the requested page of pp into the given graphics.
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
		throws PrinterException {
		Graphics2D g2 = (Graphics2D) graphics;
		if (pageIndex < pagesAcross*pagesDown) {
			
			double leftMargin = pageFormat.getImageableX();
			double topMargin = pageFormat.getImageableY();
			double width = pageFormat.getImageableWidth();
			double height = pageFormat.getImageableHeight();

			// which page we're printing in the big grid
			int col = pageIndex % pagesAcross;
			int row = pageIndex / pagesAcross;
			
			logger.debug("Printing page "+(pageIndex+1)+" of "+(pagesAcross*pagesDown) //$NON-NLS-1$ //$NON-NLS-2$
					+" at ["+col+","+row+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			AffineTransform backupXform = g2.getTransform();
			g2.translate(leftMargin - col*width, topMargin - row*height);
			g2.scale(zoom, zoom);
			pp.print(g2);
			
			g2.setTransform(backupXform);
			if (printPageNumbersBox.isSelected()) {
				g2.drawString(Messages.getString("PrintPanel.pageNumber", String.valueOf(pageIndex+1), String.valueOf(pagesAcross*pagesDown)), //$NON-NLS-1$ //$NON-NLS-2$
						(float) (leftMargin+10.0), (float) (topMargin+10.0));
			
			}
			
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}


	// --- architect panel ----
	public boolean applyChanges() {
		try {
		    // set current printer as default
			if (printerBox.getItemCount() > 0 && printerBox.getSelectedItem() instanceof PrintService) {
				session.getUserSettings().getPrintUserSettings().setDefaultPrinterName( ((PrintService)printerBox.getSelectedItem()).getName() );
			} 		
			validateLayout();
			job.setPrintService((PrintService) printerBox.getSelectedItem());
			job.setPageable(this);
			job.setCopies((Integer) numOfCopies.getValue());
			job.print(jobAttributes);
		} catch (PrinterException ex) {
			logger.error("Printing failure", ex); //$NON-NLS-1$
			ASUtils.showExceptionDialogNoReport(PrintPanel.this, Messages.getString("PrintPanel.printFailed"), ex); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	public void discardChanges() {
        // nothing to discard
	}
	
	public JPanel getPanel() {
		return this;
	}

	// --- print preview panel ---

	public class PrintPreviewPanel extends JPanel implements PropertyChangeListener {

	    /**
	     * The preferred size of the play pen at the default zoom.
	     */
	    private Dimension playPenPreferredSize;
	    
	    /**
	     * A copy of the play pen graphic to get the font render context from.
	     */
	    private Graphics fontContextGraphic;
	    
		public PrintPreviewPanel() {
			setDoubleBuffered(false);
			PrintPanel.this.addPropertyChangeListener(this);
			PreviewZoomAdjuster adjuster = new PreviewZoomAdjuster(); 
			addMouseMotionListener(adjuster);
			addMouseListener(adjuster);
			playPenPreferredSize = pp.getPreferredSize();
			fontContextGraphic = session.getPlayPen().getGraphics().create();
		}

		/**
		 * Not affected by user's zoom setting.
		 */
		public Dimension getPreferredSize() {
			validateLayout();
			double iW = pageFormat.getImageableWidth();
			double iH = pageFormat.getImageableHeight();
			double printoutWidth = pagesAcross * iW;
			double printoutHeight = pagesDown * iH;

			double preferredScale = 500.0/printoutWidth;
			return new Dimension((int) (printoutWidth * preferredScale),
								 (int) (printoutHeight * preferredScale));
		}

		/*
		 * Calculates the scaling factor we need in order to show the whole print preview in the
		 * available space.
		 */
		private double calculateZoom() {
			Dimension ppSize = playPenPreferredSize;
			double previewZoomX = (double) getWidth() / ppSize.width;
			double previewZoomY = (double) getHeight() / ppSize.height;
			return Math.min(previewZoomX, previewZoomY);
		}
		
		public void paintComponent(Graphics g) {
			validateLayout();

			Graphics2D g2 = (Graphics2D) g;
			double zoom = calculateZoom();
			
			//Set the font render context for the new panel for the play pen
			Graphics2D fcg = (Graphics2D) fontContextGraphic;
			AffineTransform backupContextTransform = ((Graphics2D)fcg).getTransform();
            FontRenderContext frc = null;
            if (fcg != null) {
                fcg.scale(zoom, zoom);
                frc = fcg.getFontRenderContext();
                fcg.setTransform(backupContextTransform);
            }
	        pp.setFontRenderContext(frc);

			pp.setZoom(zoom);
			pp.paintComponent(g);
			
	        int scaledWidth = (int) (getWidth()/zoom);
	        int scaledHeight = (int) (getHeight()/zoom);
			// and draw the lines where the page boundaries fall
			double iW = pageFormat.getImageableWidth();
			double iH = pageFormat.getImageableHeight();

			g2.scale(1/PrintPanel.this.zoom, 1/PrintPanel.this.zoom);
			g2.setColor(pp.getForeground());
			for (int i = 0; i <= pagesAcross; i++) {
				g2.drawLine((int) (i * iW), 0, (int) (i * iW), (int) (scaledHeight*PrintPanel.this.zoom));
				if (logger.isDebugEnabled()) logger.debug("Drew page separator at x="+(i*iW)); //$NON-NLS-1$
			}

			for (int i = 0; i <= pagesDown; i++) {
				g2.drawLine(0, (int) (i * iH), (int) (scaledWidth*PrintPanel.this.zoom), (int) (i * iH));
				if (logger.isDebugEnabled()) logger.debug("Drew page separator at y="+(i*iH)); //$NON-NLS-1$
			}		
		}

		// ----- property change listener -----
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName() == null) {
				return;
			} else if (e.getPropertyName().equals("zoom") //$NON-NLS-1$
					   || e.getPropertyName().equals("pageFormat")) { //$NON-NLS-1$
				repaint();
			}
		}
		
	    /**
	     * The PreviewZoomAdjuster watches for mouse drags over the 
	     * preview and adjusts the zoom slider accordingly.
	     */
	    public class PreviewZoomAdjuster extends MouseInputAdapter {
            public void mouseDragged(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                Point p = new Point(e.getPoint());
                double zoom = calculateZoom();
                p.x = (int) (p.x / zoom);
                p.y = (int) (p.y / zoom);
                
                zoomSlider.setValue((int) ( (pageFormat.getImageableWidth()/p.x) * 100));
            }
            
            public void mousePressed(MouseEvent e) {
                mouseDragged(e);
            }
            
            public void mouseReleased(MouseEvent e) {
                setCursor(null);
            }
	    }

	}

    public boolean hasUnsavedChanges() {
        // TODO return whether this panel has been changed
        return true;
    }


}
