package ca.sqlpower.architect;

public class TestSQLExceptionNode extends SQLTestCase {

    public TestSQLExceptionNode(String name) throws Exception {
        super(name);
    }

    private SQLExceptionNode node;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        node = new SQLExceptionNode(new Exception(), "Test exception");
    }
    
    @Override
    protected SQLObject getSQLObjectUnderTest() {
        return node;
    }

}
