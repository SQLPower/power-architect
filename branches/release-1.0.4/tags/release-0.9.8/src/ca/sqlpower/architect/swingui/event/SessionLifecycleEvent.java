/*
 * Created on Jul 25, 2007
 *
 * This code belongs to SQL Power Group Inc.
 */
package ca.sqlpower.architect.swingui.event;

import java.awt.AWTEvent;

import ca.sqlpower.architect.ArchitectSession;

public class SessionLifecycleEvent extends AWTEvent {

    private static int nextID = 0;
    
    /**
     * Creates a new session lifecycle event.
     * 
     * @param source The Architect Session to which this event applies.
     */
    public SessionLifecycleEvent(Object source) {
        super(source, nextID++);
        if (! (source instanceof ArchitectSession)) {
            throw new ClassCastException("Event must only apply to an ArchitectSession");
        }
    }

    @Override
    public ArchitectSession getSource() {
        return (ArchitectSession) super.getSource();
    }
}
