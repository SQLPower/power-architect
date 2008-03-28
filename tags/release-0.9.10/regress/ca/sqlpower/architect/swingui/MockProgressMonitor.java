/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.Component;

import javax.accessibility.AccessibleContext;
import javax.swing.ProgressMonitor;

public class MockProgressMonitor extends ProgressMonitor {
	
	private boolean cancelled;

	MockProgressMonitor(Component parent, Object message, String note, int min, int max) {
		super(parent, message, note, min, max);
	}

	// NON-OVERRIDE METHODS - FOR TEST CODE TO USE
	
	public void setCancelled(boolean b) {
		cancelled = b;
	}
	
	// OVERRIDE METHODS
	
	@Override
	public void close() {
		System.out.println("MockProgressMonitor.close()");
	}

	@Override
	public AccessibleContext getAccessibleContext() {
		System.out.println("MockProgressMonitor.getAccessibleContext()");
		return null;
	}

	@Override
	public int getMaximum() {
		System.out.println("MockProgressMonitor.getMaximum()");
		return super.getMaximum();
	}

	@Override
	public int getMillisToDecideToPopup() {
		System.out.println("MockProgressMonitor.getMillisToDecideToPopup()");
		return super.getMillisToDecideToPopup();
	}

	@Override
	public int getMillisToPopup() {
		System.out.println("MockProgressMonitor.getMillisToPopup()");
		return super.getMillisToPopup();
	}

	@Override
	public int getMinimum() {
		System.out.println("MockProgressMonitor.getMinimum()");
		return super.getMinimum();
	}

	@Override
	public String getNote() {
		System.out.println("MockProgressMonitor.getNote()");
		return super.getNote();
	}

	@Override
	public boolean isCanceled() {
		System.out.println("MockProgressMonitor.isCanceled()");
		return cancelled;
	}

	@Override
	public void setMaximum(int m) {
		System.out.println("MockProgressMonitor.setMaximum()");
		super.setMaximum(m);
	}

	@Override
	public void setMillisToDecideToPopup(int millisToDecideToPopup) {
		System.out.println("MockProgressMonitor.setMillisToDecideToPopup()");
		super.setMillisToDecideToPopup(millisToDecideToPopup);
	}

	@Override
	public void setMillisToPopup(int millisToPopup) {
		System.out.println("MockProgressMonitor.setMillisToPopup()");
		super.setMillisToPopup(millisToPopup);
	}

	@Override
	public void setMinimum(int m) {
		System.out.println("MockProgressMonitor.setMinimum()");
		super.setMinimum(m);
	}

	@Override
	public void setNote(String note) {
		System.out.println("MockProgressMonitor.setNote()");
		super.setNote(note);
	}

	@Override
	public void setProgress(int nv) {
		System.out.println("MockProgressMonitor.setProgress()");
		super.setProgress(nv);
	}
	
}
