package ca.sqlpower.architect.swingui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import org.apache.log4j.Logger;

/**
 * The BasicRelationshipUI is responsible for drawing the lines
 * between tables.  Subclasses decorate the ends of the lines.
 */
public class BasicRelationshipUI extends RelationshipUI
	implements PropertyChangeListener, java.io.Serializable {

	private static Logger logger = Logger.getLogger(BasicRelationshipUI.class);

	protected Relationship relationship;

	public static ComponentUI createUI(JComponent c) {
		logger.debug("Creating new BasicRelationshipUI for "+c);
        return new BasicRelationshipUI();
    }

    public void installUI(JComponent c) {
		logger.debug("Installing BasicRelationshipUI on "+c);
		relationship = (Relationship) c;
		relationship.addPropertyChangeListener(this);
    }

    public void uninstallUI(JComponent c) {
		relationship = (Relationship) c;
		relationship.removePropertyChangeListener(this);
    }
	
	/**
	 * @param g The graphics to paint on.  It should be in the
	 * coordinate space of the containing playpen.
	 */
    public void paint(Graphics g, JComponent c) {
		logger.debug("BasicRelationshipUI is painting");
		Relationship r = (Relationship) c;
		Graphics2D g2 = (Graphics2D) g;

		Point pktloc = r.getPkConnectionPoint();
		Point start = new Point(pktloc.x + r.getPkTable().getLocation().x,
								pktloc.y + r.getPkTable().getLocation().y);
 		Point fktloc = r.getFkConnectionPoint();
		Point end = new Point(fktloc.x + r.getFkTable().getLocation().x,
							  fktloc.y + r.getFkTable().getLocation().y);
 		g2.drawLine(start.x, start.y, end.x, end.y);

		logger.debug("Drew line from "+start+" to "+end);

		Color oldColor = g2.getColor();
		g2.setColor(Color.red);
		g2.fillOval(start.x - 2, start.y - 2, 4, 4);
		g2.fillOval(end.x - 2, end.y - 2, 4, 4);
		g2.setColor(oldColor);
	}

	public Point bestConnectionPoint(TablePane tp, Point p) {
		Dimension tpsize = tp.getSize();

		// clip point p to inside of tp
		Point bcp = new Point(Math.max(0, Math.min(tpsize.width, p.x)),
							  Math.max(0, Math.min(tpsize.height, p.y)));
		
		boolean adjustX = bcp.y != 0 && bcp.y != tpsize.height;
		boolean adjustY = bcp.x != 0 && bcp.x != tpsize.width;

		// push x-coordinate to left or right edge of tp, if y-coord is inside tp
		if (adjustX) {
			if (bcp.x < (tpsize.width/2)) {
				bcp.x = 0;
			} else {
				bcp.x = tpsize.width;
			}
		}
		
		// push y-coordinate to top or bottom edge of tp, if x-coord is inside tp
		if (adjustY) {
			if (bcp.y < (tpsize.height/2)) {
				bcp.y = 0;
			} else {
				bcp.y = tpsize.height;
			}
		}

		return bcp;
	}

	// --------------- PropertyChangeListener ----------------------
	public void propertyChange(PropertyChangeEvent e) {
		logger.debug("BasicRelationshipUI notices change of "+e.getPropertyName()
					 +" from "+e.getOldValue()+" to "+e.getNewValue()+" on "+e.getSource());
	}
}
