package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;


public class ZoomAction extends AbstractAction implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger(ZoomAction.class);

	protected PlayPen playpen;
	protected double zoomStep;

	public static final String ZOOM_IN = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_IN";
	public static final String ZOOM_OUT = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_OUT";

	public ZoomAction(double amount) {
		super(amount > 0.0 ? "Zoom In" : "Zoom Out",
			  ASUtils.createJLFIcon(amount > 0.0 ? "general/ZoomIn" : "general/ZoomOut",
									amount > 0.0 ? "Zoom In" : "Zoom Out",
									ArchitectFrame.getMainInstance().sprefs.getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(AbstractAction.SHORT_DESCRIPTION, amount > 0.0 ? "Zoom In" : "Zoom Out");
		this.zoomStep = amount;
	}
		
	public void actionPerformed(ActionEvent e) {
		logger.debug("oldZoom="+playpen.getZoom()+",zoomStep="+zoomStep);
		playpen.setZoom(playpen.getZoom() + zoomStep);
		logger.debug("newZoom="+playpen.getZoom());
		Rectangle scrollTo = null;
		Iterator it = playpen.getSelectedItems().iterator();
		while (it.hasNext()) {
			Rectangle bounds = ((Component) it.next()).getBounds();
			logger.debug("new rectangle, bounds: " + bounds);
			if (scrollTo == null) {
				scrollTo = new Rectangle(bounds);
			} else {
				logger.debug("added rectangles, new bounds: " + scrollTo); 
				scrollTo.add(bounds);
			}
		}
		if (scrollTo != null && !scrollTo.isEmpty()) {
			playpen.zoomRect(scrollTo);
			playpen.scrollRectToVisible(scrollTo);
		}
	}

	public void setPlayPen(PlayPen pp) {
		if (playpen != null) {
			playpen.removePropertyChangeListener(this);
		}
		playpen = pp;
		playpen.addPropertyChangeListener(this);
	}

	public void propertyChange(PropertyChangeEvent e) {
		if ("zoom".equals(e.getPropertyName())) {
			if (playpen.getZoom() + zoomStep < 0.1) {
				setEnabled(false);
			} else {
				setEnabled(true);
			}
		}
	}
}
