package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class ZoomAction extends AbstractAction implements PropertyChangeListener {

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
		playpen.setZoom(playpen.getZoom() + zoomStep);
		if (playpen.getSelection() != null) {
			Rectangle r = ((Component) playpen.getSelection()).getBounds();
			playpen.zoomRect(r);
			playpen.scrollRectToVisible(r);
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
