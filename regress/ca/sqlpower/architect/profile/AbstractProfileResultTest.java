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
        public void doProfile() throws SQLException, ArchitectException {
            // TODO Auto-generated method stub
            
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
