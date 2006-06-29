package ca.sqlpower.architect.ddl;

import java.util.ArrayList;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.swingui.action.ExportDDLAction.DDLWarningTableModel;
import junit.framework.TestCase;

public class TestDDLWarningsTableModel extends TestCase {
    DDLWarningTableModel tm;
    
    
    public void testNameChangeWarning() throws ArchitectException {
        SQLTable t = new SQLTable(null,"name","remarks","TABLE",true);
        t.setPhysicalName("name");
        NameChangeWarning change = new NameChangeWarning(t,"This is my reason","name");
        ArrayList list = new ArrayList();
        list.add(change);
        
        tm = new DDLWarningTableModel(list);
        tm.setValueAt("new_name",0,4);
        assertEquals("The name column fails to update","new_name",tm.getValueAt(0,1));
        assertEquals("The editable column fails to update","new_name",tm.getValueAt(0,4));
        
    }
    
    
    @Override
    protected void tearDown() throws Exception {
        tm = null;
        super.tearDown();
    }

}
