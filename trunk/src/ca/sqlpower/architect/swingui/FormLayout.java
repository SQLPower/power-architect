package ca.sqlpower.architect.swingui;

import java.awt.*;
import javax.swing.*;

public class FormLayout implements LayoutManager {

	int hgap;
	int vgap;

	public FormLayout() {
		this(2,2);
	}

	public FormLayout(int hgap, int vgap) {
		this.hgap = hgap;
		this.vgap = vgap;
	}

	public void addLayoutComponent(String name, Component comp) {
	}

	public void removeLayoutComponent(Component comp) {
	}

	private LeftRightHeight calcSizes(Container parent) {
		int maxLHWidth = 0;
		int maxRHWidth = 0;
		int lHeight = 0;
		int height = 0;
		Dimension d;

		// figure out column widths
		for (int i = 0; i < parent.getComponentCount(); i++) {
			d = parent.getComponent(i).getPreferredSize();
			if (i%2 == 0) {
				// left-hand column
				maxLHWidth = Math.max(maxLHWidth, d.width);
				lHeight = d.height;
			} else {
				// right-hand column
				maxRHWidth = Math.max(maxRHWidth, d.width);
				height += Math.max(lHeight, d.height);
			}
		}
		
		LeftRightHeight lrh = new LeftRightHeight();
		lrh.left = maxLHWidth;
		lrh.right = maxRHWidth;
		lrh.height = height;
		return lrh;
	}

	public Dimension preferredLayoutSize(Container parent) {
		Dimension minimum = minimumLayoutSize(parent);
		minimum.width += 150;
		return minimum;
	}

	public Dimension minimumLayoutSize(Container parent) {
		LeftRightHeight lrh = calcSizes(parent);
		Insets i = parent.getInsets();
		int rows = parent.getComponentCount() / 2;
		return new Dimension(lrh.left + lrh.right + i.left + i.right + hgap,
							 lrh.height + i.top + i.bottom + (vgap*(rows-1)));
	}

	public void layoutContainer(Container parent) {
		LeftRightHeight lrh = calcSizes(parent);
		Dimension size = parent.getSize();
		Insets ins = parent.getInsets();
		
		int lColWidth = Math.min(size.width, lrh.left);
		int rColWidth = size.width - lColWidth - ins.left - ins.right;
		int height = Math.min(size.height, lrh.height);

		Dimension d;
		int lHeight = 0;
		int y = ins.top;
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component c = parent.getComponent(i);
			d = c.getPreferredSize();
			if (i%2 == 0) {
				// left-hand column
				c.setBounds(ins.left, y, lColWidth, d.height);
				lHeight = d.height;
			} else {
				// right-hand column
				c.setBounds(lColWidth + ins.left + hgap, y, rColWidth, d.height);
				y += Math.max(d.height, lHeight) + vgap;
			}
		}		
	}

	/**
	 * A class to hold three numbers: the width of the left column,
	 * width of the right column, and the overall height of all the
	 * rows together.  The calcSizes() method returns an instance of
	 * this class.
	 */
	private class LeftRightHeight {
		int left;
		int right;
		int height;
	}
}
