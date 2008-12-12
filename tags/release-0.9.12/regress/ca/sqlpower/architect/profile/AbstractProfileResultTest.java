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
package ca.sqlpower.architect.profile;

import java.util.Date;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;

public class AbstractProfileResultTest extends TestCase {
    private class TestingAbstractProfileResult extends AbstractProfileResult<SQLObject> {

        public TestingAbstractProfileResult(SQLObject s) {
            super(s);
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
