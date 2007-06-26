package ca.sqlpower.architect.swingui.action;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPenComponent;


public class ZoomAction extends AbstractArchitectAction implements PropertyChangeListener {
	private static final Logger logger = Logger.getLogger(ZoomAction.class);

	protected double zoomStep;

	public static final String ZOOM_IN = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_IN";
	public static final String ZOOM_OUT = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_OUT";
	public static final String ZOOM_ALL = "ca.sqlpower.architect.swingui.ZoomAction.ZOOM_ALL";

	public ZoomAction(ArchitectSwingSession session, double amount) {
        super(session,
              amount > 0.0 ? "Zoom In" : "Zoom Out",
              amount > 0.0 ? "Zoom In" : "Zoom Out",
              amount > 0.0 ? "zoom_in" : "zoom_out");        
		this.zoomStep = amount;
        playpen.addPropertyChangeListener(this);
	}
		
	public void actionPerformed(ActionEvent e) {
		logger.debug("oldZoom="+playpen.getZoom()+",zoomStep="+zoomStep);
		// 	zoom by a factor of sqrt(2) instead of linear so we can go below 0.1

		// playpen.setZoom(playpen.getZoom() + zoomStep); 
		playpen.setZoom(playpen.getZoom() * Math.pow(2,zoomStep));
		logger.debug("newZoom="+playpen.getZoom());
		Rectangle scrollTo = null;
		Iterator it = playpen.getSelectedItems().iterator();
		while (it.hasNext()) {
			Rectangle bounds = ((PlayPenComponent) it.next()).getBounds();
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

	public void propertyChange(PropertyChangeEvent e) {
		// this used to enable/disable zooming out when the zoom factor got lower than 0.1
	}
}
