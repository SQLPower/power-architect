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
package ca.sqlpower.architect.diff;

import java.util.Comparator;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.StubSQLObject;

public class SQLComparatorTest extends TestCase {
	Comparator<SQLObject> comparator = new SQLObjectComparator();

	SQLObject o1 = new StubSQLObject();

	public void testForNull() {
		assertEquals(0, comparator.compare(null, null));
		assertEquals(0, comparator.compare(o1, o1));
	}

	public void testForConsistentWithEquals() {
		// assertEquals(comparator.compare((Object)e1, (Object)e2)==0),
		// e1.equals((Object)e2)
	}
	
	public void testForObjectCompareToNull() throws ArchitectException{
		SQLTable t = new SQLTable();
		t.setName("Testing");
		assertEquals (1, comparator.compare(t, null));
		assertEquals (-1, comparator.compare(null, t));		
	}
	
	public void testForObjectCompareToObject() throws ArchitectException{
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		t1.setName("cow");
		t2.setName("pigs");
		assertTrue( comparator.compare(t1,t2) < 0);
	
		
	}
	
	public void testWithNullName() throws ArchitectException {
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		assertEquals(0, comparator.compare(t1,t2));		
	}
	
	public void testWithSameName() throws ArchitectException{
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		t1.setName("cow");
		t2.setName("cow");
		assertEquals( 0, comparator.compare(t1,t2));
	}
}
