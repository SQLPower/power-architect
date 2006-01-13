package ca.sqlpower.architect.swingui;

import ca.sqlpower.architect.ArchitectException;

public abstract class TablePaneUI implements PlayPenComponentUI, java.io.Serializable {
	public static final String UI_CLASS_ID = "TablePaneUI";

	/**
	 * This delegate method is specified by TablePane.pointToColumnIndex().
	 */
	public abstract int pointToColumnIndex(java.awt.Point p) throws ArchitectException;
	
}
