package ca.sqlpower.architect.swingui;

import javax.swing.plaf.ComponentUI;
import ca.sqlpower.architect.ArchitectException;
import java.awt.Point;

public abstract class RelationshipUI extends ComponentUI implements java.io.Serializable {
	public static final String UI_CLASS_ID = "RelationshipUI";

	public abstract Point bestConnectionPoint(TablePane tp, Point p);
}
