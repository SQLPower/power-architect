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

package ca.sqlpower.architect.diff;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;

/**
 * Simple test cases to test the SQLIndexComparator that performs indices comparisons. 
 *
 */
public class SQLIndexComparatorTest extends TestCase {
    
    private SQLIndexComparator indComparator = new SQLIndexComparator();
    private Comparator<SQLObject> comparator = new SQLObjectComparator();
    
    private SQLIndex left;
    private SQLIndex right;
    
    protected void setUp() throws Exception {
        left = makeIndex(3);
        right = makeIndex(3);
    }
    
    public void testCompareIndexWithNulls() {
        assertEquals("Both source and target are null.", 0, indComparator.compare(null, null));
        assertTrue("The source is null.", indComparator.compare(null, left) < 0);
        assertTrue("The target is null.", indComparator.compare(left, null) > 0);
        assertEquals("Should be same index.", 0, indComparator.compare(left, right));
    }
    
    public void testCompareByQualifier() {
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        left.setQualifier("cool_table");
        right.setQualifier("cool_table");
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        right.setQualifier("lame_table");
        assertTrue("Should compare as different.", indComparator.compare(left, right) != 0);
    }
    
    public void testCompareByType() {
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        left.setType("cool_type");
        right.setType("cool_type");
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        right.setType("lame_type");
        assertTrue("Should compare as different.", indComparator.compare(left, right) != 0);
    }
    
    public void testCompareByFilterCondition() {
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        left.setFilterCondition("WHERE animal_type = 'cow'");
        right.setFilterCondition("WHERE animal_type = 'cow'");
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        right.setFilterCondition("WHERE animal_type = 'phoenix'");
        assertTrue("Should compare as different.", indComparator.compare(left, right) != 0);
    }
    
    public void testCompareByUnique() {
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        left.setUnique(true);
        assertTrue("Should compare as source > target.", indComparator.compare(left, right) > 0);
        
        right.setUnique(true);
        assertEquals("Unique should be same.", 0, indComparator.compare(left, right));
        
        left.setUnique(false);
        assertTrue("Should compare as source < target.", indComparator.compare(left, right) < 0);
    }
    
    public void testCompareByClustered() {
        assertEquals("Should compare as same.", 0, indComparator.compare(left, right));
        
        left.setClustered(true);
        assertTrue("Should compare as source > target.", indComparator.compare(left, right) > 0);
        
        right.setClustered(true);
        assertEquals("Clustered should be same.", 0, indComparator.compare(left, right));
        
        left.setClustered(false);
        assertTrue("Should compare as source < target.", indComparator.compare(left, right) < 0);
    }
    
    public void testCompareByPrimaryKeyIndex() throws ArchitectException {
        SQLIndex ind1 = new SQLIndex();
        SQLIndex ind2 = new SQLIndex();
        
        SQLColumn col = new SQLColumn();
        col.setName("cool_col");
        
        ind1.addIndexColumn(col, AscendDescend.UNSPECIFIED);
        ind2.addIndexColumn(col, AscendDescend.UNSPECIFIED);
        
        assertEquals("Should compare as same.", 0, indComparator.compare(ind1, ind2));
        
        ind1.setPrimaryKeyIndex(true);
        assertTrue("Should compare as source > target.", indComparator.compare(ind1, ind2) > 0);
        
        ind2.setPrimaryKeyIndex(true);
        assertEquals("PrimaryKeyIndex should be same.", 0, indComparator.compare(ind1, ind2));
        
        ind1.setPrimaryKeyIndex(false);
        assertTrue("Should compare as source < target.", indComparator.compare(ind1, ind2) < 0);
    }
    
    /**
     * Tests the method in SQLIndexComparator used to compare SQLIndex.Column's.
     */
    public void testCompareColumns() {
        Set<Column> list1 = generateColumnList(3, AscendDescend.UNSPECIFIED);
        Set<Column> list2 = generateColumnList(3, AscendDescend.UNSPECIFIED);
        
        assertEquals("Should be same column list.", 0, indComparator.compareColumns(list1, list2));
        
        list1.add(new SQLIndex().new Column("new_col", AscendDescend.UNSPECIFIED));
        assertTrue(indComparator.compareColumns(list1, list2) > 0);        
        assertTrue(indComparator.compareColumns(list2, list1) < 0);
        
        list1 = generateColumnList(3, AscendDescend.ASCENDING);
        assertEquals("Should match AscenDescend.compareTo().", AscendDescend.ASCENDING
                .compareTo(AscendDescend.UNSPECIFIED), indComparator.compareColumns(list1, list2));
        assertEquals("Should match AscenDescend.compareTo().", AscendDescend.UNSPECIFIED
                .compareTo(AscendDescend.ASCENDING), indComparator.compareColumns(list2, list1));

        list1 = generateColumnList(3, AscendDescend.DESCENDING);
        assertEquals("Should match AscenDescend.compareTo().", AscendDescend.DESCENDING
                .compareTo(AscendDescend.UNSPECIFIED), indComparator.compareColumns(list1, list2));
        assertEquals("Should match AscenDescend.compareTo().", AscendDescend.UNSPECIFIED
                .compareTo(AscendDescend.DESCENDING), indComparator.compareColumns(list2, list1));
    }

    /**
     * Tests the null-safe String comparison method in SQLIndexComparator.
     */
    public void testCompareStringsWithNulls() {
        assertEquals("Both source and target column lists are null.", 0, indComparator.compareString(null, null));
        assertTrue("The source is null.", indComparator.compareString(null, "chocolate_almonds") < 0);
        assertTrue("The target is null.", indComparator.compareString("chocolate_almonds", null) > 0);
        assertEquals("Should be same column list.", 0, indComparator.compareString("chocolate_almonds",
                "chocolate_almonds"));
        assertEquals("Should match String.compareTo().", "chocolate_almonds".compareTo("chocolate_raisins"),
                indComparator.compareString("chocolate_almonds", "chocolate_raisins"));
    }
    
    /**
     * Utility method to make a SQLIndex with the given number of Column's.
     * The Column's will have names in the format "col_i" and UNSPECIFIED as
     * ascend/descend.
     */
    private SQLIndex makeIndex(int num) throws ArchitectException {
        SQLIndex ind = new SQLIndex();
        for (int i = 0; i < num; i++) {
            ind.addChild(ind.new Column("col_" + i, AscendDescend.UNSPECIFIED));
        }
        return ind;
    }
    
    /**
     * Utility method to generate a list of SQLIndex.Column's. The list will be
     * of the given size and each Column's ascend/descend will be set to the
     * given ascDec.
     */
    public Set<Column> generateColumnList(int num, AscendDescend ascDes) {
        Set<Column> colList = new TreeSet<Column>(comparator);
        SQLIndex ind = new SQLIndex();
        for (int i = 0; i < num; i++){
            colList.add(ind.new Column("col_" + i, ascDes)); 
        }       
        return colList;
    }
}
