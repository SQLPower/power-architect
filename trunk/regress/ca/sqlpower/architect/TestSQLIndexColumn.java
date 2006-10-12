package ca.sqlpower.architect;

import ca.sqlpower.architect.SQLIndex.IndexType;

public class TestSQLIndexColumn extends SQLTestCase {
    
    private SQLIndex.Column indexColumn; 

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SQLIndex index = new SQLIndex("Test Index",true,"",IndexType.HASHED,"");
        indexColumn =index.new Column("Index1",true,true);
        
    }
    public TestSQLIndexColumn(String name) throws Exception {
        super(name);
        
        
    }

    @Override
    protected SQLObject getSQLObjectUnderTest() {
        
        return indexColumn;
    }

}
