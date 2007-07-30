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
package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.Date;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import junit.framework.TestCase;

public class AbstractProfileResultTest extends TestCase {
    private class TestingAbstractProfileResult extends AbstractProfileResult<SQLObject> {

        public TestingAbstractProfileResult(SQLObject s) {
            super(s);
        }
        
        @Override
        protected void doProfile() throws SQLException, ArchitectException {
            // nothing to do for stub profile
        }
    }
    
    public void testCompareToWithDifferentTimes() {
        SQLTable t = new SQLTable();
        t.setName("name");
        long curTime = new Date().getTime();
        TestingAbstractProfileResult result1 = new TestingAbstractProfileResult(t);
        result1.setCreateEndTime(curTime);
        
        TestingAbstractProfileResult result2 = new TestingAbstractProfileResult(t);
        result2.setCreateEndTime(curTime+1000);
        
        assertFalse("compareTo ignores time", result1.compareTo(result2) == 0);
    }
    
    public void testEqualsToWithDifferentTimes() {
        SQLTable t = new SQLTable();
        t.setName("name");
        long curTime = new Date().getTime();
        TestingAbstractProfileResult result1 = new TestingAbstractProfileResult(t);
        result1.setCreateEndTime(curTime);
        
        TestingAbstractProfileResult result2 = new TestingAbstractProfileResult(t);
        result2.setCreateEndTime(curTime+1000);
        
        assertFalse("compareTo ignores time", result1.equals(result2));

    }

}
