package ca.sqlpower.architect.swingui;

import javax.swing.plaf.ComponentUI;
import ca.sqlpower.architect.ArchitectException;

public abstract class TablePaneUI extends ComponentUI implements java.io.Serializable {
	public static final String UI_CLASS_ID = "TablePaneUI";

	/**
	 * This delegate method is specified by TablePane.pointToColumnIndex().
	 */
	public abstract int pointToColumnIndex(java.awt.Point p) throws ArchitectException;
}
