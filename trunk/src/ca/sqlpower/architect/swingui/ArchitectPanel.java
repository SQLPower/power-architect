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

import javax.swing.JComponent;

/**
 * The ArchitectPanel interface defines the contract between a panel
 * of components that help the user edit the data model and its parent
 * frame.  Classes that implement ArchitectPanel require that exactly
 * one of the two methods {@link #applyChanges()} or {@link
 * #discardChanges()} are called at the end of the panel's lifetime.
 * After affecting the model in the specified way, these methods will
 * free up resources associated with the panel (such as removing the
 * panel from listener lists).  After calling {@link #applyChanges()}
 * or {@link #discardChanges()} on an instance of ArchitectPanel, it
 * may not be used anymore.
 *
 * <p>Remember that it's important to call one of these methods
 * (usually discardChanges()) when a containing frame's window gets
 * closed by the native window system.
 */
public interface ArchitectPanel {

	/**
	 * An OK button in the panel's containing frame should invoke this
	 * method.
	 */
	public boolean applyChanges();

	/**
	 * A cancel button in the panel's containing frame should invoke
	 * this method.
	 */
	public void discardChanges();
	
	/**
	 * @return This ArchitectPanel's JPanel
	 */
	public JComponent getPanel();
}
