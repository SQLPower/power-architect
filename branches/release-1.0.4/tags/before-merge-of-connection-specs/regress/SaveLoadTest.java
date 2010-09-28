/*
 * Created on Jun 21, 2005
 *
 * This code belongs to SQL Power Group Inc.
 */
package regress;

import java.sql.DatabaseMetaData;
import java.sql.Types;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUIProject;
import junit.framework.TestCase;

/**
 * The SaveLoadTest tests saving and loading a project.
 *
 * @author fuerth
 * @version $Id$
 */
public class SaveLoadTest extends TestCase {

    private SwingUIProject project;

    static {
        try {
            // This creates the singleton ArchitectFrame.mainInstance that much of the code is addicted to... :(
            new ArchitectFrame();
        } catch (ArchitectException e) {
            e.printStackTrace();
        }
    }
    
    public SaveLoadTest(String testName) {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = new SwingUIProject("Test Project");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLoad() {
        // TODO: implement test
    }

    public void testSave() {
        SQLDatabase ppdb = project.getTargetDatabase();
        SQLTable t1 = new SQLTable(ppdb, "Table 1", "Table 1 Remarks", "TABLE");
        t1.addColumn(0, new SQLColumn(t1, "t1c0", Types.DECIMAL, "c1 native type", 10, 20, DatabaseMetaData.columnNoNulls, "t1c1 remarks", null, null, false));
        SQLTable t2 = new SQLTable(ppdb, "Table 2", "Table 2 Remarks", "TABLE");
        ppdb.addChild(t1);
        ppdb.addChild(t2);
    }

    public void testGetName() {
        // TODO: implement test
    }

    public void testSetName() {
        // TODO: implement test
    }

    public void testGetSourceDatabases() {
        // TODO: implement test
    }

    public void testSetSourceDatabases() {
        // TODO: implement test
    }

    public void testSetSourceDatabaseList() {
        // TODO: implement test
    }

    public void testGetTargetDatabase() {
        // TODO: implement test
    }

    public void testSetTargetDatabase() {
        // TODO: implement test
    }

    public void testGetFile() {
        // TODO: implement test
    }

    public void testSetFile() {
        // TODO: implement test
    }

    public void testGetPlayPen() {
        // TODO: implement test
    }

    public void testSetPlayPen() {
        // TODO: implement test
    }

    public void testGetDDLGenerator() {
        // TODO: implement test
    }

    public void testSetDDLGenerator() {
        // TODO: implement test
    }

    public void testIsSavingEntireSource() {
        // TODO: implement test
    }

    public void testSetSavingEntireSource() {
        // TODO: implement test
    }

    public void testGetPLExport() {
        // TODO: implement test
    }

    public void testSetPLExport() {
        // TODO: implement test
    }

    public void testIsModified() {
        // TODO: implement test
    }

    public void testSetModified() {
        // TODO: implement test
    }

}
