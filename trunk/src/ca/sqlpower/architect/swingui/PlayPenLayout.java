package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Iterator;

public class PlayPenLayout implements LayoutManager {

	public void addLayoutComponent(String name, Component comp) {
		// don't care
	}

	public void removeLayoutComponent(Component comp) {
		// don't care
	}

	public Dimension preferredLayoutSize(Container parent) {
		return null; // FIXME might not work
	}

	public Dimension minimumLayoutSize(Container parent) {
		return null; // FIXME might not work
	}

	public void layoutContainer(Container parent) {
		PlayPen pp = (PlayPen) parent;

		int minX, minY;
		minX = minY = 0;
		Iterator it = pp.getTablePanes().iterator();
		while (it.hasNext()) {
			TablePane tp = (TablePane) it.next();
			if ( minX > tp.getX() )
				minX = tp.getX();
			if ( minY > tp.getY() )
				minY = tp.getY();
		}
		int newX, newY;
		if ( minX < 0 )
			newX = 0 - minX;
		else
			newX = 0;
		if ( minY < 0 )
			newY = 0 - minY;
		else
			newY = 0;

		if ( newX > 0 || newY > 0 ) {

			it = pp.getTablePanes().iterator();
			while (it.hasNext()) {
				TablePane tp = (TablePane) it.next();
				tp.setLocation(tp.getX()+newX, tp.getY()+newY);
			}
			// This layout manager has expanded the playpen's minimum
			// and preferred sizes, so the original repaint region could be
			// too small!
			pp.repaint();
		}			
	}
}
