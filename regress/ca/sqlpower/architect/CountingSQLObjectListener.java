package regress.ca.sqlpower.architect;

import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLObjectListener;

/**
 * Helps with testing SQLObject methods that should fire SQLObjectEvents.
 * 
 * @version $Id$
 */
public class CountingSQLObjectListener implements SQLObjectListener {
	
	/**
	 * The number of times dbChildredInserted has been called.
	 */
	private int insertedCount;

	/**
	 * The number of times dbChildredRemoved has been called.
	 */
	private int removedCount;
	
	/**
	 * The number of times dbObjectChanged has been called.
	 */
	private int changedCount;
	
	/**
	 * The number of times dbStructureChanged has been called.
	 */
	private int structureChangedCount;
	
	/**
	 * The last SQLObjectEvent that was received by this listener
	 */
	private SQLObjectEvent lastEvent;
	
	// ============= SQLObjectListener Implementation ==============
	
	/**
	 * Increments the insertedCount.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		lastEvent=e;
		insertedCount++;
	}
	
	/**
	 * Increments the removedCount.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		lastEvent=e;
		removedCount++;
	}
	
	/**
	 * Increments the changedCount.
	 */
	public void dbObjectChanged(SQLObjectEvent e) {
		lastEvent=e;
		changedCount++;
	}
	
	/**
	 * Increments the structureChangedCount.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		lastEvent=e;
		structureChangedCount++;
	}
	
	
	// =========== Getters ============
	
	/**
	 * See {@link #changedCount}.
	 */
	public int getChangedCount() {
		return changedCount;
	}
	
	/**
	 * See {@link #insertedCount}.
	 */
	public int getInsertedCount() {
		return insertedCount;
	}
	
	/**
	 * See {@link #removedCount}.
	 */
	public int getRemovedCount() {
		return removedCount;
	}
	
	/**
	 * See {@link #structureChangedCount}.
	 */
	public int getStructureChangedCount() {
		return structureChangedCount;
	}

	/**
	 * See {@link #lastEvent}
	 */
	public SQLObjectEvent getLastEvent() {
		return lastEvent;
	}
	
}
