package ca.sqlpower.architect.swingui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class PlayPenContentPane {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	protected PlayPen owner;
	private List children = new ArrayList();
	private List playPenComponentListeners = new ArrayList();
	private PlayPenComponentEventPassthrough playPenComponentEventPassthrough;


	public PlayPenContentPane(PlayPen owner) {
		this.owner = owner;
		playPenComponentEventPassthrough = new PlayPenComponentEventPassthrough();
	}
	
	
	/**
	 * Returns the PlayPen that this content pane belongs to.
	 */
	public PlayPen getOwner() {
		return owner;
	}

	public boolean contains(Point p) {
		return contains(p.x, p.y);
	}

	public boolean contains(int x, int y) {
		return true;
	}

	/**
	 * Returns true.
	 */
	public boolean isValidateRoot() {
		logger.debug("isValidateRoot returning true");
		return true;
	}

	/**
	 * Looks for tooltip text in the component under the pointer,
	 * respecting the current zoom level.
	 *
	 * <p>  XXX: doesn't actually cause tooltips to appear.
	 */
	public String getToolTipText(MouseEvent e) {
		String text = null;
		Point ep = e.getPoint(); // event's point in playpen in screen coords
		Point sp = owner.unzoomPoint(new Point(ep));
		PlayPenComponent c = getComponentAt(sp);
		if (c != null) {
			Object oldSource = e.getSource();
			e.setSource(c);
			e.translatePoint(-1 * e.getX() + sp.x - c.getX(),
							 -1 * e.getY() + sp.y - c.getY());
			text = c.getToolTipText();
 			e.setSource(oldSource);
 			e.translatePoint(-1 * e.getX() + ep.x,
 							 -1 * e.getY() + ep.y);
		}
		return text;
	}

	public PlayPenComponent getComponentAt(Point p) {
		Iterator it = children.iterator();
		while (it.hasNext()) {
			PlayPenComponent ppc = (PlayPenComponent) it.next();
			if (ppc.contains(p)) {
				return ppc;
			}
		}
		return null;
	}


	public int getComponentCount() {
		return children.size();
	}

	public PlayPenComponent getComponent(int i) {
		return (PlayPenComponent) children.get(i);
	}

	public void add(PlayPenComponent c, int i) {
		children.add(i,c);
		c.addPlayPenComponentListener(playPenComponentEventPassthrough);
		c.addSelectionListener(getOwner());
		c.revalidate();
	}

	public void remove(int j) {
		PlayPenComponent c = (PlayPenComponent) children.get(j);
		Rectangle r = c.getBounds();
		c.removePlayPenComponentListener(playPenComponentEventPassthrough);
		c.removeSelectionListener(getOwner());
		children.remove(j);
		getOwner().repaint(r);
	}
	
	public void remove(PlayPenComponent c) {
		int j = children.indexOf(c);
		if ( j >= 0 ) remove(j);
	}

	
	// ----------------- PlayPenComponentListener Passthrough stuff ---------------------------
	public void addPlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.add(l);
	}
	
	public void removePlayPenComponentListener(PlayPenComponentListener l) {
		playPenComponentListeners.remove(l);
	}
	
	private void refirePlayPenComponentMoved(PlayPenComponentEvent e) {
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentMoved(e);
		}
	}

	private void refirePlayPenComponentResized(PlayPenComponentEvent e) {
		Iterator it = playPenComponentListeners.iterator();
		while (it.hasNext()) {
			((PlayPenComponentListener) it.next()).componentResized(e);
		}
	}
	
	private class PlayPenComponentEventPassthrough implements PlayPenComponentListener {

		public void componentMoved(PlayPenComponentEvent e) {
			refirePlayPenComponentMoved(e);
		}

		public void componentResized(PlayPenComponentEvent e) {
			refirePlayPenComponentResized(e);
		}
		
	}
}
