package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.swingui.event.SessionLifecycleEvent;

public interface SessionLifecycleListener {
    
    /**
     * This is called when the session is closing.
     */
    public void sessionClosing(SessionLifecycleEvent e);

}
