package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;

public class TableProfileManagerTest extends TestCase {
    TableProfileManager m;
    
    TableProfileResult r1;
    SQLTable t1;
    TableProfileResult r2;
    SQLTable t2;
    TableProfileResult r3;
    SQLTable t3;
    TableProfileResult r4;
    SQLTable t4;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m = new TableProfileManager();
        t1 = new SQLTable();
        r1 = new TableProfileResult(t1,m);
        t2 = new SQLTable();
        r2 = new TableProfileResult(t2,m);
        t3 = new SQLTable();
        r3 = new TableProfileResult(t3,m);
        t4 = new SQLTable();
        r4 = new TableProfileResult(t4,m);
    }
    
    public void testLoadCountInSync(){
        m.loadResult(r1);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        m.removeProfile(r1);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
    }
    
    public void testCreateCountInSync() throws SQLException, ArchitectException{
        TableProfileResult tpr = m.createProfile(t1);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        m.removeProfile(tpr);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
    }
       
    public void testLoadManyCountInSync(){
        List list = new ArrayList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
        m.loadManyResults(list);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r1);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r2);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r3);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.removeProfile(r4);
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertFalse("Table t4 in profile",m.isTableProfiled(t4));
    }
    
    public void testClearCountInSync(){
        List list = new ArrayList();
        list.add(r1);
        list.add(r2);
        list.add(r3);
        list.add(r4);
        m.loadManyResults(list);
        assertTrue("Table t1 not in profile",m.isTableProfiled(t1));
        assertTrue("Table t2 not in profile",m.isTableProfiled(t2));
        assertTrue("Table t3 not in profile",m.isTableProfiled(t3));
        assertTrue("Table t4 not in profile",m.isTableProfiled(t4));
        m.clear();
        assertFalse("Table t1 in profile",m.isTableProfiled(t1));
        assertFalse("Table t2 in profile",m.isTableProfiled(t2));
        assertFalse("Table t3 in profile",m.isTableProfiled(t3));
        assertFalse("Table t4 in profile",m.isTableProfiled(t4));
    }
    
    
}
