package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import org.apache.log4j.Logger;

/**
 * PlayPenComponent is a JComponent that can live in the playpen's
 * content pane.
 */
public abstract class PlayPenComponent extends JComponent {

	private static final Logger logger = Logger.getLogger(PlayPenComponent.class);

	/**
	 * Shows the given popup menu on the PlayPen that owns this table
	 * pane because it doesn't work to show it on this component,
	 * which is not really part of the swing hierarchy.
	 *
	 * @param menu the menu to show
	 * @param p the point (relative to this component's top-left
	 * corner) to show it at.
	 */
	public void showPopup(JPopupMenu menu, Point p) {
		p.translate(getX(), getY());
		getPlayPen().zoomPoint(p);
		menu.show(getPlayPen(), p.x, p.y);

	}

	public PlayPen getPlayPen() {
		if (getParent() == null) return null;
		return ((PlayPenContentPane) getParent()).getOwner();
	}

	/**
	 * Translates this request into a call to
	 * PlayPen.repaint(Rectangle).  That will eventually cause a call
	 * to PlayPen.paint(). This override is required because the
	 * PlayPen's contents are not part of the Swing containment
	 * hierarchy, and the standard RepaintManager discards
	 * revalidate() calls for invisible/unreachable components.
	 *
	 * <p>FIXME: should we check if this is the event dispatch thread?
	 */
	public void revalidate() {
		setSize(getPreferredSize());
		setLocation(getPreferredLocation());
		PlayPen pp = getPlayPen();
		if (pp == null) {
			logger.debug("getPlayPen() returned null.  Not generating repaint request.");
			return;
		}
		Rectangle r = new Rectangle();
		getBounds(r);
		pp.zoomRect(r);
		if (logger.isDebugEnabled()) logger.debug("Scheduling repaint at "+r);
		pp.repaint(r);
	}

	/**
	 * Issues a repaint to the PlayPen which covers the old bounds of
	 * this component.  This will allow newly-exposed sections of the
	 * PlayPen to draw themselves in case this setBounds call is
	 * shrinking this component. Normally in Swing, this is done
	 * automatically to the parent component but because of the
	 * zooming implementation, the PlayPen is not our parent.
	 */
	public void setBounds(int x, int y, int width, int height) {
		PlayPen owner = getPlayPen();
		if (owner != null) {
			Rectangle r = getBounds();
			double zoom = owner.getZoom();
			owner.repaint((int) Math.floor((double) r.x * zoom),
						  (int) Math.floor((double) r.y * zoom),
						  (int) Math.ceil((double) r.width * zoom),
						  (int) Math.ceil((double) r.height * zoom));
		}
		if (logger.isDebugEnabled()) {
			logger.debug("[36mUpdating bounds on "+getName()
						 +" to ["+x+","+y+","+width+","+height+"][0m");
		}
		super.setBounds(x, y, width, height);
	}

	/**
	 * The revalidate() call uses this to determine the component's
	 * correct location.  This implementation just returns the current
	 * location.  Override it if you need to be moved during validation.
	 */
	public Point getPreferredLocation() {
		return getLocation();
	}

	/**
	 * Forwards to {@link #repaint(Rectangle)}.
	 */
	public void repaint() {
		repaint(getBounds());
	}

	/**
	 * Forwards to {@link #repaint(long,int,int,int,int)}.
	 */
	public void repaint(Rectangle r) {
		repaint(0, r.x, r.y, r.width, r.height);
	}

	/**
	 * Tells the owning PlayPen to repaint the given region.  The
	 * rectangle is manipulated (zoomed) into screen coordinates.
	 */
	public void repaint(long tm,
						int x,
						int y,
						int width,
						int height) {
		PlayPen owner = getPlayPen();
		if (owner == null) return;
 		double zoom = owner.getZoom();
 		owner.repaint((int) Math.floor((double) x * zoom),
 					  (int) Math.floor((double) y * zoom),
 					  (int) Math.ceil((double) width * zoom),
 					  (int) Math.ceil((double) height * zoom));
	}
}
