package ca.sqlpower.architect.swingui;

public interface SessionLifecycleListener {
    
    /**
     * This is called when the session is closing.
     */
    public void sessionClosing();

}
