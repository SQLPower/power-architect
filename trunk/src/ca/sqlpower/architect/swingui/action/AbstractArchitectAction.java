package ca.sqlpower.architect.swingui.action;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.ArchitectSwingSession;
import ca.sqlpower.architect.swingui.ArchitectSwingSessionContext;
import ca.sqlpower.architect.swingui.PlayPen;

/**
 * A set of basic functionality that all actions in the Architect
 * rely on.
 */
public abstract class AbstractArchitectAction extends AbstractAction {

    protected final ArchitectFrame frame;
    protected final PlayPen playpen;
    protected final ArchitectSwingSession session;
    
    /**
     * Helper constructor that all architect action subclasses that use an icon will call.
     * Ensures that the session, its frame, and its frame's playpen are
     * all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * @param iconResourceName The resource name of the icon. See
     * {@link ASUtils#createIcon(String, String)} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription,
            String iconResourceName) {
        
        super(actionName,
                iconResourceName == null ?
                        null :
                        ASUtils.createIcon(iconResourceName, actionName, ArchitectSwingSessionContext.ICON_SIZE));
        putValue(SHORT_DESCRIPTION, actionDescription);

        this.session = session;
        if (session == null) throw new NullPointerException("Null session");

        this.frame = session.getArchitectFrame();
        if (frame == null) throw new NullPointerException("Null parentFrame");
        
        this.playpen = frame.getPlayPen();
        if (playpen == null) throw new NullPointerException("Null playpen");
        

    }
    
    /**
     * Helper constructor that all architect action subclasses that do not
     * use an icon will call. Ensures that the session, its frame, and its 
     * frame's playpen are all non-null.
     * 
     * @param session The session that this action will operate on. Must not be null.
     * @param actionName The name for this action. This will appear in menu items.
     * @param actionDescription This action's description. Appears in tooltips.
     * {@link ASUtils#createIcon(String, String)} for details.
     */
    public AbstractArchitectAction(
            ArchitectSwingSession session,
            String actionName,
            String actionDescription) {
        this(session, actionName, actionDescription, null);
    }
}
