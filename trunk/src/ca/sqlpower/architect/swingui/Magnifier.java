package ca.sqlpower.architect.swingui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Dimension;
import javax.swing.*;
import java.awt.geom.AffineTransform;
import org.apache.log4j.Logger;

public class Magnifier extends JPanel {
	private static final Logger logger = Logger.getLogger(Magnifier.class);

	/**
	 * The amount of zoom that this viewport's contents should be
	 * rendered with.  Larger than 1.0 means "zoom in" and smaller
	 * means "zoom out".
	 */
	protected double zoom;

	protected JComponent view;

	public Magnifier(JComponent view, double zoom) {
		super(null);
		add(view);
		this.view = view;
		setZoom(zoom);
	}

	/**
	 * Applies the current zoom before calling super.paint();
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		AffineTransform oldTransform = g2.getTransform();
		try {
			g2.scale(zoom, zoom);
			logger.debug("Painting view "+view+" with zoom="+zoom);
			super.paint(g);
		} finally {
			g2.setTransform(oldTransform);
		}
	}

	public double getZoom() {
		return zoom;
	}
	
	public void setZoom(double newZoom) {
		if (zoom != newZoom) {
			double oldZoom = zoom;
			zoom = newZoom;
			firePropertyChange("zoom", oldZoom, newZoom);
			setSize(new Dimension((int) (view.getWidth() * zoom),
								  (int) (view.getHeight() * zoom)));
			invalidate();
			logger.debug("Resized to "+getSize());
			//repaint();
		}
	}

	public Dimension getPreferredSize() {
		return getSize();
	}
	
	public Dimension getMinimumSize() {
		return getSize();
	}

	public boolean isOptimizedDrawingEnabled() {
		return false;
	}
}
