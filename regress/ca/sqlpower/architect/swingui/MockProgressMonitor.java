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
