package ca.sqlpower.architect;

import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.SQLIndex.IndexType;

public class TestSQLIndex extends SQLTestCase {

    private SQLIndex index;
    private SQLColumn col1;
    
    public TestSQLIndex(String name) throws Exception {
        super(name);
        propertiesToIgnoreForEventGeneration.add("parentTable");
        propertiesToIgnoreForUndo.add("parentTable");
    }

    protected void setUp() throws Exception {
        super.setUp();
        index = new SQLIndex("Test Index",true,"a",IndexType.HASHED,"b");
        col1 = new SQLColumn();
        SQLColumn col2 = new SQLColumn();
        SQLColumn col3 = new SQLColumn();
        index.addIndexColumn(col1, true, true);
        index.addIndexColumn(col2, false, true);
        index.addIndexColumn(col3, true, false);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected SQLObject getSQLObjectUnderTest() {
       
        return index;
    }

    /**
     * When you add an index column, it should attach a listener to its target column.
     */
    public void testReAddColumnAddsListener() throws Exception {
        System.out.println("Original listeners:       "+col1.getSQLObjectListeners());
        int origListeners = col1.getSQLObjectListeners().size();
        SQLIndex.Column removed = (Column) index.removeChild(0);
        index.addChild(removed);
        System.out.println("Post-remove-add listeners: "+col1.getSQLObjectListeners());
        assertEquals(origListeners, col1.getSQLObjectListeners().size());
    }
    
    /**
     * When you remove a column from an index, it has to unsubscribe its
     * listener from its target column.
     */
    public void testRemoveColumnNoListenerLeak() {
        System.out.println("Original listeners:    "+col1.getSQLObjectListeners());
        int origListeners = col1.getSQLObjectListeners().size();
        index.removeChild(0);
        System.out.println("Post-remove listeners: "+col1.getSQLObjectListeners());
        assertEquals(origListeners - 1, col1.getSQLObjectListeners().size());
    }
    
    public void testCopyConstructor() throws ArchitectException{
        SQLIndex copyIndex = new SQLIndex(index);
        
        assertEquals("Different Name",index.getName(),copyIndex.getName());
        assertEquals("Different uniqueness values", index.isUnique(),copyIndex.isUnique());
        assertEquals("Different index types", index.getType(),copyIndex.getType());
        assertEquals("Different qualifiers", index.getQualifier(),copyIndex.getQualifier());
        assertEquals("Different filters", index.getFilterCondition(),copyIndex.getFilterCondition());
        assertEquals("Different number of children", index.getChildCount(),copyIndex.getChildCount());
        
        for (int i=0; i< index.getChildCount();i++){
            assertEquals("Different columns for index column "+1, index.getChild(i).getColumn(),copyIndex.getChild(i).getColumn());
        }
    }
}
