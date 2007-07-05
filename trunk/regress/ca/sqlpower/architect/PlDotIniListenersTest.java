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

public class PlDotIniListenersTest extends TestCase {
	DataSourceCollection pld = new PlDotIni();
	ArchitectDataSource dbcs = new ArchitectDataSource();
	
	@Override
	protected void setUp() throws Exception {
		dbcs.setDisplayName("Goofus");
	}
	
	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.addDataSource(ArchitectDataSource)'
	 * Test it without any listeners.
	 */
	public void testAddDataSource() {
		assertEquals(0, pld.getConnections().size());
		pld.addDataSource(dbcs);
		assertEquals(1, pld.getConnections().size());
		try {
			pld.addDataSource(dbcs);	// should fail!
			fail("Didn't fail to add a second copy!");
		} catch (IllegalArgumentException e) {
			System.out.println("Caught expected " + e);
		}
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.mergeDataSource(ArchitectDataSource)'
	 */
	public void testMergeDataSource() {
		pld.addDataSource(dbcs);
		dbcs.getParentType().setJdbcDriver("mock.Driver");
		pld.mergeDataSource(dbcs);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.removeDataSource(ArchitectDataSource)'
	 */
	public void testRemoveDataSource() {
		assertEquals(0, pld.getConnections().size());
		pld.addDataSource(dbcs);
		assertEquals(1, pld.getConnections().size());
		assertSame(dbcs, pld.getConnections().get(0));
		pld.removeDataSource(dbcs);
		assertEquals(0, pld.getConnections().size());
	}
	
	DatabaseListChangeEvent addNotified;
	DatabaseListChangeEvent removeNotified;
	
	DatabaseListChangeListener liszt = new DatabaseListChangeListener() {

		public void databaseAdded(DatabaseListChangeEvent e) {
			addNotified = e;
		}

		public void databaseRemoved(DatabaseListChangeEvent e) {
			removeNotified = e;
		}
	};

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.addListener(DatabaseListChangeListener)'
	 */
	public void testAddListener() {
		pld.addDatabaseListChangeListener(liszt);
		assertNull(addNotified);
		pld.addDataSource(dbcs);
		assertNotNull(addNotified);
		System.out.println(addNotified);
	}

	/*
	 * Test method for 'ca.sqlpower.architect.PlDotIni.removeListener(DatabaseListChangeListener)'
	 */
	public void testRemoveListener() {

	}

}
