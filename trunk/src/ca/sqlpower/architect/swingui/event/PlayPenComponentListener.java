package ca.sqlpower.architect.swingui.event;


public interface PlayPenComponentListener {

	/**
	 * Identifies when components in the playpen begin to move
	 */
	public void componentMoveStart(PlayPenComponentEvent e);
	/**
	 * Identifies when components in the playpen stop the move
	 */
	public void componentMoveEnd(PlayPenComponentEvent e);
	
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