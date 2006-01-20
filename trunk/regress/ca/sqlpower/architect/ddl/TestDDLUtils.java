package regress.ca.sqlpower.architect.ddl;

import junit.framework.TestCase;
import ca.sqlpower.architect.ddl.DDLUtils;

public class TestDDLUtils extends TestCase {


	
	public TestDDLUtils() {
		
	}
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
	}
	
	
	
	public void testToQualifiedName(){
		String sampleName= "Some Name";
		String sampleCatalog="Catalog";
		String sampleSchema = "Schema";
		
		assertEquals("Qualified name incorrect when name only is passed in",DDLUtils.toQualifiedName(null,null,sampleName),sampleName);
		assertEquals("Qualified name incorrect when schema and name are passed in",DDLUtils.toQualifiedName(null,sampleSchema,sampleName),sampleSchema+"."+sampleName);
		assertEquals("Qualified name incorrect when catalog and name are passed in",DDLUtils.toQualifiedName(sampleCatalog,null,sampleName),sampleCatalog+"."+sampleName);
		assertEquals("Qualified name incorrect when all three parameters are passed in",DDLUtils.toQualifiedName(sampleCatalog,sampleSchema,sampleName),sampleCatalog+"."+sampleSchema+"."+sampleName);
		
	}
	
}
