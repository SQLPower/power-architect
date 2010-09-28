package ca.sqlpower.architect.swingui.event;


public interface PlayPenComponentListener {
	
	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentMoved(PlayPenComponentEvent e);

	/**
	 * Recalculates the connection points if the event was generated
	 * by pkTable or fkTable.
	 */
	public void componentResized(PlayPenComponentEvent e);

}