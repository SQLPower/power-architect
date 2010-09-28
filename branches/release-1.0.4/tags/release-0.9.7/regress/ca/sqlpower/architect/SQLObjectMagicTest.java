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

import junit.framework.TestCase;

/**
 * Test the MagicEnabled stuff in SQLObject
 */
public class SQLObjectMagicTest extends TestCase {

	SQLObject parent;
	SQLObject child = new SQLTable.Folder<SQLColumn>(SQLTable.Folder.COLUMNS, false);
	
	@Override
	protected void setUp() throws Exception {
        parent  = new SQLTable();
		parent.addChild(child);
	}
	
	public void testOneLevel() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
		
	}
	
	public void testMultipleDisables() {
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		child.setMagicEnabled(false);	// twice in a row, just to be sure :-)
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertTrue(child.isMagicEnabled());
	}
	
	public void testParentChild() {
		assertTrue(child.isMagicEnabled());
		parent.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());
		child.setMagicEnabled(true);
		assertFalse(child.isMagicEnabled()); // Because parent still magic-enabled
		parent.setMagicEnabled(true);
		try {
			child.setMagicEnabled(true);		// Should object
		} catch (Throwable e) { 
			System.out.println("Caught expected: " + e);
		}
		assertTrue(child.isMagicEnabled());
		child.setMagicEnabled(false);
		assertFalse(child.isMagicEnabled());	// finally
	}
	
}
