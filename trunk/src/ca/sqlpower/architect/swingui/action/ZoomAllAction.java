package ca.sqlpower.architect.swingui.action;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.PlayPenComponent;

public class ZoomAllAction extends AbstractArchitectAction {
    public ZoomAllAction(ArchitectSwingSession session) {
        super(session, "Zoom to fit", "Zoom to fit", "zoom_fit");
    }

    public void actionPerformed(ActionEvent e) {
        Rectangle rect = null;
        if ( playpen != null ) {
            for (int i = 0; i < playpen.getContentPane().getComponentCount(); i++) {
                PlayPenComponent ppc = playpen.getContentPane().getComponent(i);
                if ( rect == null ) {
                    rect = new Rectangle(ppc.getLocation(),ppc.getSize());
                }
                else {
                    rect.add(ppc.getBounds());
                }
            }
        }
    
        if ( rect == null )
            return;
    
        double zoom = Math.min(playpen.getViewportSize().getHeight()/rect.height,
                playpen.getViewportSize().getWidth()/rect.width);
        zoom *= 0.90;
    
        playpen.setZoom(zoom);
        playpen.scrollRectToVisible(playpen.zoomRect(rect));
    }
}