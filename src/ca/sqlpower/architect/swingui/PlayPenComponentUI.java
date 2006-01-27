package ca.sqlpower.architect.swingui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;

import ca.sqlpower.architect.SQLRelationship;

/**
 * The PlayPenComponentUI is the superclass of all UI delegates for Play Pen components.
 * It provides a pluggable look-and-feel for PlayPenComponents exactly the same way the
 * ComponentUI does for Swing components.
 */
public interface PlayPenComponentUI {
	
    public void installUI(PlayPenComponent c);
    public void uninstallUI(PlayPenComponent c);

	public boolean contains(Point p);
	
	public void paint(Graphics2D g2);

	public Dimension getPreferredSize();
}
