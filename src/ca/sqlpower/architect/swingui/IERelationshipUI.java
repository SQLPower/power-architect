package ca.sqlpower.architect.swingui;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import org.apache.log4j.Logger;

public class IERelationshipUI extends BasicRelationshipUI {
	private static Logger logger = Logger.getLogger(IERelationshipUI.class);

	public static ComponentUI createUI(JComponent c) {
		logger.debug("Creating new IERelationshipUI for "+c);
        return new IERelationshipUI();
    }

	/**
	 * Paints relationship line terminations based on the IE diagram
	 * language.
	 */
	protected void paintTerminations(Graphics2D g2, Point parent, Point child, int orientation) {
		if ( (orientation & PARENT_FACES_LEFT) != 0) {
			g2.drawLine(parent.x - 10, parent.y - 5, parent.x - 10, parent.y + 5);
		} else if ( (orientation & PARENT_FACES_RIGHT) != 0) {
			g2.drawLine(parent.x + 10, parent.y - 5, parent.x + 10, parent.y + 5);
		} else if ( (orientation & PARENT_FACES_TOP) != 0) {
			g2.drawLine(parent.x - 5, parent.y - 10, parent.x + 5, parent.y - 10);
		} else if ( (orientation & PARENT_FACES_BOTTOM) != 0) {
			g2.drawLine(parent.x - 5, parent.y + 10, parent.x + 5, parent.y + 10);
		}

		if ( (orientation & CHILD_FACES_RIGHT) != 0) {
			g2.drawLine(child.x + 10, child.y, child.x, child.y + 5);
			g2.drawLine(child.x + 10, child.y, child.x, child.y - 5);
			g2.drawOval(child.x + 10, child.y - 3, 6, 6);
		} else if ( (orientation & CHILD_FACES_LEFT) != 0) {
			g2.drawLine(child.x - 10, child.y, child.x, child.y + 5);
			g2.drawLine(child.x - 10, child.y, child.x, child.y - 5);
			g2.drawOval(child.x - 16, child.y - 3, 6, 6);
		} else if ( (orientation & CHILD_FACES_TOP) != 0) {
			g2.drawLine(child.x - 5, child.y, child.x, child.y - 10);
			g2.drawLine(child.x + 5, child.y, child.x, child.y - 10);
			g2.drawOval(child.x - 3, child.y - 16, 6, 6);
		} else if ( (orientation & CHILD_FACES_BOTTOM) != 0) {
			g2.drawLine(child.x - 5, child.y, child.x, child.y + 10);
			g2.drawLine(child.x + 5, child.y, child.x, child.y + 10);
			g2.drawOval(child.x - 3, child.y + 10, 6, 6);
		}			
	}

	public int getParentTerminationLength() {
		return 20;
	}

	public int getChildTerminationLength() {
		return 14;
	}
}
