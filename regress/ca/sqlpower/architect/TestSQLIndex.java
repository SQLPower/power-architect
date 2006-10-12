package ca.sqlpower.architect;

import ca.sqlpower.architect.SQLIndex.IndexType;

public class TestSQLIndex extends SQLTestCase {

    SQLIndex index;
    public TestSQLIndex(String name) throws Exception {
        super(name);
        
    }

    protected void setUp() throws Exception {
        super.setUp();
        index = new SQLIndex("Test Index",true,"",IndexType.HASHED,"");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected SQLObject getSQLObjectUnderTest() {
       
        return index;
    }

}
