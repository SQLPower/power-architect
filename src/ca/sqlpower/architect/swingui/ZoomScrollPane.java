package ca.sqlpower.architect.swingui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Dimension;
import javax.swing.*;
import java.awt.geom.AffineTransform;

/**
 * Warning: this class doesn't work yet, and maybe never will.
 */
public class ZoomScrollPane extends JScrollPane {
	public ZoomScrollPane(JComponent view) {
		super(view);
	}

	public void setZoom(double zoom) {
		((ZoomViewport) viewport).setZoom(zoom);
	}

	public double getZoom() {
		return ((ZoomViewport) viewport).getZoom();
	}

	protected JViewport createViewport() {
		return new ZoomViewport(1.0);
	}

	public static class ZoomViewport extends JViewport {

		/**
		 * The amount of zoom that this viewport's contents should be
		 * rendered with.  Larger than 1.0 means "zoom in" and smaller
		 * means "zoom out".
		 */
		protected double zoom;
		
		public ZoomViewport(double zoom) {
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
				Dimension viewportSize = getSize();
				setExtentSize(new Dimension((int) (viewportSize.width * (1.0/zoom)),
											(int) (viewportSize.height * (1.0/zoom))));
				repaint();
			}
		}
		
 		public Dimension toViewCoordinates(Dimension size) {
 			return new Dimension((int) (size.width * zoom), (int) (size.height * zoom));
 		}
		
 		public Point toViewCoordinates(Point p) {
 			return new Point((int) (p.x * zoom), (int) (p.y * zoom));
 		}

	}
}
