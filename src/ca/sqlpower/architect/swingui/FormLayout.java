/*
 * Copyright (c) 2007, SQL Power Group Inc.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of SQL Power Group Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.JTextField;

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
        // we don't keep component state
	}

	public void removeLayoutComponent(Component comp) {
        // we don't keep component state
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
		return minimum;
	}

	public Dimension minimumLayoutSize(Container parent) {
		LeftRightHeight lrh = calcSizes(parent);
		Insets i = parent.getInsets();
		int rows = parent.getComponentCount() / 2;
		/*
		return new Dimension(lrh.left + lrh.right + i.left + i.right + hgap,
							 lrh.height + i.top + i.bottom + (vgap*(rows-1)));
				 */

		return new Dimension(lrh.left + lrh.right + i.left + i.right + hgap,
							lrh.height + i.top + i.bottom + (vgap*(rows)));
	}

	/**
	 * Lays out the container as a 2-column form.  Labels go in the
	 * left column and input fields go in the right column.
	 *
	 * <p>The left column will always be as wide as its widest
	 * component (so that the labels always fit), and the right column
	 * will be as wide as leftover space permits.  Leftover space is
	 * defined as the parent container's width minus its left and
	 * right insets, this layout's hgap, and the left column width.
	 */
	public void layoutContainer(Container parent) {
		LeftRightHeight lrh = calcSizes(parent);
		Dimension size = parent.getSize();
		Insets ins = parent.getInsets();
		
		int lColWidth = Math.min(size.width, lrh.left);
		int rColWidth = size.width - ins.left - lColWidth - hgap - ins.right;
		
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
				int width = c.getPreferredSize().width;
				if (c instanceof JTextField || c instanceof JPanel) {
					width = rColWidth; // full width of this column
				}
				c.setBounds(lColWidth + ins.left + hgap, y, width, d.height);
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
