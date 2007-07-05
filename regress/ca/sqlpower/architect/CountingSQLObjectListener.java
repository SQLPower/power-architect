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


/**
 * Helps with testing SQLObject methods that should fire SQLObjectEvents.
 * 
 * @version $Id$
 */
public class CountingSQLObjectListener implements SQLObjectListener {
	
	/**
	 * The number of times dbChildredInserted has been called.
	 */
	private int insertedCount;

	/**
	 * The number of times dbChildredRemoved has been called.
	 */
	private int removedCount;
	
	/**
	 * The number of times dbObjectChanged has been called.
	 */
	private int changedCount;
	
	/**
	 * The number of times dbStructureChanged has been called.
	 */
	private int structureChangedCount;
	
	/**
	 * The last SQLObjectEvent that was received by this listener
	 */
	private SQLObjectEvent lastEvent;
	
	// ============= SQLObjectListener Implementation ==============
	
	/**
	 * Increments the insertedCount.
	 */
	public void dbChildrenInserted(SQLObjectEvent e) {
		lastEvent=e;
		insertedCount++;
	}
	
	/**
	 * Increments the removedCount.
	 */
	public void dbChildrenRemoved(SQLObjectEvent e) {
		lastEvent=e;
		removedCount++;
	}
	
	/**
	 * Increments the changedCount.
	 */
	public void dbObjectChanged(SQLObjectEvent e) {
		lastEvent=e;
		changedCount++;
	}
	
	/**
	 * Increments the structureChangedCount.
	 */
	public void dbStructureChanged(SQLObjectEvent e) {
		lastEvent=e;
		structureChangedCount++;
	}
	
	
	// =========== Getters ============
	
	/**
	 * See {@link #changedCount}.
	 */
	public int getChangedCount() {
		return changedCount;
	}
	
	/**
	 * See {@link #insertedCount}.
	 */
	public int getInsertedCount() {
		return insertedCount;
	}
	
	/**
	 * See {@link #removedCount}.
	 */
	public int getRemovedCount() {
		return removedCount;
	}
	
	/**
	 * See {@link #structureChangedCount}.
	 */
	public int getStructureChangedCount() {
		return structureChangedCount;
	}

	/**
	 * See {@link #lastEvent}
	 */
	public SQLObjectEvent getLastEvent() {
		return lastEvent;
	}
	
}
