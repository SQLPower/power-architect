/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.architect.undo;

import junit.framework.TestCase;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObjectEvent;
import ca.sqlpower.architect.SQLTable.Folder;

public class SQLObjectChildrenTest extends TestCase {
    
    public class TestSQLObjectChildren extends SQLObjectChildren {

        public TestSQLObjectChildren() {
            super();
        }
        
        @Override
        public void createToolTip() {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    public void testAddChildren() throws Exception {
        Folder<SQLColumn> folder = new Folder<SQLColumn>(Folder.COLUMNS, true);
        SQLColumn column1 = new SQLColumn();
        column1.setName("cow");
        SQLColumn column2 = new SQLColumn();
        column2.setName("chicken");
        SQLColumn column3 = new SQLColumn();
        column3.setName("fish");
        SQLColumn column4 = new SQLColumn();
        column4.setName("sheep");
        SQLColumn column5 = new SQLColumn();
        column5.setName("dog");
        SQLColumn column6 = new SQLColumn();
        column6.setName("cat");
        SQLColumn column7 = new SQLColumn();
        column7.setName("bear");
        SQLColumn[] columnArray = {column1,column2,column3,column4,column5,column6,column7};
        int[] intArray = {0,1};
        SQLObjectEvent event = new SQLObjectEvent(folder,intArray,columnArray);
        SQLObjectChildren objectChildren = new TestSQLObjectChildren();
        objectChildren.createEditFromEvent(event);
        objectChildren.addChildren();
        
        assertEquals(2, folder.getChildCount());
        assertEquals(column1, folder.getChild(0));
        assertEquals(column2, folder.getChild(1));
    }
    
    public void testRemoveChildren() throws Exception {
        Folder<SQLColumn> folder = new Folder<SQLColumn>(Folder.COLUMNS, true);
        SQLColumn column1 = new SQLColumn();
        column1.setName("cow");
        SQLColumn column2 = new SQLColumn();
        column2.setName("chicken");
        SQLColumn column3 = new SQLColumn();
        column3.setName("fish");
        SQLColumn column4 = new SQLColumn();
        column4.setName("sheep");
        SQLColumn column5 = new SQLColumn();
        column5.setName("dog");
        SQLColumn column6 = new SQLColumn();
        column6.setName("cat");
        SQLColumn column7 = new SQLColumn();
        column7.setName("bear");
        SQLColumn[] columnArray = {column1,column2,column3,column4,column5,column6,column7};
        int[] intArray = {0,1,2,3,4};
        SQLObjectEvent event = new SQLObjectEvent(folder,intArray,columnArray);
        SQLObjectChildren objectChildren = new TestSQLObjectChildren();
        objectChildren.createEditFromEvent(event);
        objectChildren.addChildren();
        
        assertEquals(5, folder.getChildCount());
        
        int[] Array = {1,2};
        event = new SQLObjectEvent(folder,Array,columnArray);
        objectChildren = new TestSQLObjectChildren();
        objectChildren.createEditFromEvent(event);
        objectChildren.removeChildren();
        
        
        assertEquals(3,folder.getChildCount());
        assertEquals(column1,folder.getChild(0));
        assertEquals(column4,folder.getChild(1));
        assertEquals(column5,folder.getChild(2));
        
        
    }

}
