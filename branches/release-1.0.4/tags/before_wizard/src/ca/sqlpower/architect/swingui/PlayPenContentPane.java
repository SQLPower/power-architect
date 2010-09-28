package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.apache.log4j.Logger;

public class PlayPenContentPane extends JComponent {
	private static final Logger logger = Logger.getLogger(PlayPenContentPane.class);
	protected PlayPen owner;
	
	public PlayPenContentPane(PlayPen owner) {
		setName("PlayPen content pane");
		setLayout(new PlayPenLayout());
		this.owner = owner;
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
		Component c = getComponentAt(sp);
		if (c != null && c != this && c instanceof JComponent) {
			Object oldSource = e.getSource();
			e.setSource(c);
			e.translatePoint(-1 * e.getX() + sp.x - c.getX(),
							 -1 * e.getY() + sp.y - c.getY());
			text = ((JComponent) c).getToolTipText(e);
 			e.setSource(oldSource);
 			e.translatePoint(-1 * e.getX() + ep.x,
 							 -1 * e.getY() + ep.y);
		}
		return text;
	}

	public boolean delegateEvent(MouseEvent e) {
		if (logger.isDebugEnabled()) logger.debug(e.paramString());
		Point ep = e.getPoint(); // event's point in playpen in screen coords
		Point sp = owner.unzoomPoint(new Point(ep));
		Component c = getComponentAt(sp);
		if (c != null && c != this) {
//          sp.translate(c.getX() * -1, c.getY() * -1);
// 			c.dispatchEvent(new MouseEvent(c,
// 										   e.getID(),
// 										   e.getWhen(),
// 										   e.getModifiers(),
// 										   sp.x,
// 										   sp.y,
// 										   e.getClickCount(),
// 										   e.isPopupTrigger()));
			logger.debug("Changing source from [31m"+e.getSource()+" to [32m"+c+" [33m point "+e.getPoint()+"[0m");
			Object oldSource = e.getSource();
			e.setSource(c);
			e.translatePoint(-1 * e.getX() + sp.x - c.getX(),
							 -1 * e.getY() + sp.y - c.getY());
			logger.debug("new point is [34m"+e.getPoint()+"[0m");

			c.dispatchEvent(e);

 			e.setSource(oldSource);
 			e.translatePoint(-1 * e.getX() + ep.x,
 							 -1 * e.getY() + ep.y);
			return true;
		} else {
			return false;
		}
	}

	
}
