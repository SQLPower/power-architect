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
package ca.sqlpower.architect;

import ca.sqlpower.architect.undo.UndoCompoundEvent;
import ca.sqlpower.architect.undo.UndoCompoundEventListener;

public class CountingUndoCompoundEventListener implements
		UndoCompoundEventListener, SQLObjectListener {

	// ========= Undo Compound Listener ==========
	private int editDepth;
	private int editsInLastGroup;
	private int editsBeforeLastGroup;
	
	public void compoundEditStart(UndoCompoundEvent e) {
		if (editDepth == 0) {
			editsBeforeLastGroup = liveEdits;
		}
		editDepth++;
	}

	/**
	 * Ends the current compound edit. This method makes sure there was already
	 * a compound edit in progress. If there was not a compound edit in
	 * progress, throws IllegalStateException. In that case, there is either a
	 * bug in the code you're listening to, or you started listening to an
	 * object that was already in the middle of a compound edit.
	 */
	public void compoundEditEnd(UndoCompoundEvent e) {
		if (editDepth == 0) {
			throw new IllegalStateException("Compound edit depth was already 0");
		}
		editDepth--;
		if (editDepth == 0) {
			editsInLastGroup = liveEdits;
			liveEdits = 0;
		}
	}

	/**
	 * Returns how many compound edits have started minus how many
	 * compound edits have finished. Due to fail-fast check on {@link #compoundEditEnd(UndoCompoundEvent)},
	 * this will never be less than 0.
	 */
	public int getEditDepth() {
		return editDepth;
	}

	/**
	 * Returns the number of edits that happened in the last complete
	 * group of compound edits.  Does not count edits inside the current
	 * compound edit (if there is one).
	 */
	public int getEditsInLastGroup() {
		return editsInLastGroup;
	}
	
	/**
	 * Returns the number of edits that happened outside of any compound
	 * group, before the current compound group started.  If not currently
	 * in a compound group ({@link #getEditDepth()} returns 0), this returns
	 * the count from before the last group started, not the current live edit
	 * count.
	 */
	public int getEditsBeforeLastGroup() {
		return editsBeforeLastGroup;
	}

	// ========= SQL Object Listener ==========
	private int liveEdits;
	
	public void dbChildrenInserted(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbChildrenRemoved(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbObjectChanged(SQLObjectEvent e) {
		liveEdits++;
	}

	public void dbStructureChanged(SQLObjectEvent e) {
		liveEdits++;
	}
}
