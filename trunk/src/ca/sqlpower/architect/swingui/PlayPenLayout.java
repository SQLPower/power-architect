package ca.sqlpower.architect.swingui;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * Manages the layout of the PlayPen content pane.
 *
 * @see PlayPenContentPane
 */
public class PlayPenLayout implements LayoutManager2 {
	private static final Logger logger = Logger.getLogger(PlayPenLayout.class);

	public PlayPenLayout() {
	}
	
	/**
	 * Does nothing.  Use the Object-style constraints, not String.
	 *
	 * @throws UnsupportedOperationException if called.
	 */
	public void addLayoutComponent(String name, Component comp) {
		throw new UnsupportedOperationException("Use addLayoutComponent(Component,Object) instead");
	}
	
	/**
	 */
	public void addLayoutComponent(Component comp,
								   Object position) {
		comp.setSize(comp.getPreferredSize());
	}
	
	/**
	 */
	protected void translateAllComponents(int xdist, int ydist, boolean scrollToCompensate) {
	}
	
	/**
	 * Does nothing.
	 */
	public void removeLayoutComponent(Component comp) {
		logger.debug("PlayPenLayout.removeLayoutComponent");
	}
	
	/**
	 * Just returns 100x100.
	 */
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(100,100);
	}
	
	/**
	 * Identical to {@link #preferredLayoutSize(Container)}.
	 */
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}
	
	/**
	 * Identical to {@link #preferredLayoutSize(Container)}.
	 */
	public Dimension maximumLayoutSize(Container target) {
		return preferredLayoutSize(target);
	}
	
	public float getLayoutAlignmentX(Container target) {
		return 0.5f;
	}
	
	public float getLayoutAlignmentY(Container target) {
		return 0.5f;
	}
	
	/**
	 * Discards cached layout information.  Currently this is a no-op.
	 */
	public void invalidateLayout(Container target) {
		return;
	}
	
	/**
	 * Does nothing.  Components will stay put.
	 */
	public void layoutContainer(Container parent) {
		logger.debug("PlayPenLayout.layoutContainer");
	}
}
