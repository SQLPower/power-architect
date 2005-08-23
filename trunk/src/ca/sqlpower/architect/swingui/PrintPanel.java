package ca.sqlpower.architect.swingui;

import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import org.apache.log4j.Logger;
import java.util.Iterator;
import java.util.Arrays;

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
public class PrintPanel extends JPanel implements ArchitectPanel, Pageable, Printable, ChangeListener {
	private static final Logger logger = Logger.getLogger(PrintPanel.class);

	/**
	 * This is the playpen we're printing.
	 */
	protected PlayPen pp;

	protected JComboBox printerBox;

	protected PrinterJob job;
	protected PrintRequestAttributeSet jobAttributes;
	protected PageFormat pageFormat;
	protected JLabel pageFormatLabel;
	protected JButton pageFormatButton;
	protected JLabel zoomLabel;
	protected JSlider zoomSlider;

	protected JDialog previewDialog;
	protected PrintPreviewPanel previewPanel;
	
	protected int pagesAcross;
	protected int pagesDown;

	protected double zoom;

	public PrintPanel(PlayPen pp) {
		super();
		setOpaque(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.pp = pp;
		job = PrinterJob.getPrinterJob();
		jobAttributes = new HashPrintRequestAttributeSet();
		pageFormat = job.defaultPage();

		JPanel formPanel = new JPanel(new FormLayout());
		formPanel.add(new JLabel("Printer"));
		formPanel.add(printerBox = new JComboBox(PrinterJob.lookupPrintServices()));
		printerBox.setSelectedItem(getPreferredPrinter());				

		formPanel.add(new JLabel("Page Format"));
		formPanel.add(pageFormatLabel = new JLabel(pageFormat.toString()));
		
		formPanel.add(new JLabel("Change Page Format"));
		formPanel.add(pageFormatButton = new JButton("Change Page Format"));
		pageFormatButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setPageFormat(job.pageDialog(jobAttributes));
				}
			});

		formPanel.add(zoomLabel = new JLabel("Scaling = 100%"));
		formPanel.add(zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 300, 100));
		setZoom(1.0);
		zoomSlider.addChangeListener(this);
		add(formPanel);
	}

	/**
	 * Called to determine what this user last printed from.
	 */
	PrintService getPreferredPrinter() {
		String defaultPrinterName = ArchitectFrame.getMainInstance().getUserSettings().getPrintUserSettings().getDefaultPrinterName();
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
		pageFormat = pf;
		if (pf != oldPF) {
			validateLayout();
			pageFormatLabel.setText(pageFormat.toString());
			firePropertyChange("pageFormat", oldPF, pageFormat);
		}
	}

	/**
	 * Call this if you want a preview dialog.  It will only work if
	 * this PrintPanel is visible and has a Window ancestor.
	 */
	public void showPreviewDialog() {
		previewDialog = new JDialog((JDialog) SwingUtilities.getWindowAncestor(this),
									"Print Preview");
		previewDialog.setContentPane(previewPanel = new PrintPreviewPanel(this));
		previewDialog.pack();
		previewDialog.setLocationRelativeTo(this);
		previewDialog.setVisible(true);
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
	}

	public void setZoom(double newZoom) {
		double oldZoom = zoom;
		zoom = newZoom;
		zoomLabel.setText("Scaling = "+((int) (newZoom*100.0))+"%");
		firePropertyChange("zoom", oldZoom, zoom);
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

			g2.drawString("Page "+pageIndex+" of "+(pagesAcross*pagesDown),
						  (float) (leftMargin+10.0), (float) (topMargin+10.0));
			logger.debug("Printing page "+(pageIndex+1)+" of "+(pagesAcross*pagesDown)
						 +" at ["+col+","+row+"]");

			g2.translate(leftMargin - col*width, topMargin - row*height);
			g2.scale(zoom, zoom);
			pp.print(g2);

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
				ArchitectFrame.getMainInstance().getUserSettings().getPrintUserSettings().setDefaultPrinterName( ((PrintService)printerBox.getSelectedItem()).getName() );
			} 		
			validateLayout();
			job.setPrintService((PrintService) printerBox.getSelectedItem());
			job.setPageable(this);
			job.print(jobAttributes);
		} catch (PrinterException ex) {
			logger.error("Printing failure", ex);
			JOptionPane.showMessageDialog(this, "Printing failed: "+ex.getMessage());
		}
		return true;
	}
	
	public void discardChanges() {
        // nothing to discard
	}

	// --- print preview panel ---

	public static class PrintPreviewPanel extends JPanel implements PropertyChangeListener {
		PrintPanel settings;

		public PrintPreviewPanel(PrintPanel settings) {
			this.settings = settings;
			setDoubleBuffered(false);
			settings.addPropertyChangeListener(this);
		}

		/**
		 * Not affected by user's zoom setting.
		 */
		public Dimension getPreferredSize() {
			settings.validateLayout();
			double iW = settings.pageFormat.getImageableWidth();
			double iH = settings.pageFormat.getImageableHeight();
			double printoutWidth = settings.pagesAcross * iW;
			double printoutHeight = settings.pagesDown * iH;

			double preferredScale = 500.0/printoutWidth;
			return new Dimension((int) (printoutWidth * preferredScale),
								 (int) (printoutHeight * preferredScale));
		}

		public void paintComponent(Graphics g) {
			settings.validateLayout();
			double iW = settings.pageFormat.getImageableWidth();
			double iH = settings.pageFormat.getImageableHeight();
			double printoutWidth = settings.pagesAcross * iW;
			double printoutHeight = settings.pagesDown * iH;

			// create a bitmapped image of the scaled playpen
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(settings.getBackground());
			g2.fill(new Rectangle(0, 0, getWidth(), getHeight()));
			double previewZoomX = (double) getWidth() / printoutWidth;
			double previewZoomY = (double) getHeight() / printoutHeight;
			double zoom = Math.min(previewZoomX, previewZoomY);

			int scaledWidth = (int) (getWidth()/zoom);
			int scaledHeight = (int) (getHeight()/zoom);
			logger.debug("After scaling, preview will seem to be size ("+scaledWidth+","+scaledHeight+")");

			// print the page background at the panel's zoom setting, centered in available space
			g2.scale(zoom, zoom);
			g2.translate((scaledWidth - printoutWidth) / 2,
						 (scaledHeight - printoutHeight) / 2);
			AffineTransform backup = g2.getTransform();
			g2.setColor(settings.pp.getBackground());
			g2.fill(new Rectangle(0, 0, (int) printoutWidth, (int) printoutHeight));

			// now print the playpen at the user's zoom setting, compounded with ours.
			g2.scale(settings.zoom, settings.zoom);
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			logger.debug("Printout size = ("+printoutWidth+","+printoutHeight
						 +"); playpen size = "+settings.pp.getPreferredSize());
			settings.pp.paint(g2);

			// and draw the lines where the page boundaries fall, at our own zoom scale
			g2.setTransform(backup);
			g2.setColor(getForeground());
			for (int i = 0; i <= settings.pagesAcross; i++) {
				g2.drawLine((int) (i * iW), 0, (int) (i * iW), (int) printoutHeight);
				logger.debug("Drew page separator at x="+(i*iW));
			}

			for (int i = 0; i <= settings.pagesDown; i++) {
				g2.drawLine(0, (int) (i * iH), (int) printoutWidth, (int) (i * iH));
				logger.debug("Drew page separator at y="+(i*iH));
			}
		}

		// ----- property change listener -----
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName() == null) {
				return;
			} else if (e.getPropertyName().equals("zoom")
					   || e.getPropertyName().equals("pageFormat")) {
				repaint();
			}
		}
	}
}
