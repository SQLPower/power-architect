package ca.sqlpower.architect.swingui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.apache.log4j.Logger;

public class MagnifierAwareGlassPane extends JComponent implements MouseListener, MouseMotionListener {
	private static Logger logger = Logger.getLogger(MagnifierAwareGlassPane.class);

	protected JFrame frame;

	public MagnifierAwareGlassPane() {
		super();
		setName("Magnifier aware glass pane");
		addMouseListener(this);
		addMouseMotionListener(this);
		setBackground(Color.green);
		setOpaque(false);
	}

	public void setFrame(JFrame f) {
		this.frame = f;
		setVisible(true);
	}

	protected void retarget(MouseEvent e) {
		Point p = e.getPoint();
		Magnifier mag = null;
		Component c = SwingUtilities.getDeepestComponentAt(frame.getContentPane(), p.y, p.y);
		if (c == null) {
			// this happens when the mouse exits the frame
			logger.debug("Discarding event from null component");
		} else if (c == this) {
			logger.debug("Discarding mouse event over glasspane");
		} else {
			logger.debug("Retargeting mouse event at "+p+" to "+c.getName());
			mag = (Magnifier) SwingUtilities.getAncestorOfClass(Magnifier.class, c);
			if (mag != null) {
				logger.error("Unsupported mouse activity over magnifier");
			} else {
				SwingUtilities.convertPoint(this, p, c);
				c.dispatchEvent(new MouseEvent(c,
											   e.getID(),
											   e.getWhen(),
											   e.getModifiers(),
											   p.x,
											   p.y,
											   e.getClickCount(),
											   e.isPopupTrigger()));
			}
		}
	}

	// ---------------------- Mouse listener ---------------------
	public void mouseClicked(MouseEvent e) {
		retarget(e);
	}
	
	public void mousePressed(MouseEvent e) {
		retarget(e);
	}
		
	public void mouseReleased(MouseEvent e) {
		retarget(e);
	}

	public void mouseEntered(MouseEvent e) {
		retarget(e);
	}

	public void mouseExited(MouseEvent e) {
		retarget(e);
	}

	// ---------------------- Mouse Motion listener ---------------------
	public void mouseDragged(MouseEvent e) {
		retarget(e);
	}

	public void mouseMoved(MouseEvent e) {
		retarget(e);
	}
}
