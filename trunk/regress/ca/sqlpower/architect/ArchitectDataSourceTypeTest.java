package ca.sqlpower.architect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class ArchitectDataSourceTypeTest extends TestCase {

    private static final String PL_INI_CONTENTS = 
        "[Database Types_0]\n" +
        "Name=Type 0\n" +
        "JDBC Driver Class=ca.sqlpower.cow.Cow\n" +
        "JDBC URL=jdbc:cow://moo\n" +
        "Comment=No Comment\n" +
        "Property Name We Will Never Use=I hope this is ok\n" +
        "JDBC JAR Count=2\n" +
        "JDBC JAR_0=my.jar\n" +
        "JDBC JAR_1=your.jar\n" +
        "[Database Types_1]\n" +
        "Parent Type=Type 0\n" +
        "Name=Type 0.1\n" +
        "";
    
    /**
     * A sample instance of the data source type for testing.
     */
    private ArchitectDataSourceType superType;
    
    /**
     * This is a subtype of superType.
     */
    private ArchitectDataSourceType subType;
    
    protected void setUp() throws Exception {
        superType = new ArchitectDataSourceType();
        subType = new ArchitectDataSourceType();
        subType.setParentType(superType);
        ArchitectDataSourceType currentType = null;
        for (String line : PL_INI_CONTENTS.split("\\n")) {
            if (line.equals("[Database Types_0]")) {
                currentType = superType;
            } else if (line.equals("[Database Types_1]")) {
                currentType = subType;
            } else if (line.startsWith("[")) {
                throw new RuntimeException("File format problem");
            } else {
                int splitPoint = line.indexOf('=');
                String key = line.substring(0, splitPoint);
                String value = line.substring(splitPoint + 1);
                currentType.putProperty(key, value);
            }
        }
        
        assertTrue("Supertype didn't load properly", superType.getProperties().size() > 0);
        assertTrue("Subtype didn't load properly", subType.getProperties().size() > 0);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetName() {
        assertEquals("Type 0", superType.getName());
    }
    
    public void testInheritedProperty() {
        assertEquals("ca.sqlpower.cow.Cow", subType.getJdbcDriver());
    }
    
    public void testHasClassLoader() {
        assertNotNull(superType.getJdbcClassLoader());
    }
    
    public void testGetComment() {
        assertEquals("No Comment", superType.getComment());
    }
    
    public void testGetJdbcDriver() {
        assertEquals("ca.sqlpower.cow.Cow", superType.getJdbcDriver());
    }
    
    public void testGetJdbcJarList() {
        List<String> l = superType.getJdbcJarList();
        assertNotNull(l);
        assertEquals(2, l.size());
        assertEquals("incorrect jar list entry. properties="+superType.getProperties(), "my.jar", l.get(0));
        assertEquals("your.jar", l.get(1));
    }
    
    public void testGetJdbcUrl() throws Exception {
        assertEquals("jdbc:cow://moo", superType.getJdbcUrl());
    }
    
    public void testGetParentType() {
        assertSame(superType, subType.getParentType());
        assertNull(superType.getParentType());
    }
    
    public void testGetPropertiesImmutable() {
        try {
            superType.getProperties().put("this won't", "work");
            fail("I changed a property");
        } catch (UnsupportedOperationException ex) {
            // this is good
        }
    }
    
    public void testUnknownPropertiesStillExist() {
        assertEquals("I hope this is ok", superType.getProperties().get("Property Name We Will Never Use"));
    }
    
    public void testSetJdbcJarList() {
        List<String> list = new ArrayList<String>();
        list.add("zero");
        list.add("one");
        list.add("two");
        
        superType.setJdbcJarList(list);
        
        List<String> newList = superType.getJdbcJarList();
        
        assertEquals(list, newList);
    }
    
    public void testAddJdbcJar() {
        List<String> list = new ArrayList<String>(superType.getJdbcJarList());
        
        list.add("new thing");
        superType.addJdbcJar("new thing");
        
        assertEquals(list, superType.getJdbcJarList());
        assertEquals(String.valueOf(list.size()), superType.getProperty(ArchitectDataSourceType.JDBC_JAR_COUNT));
    }

    public void testJdbcJarListImmutable() {
        List<String> list = superType.getJdbcJarList();
        
        try {
            list.add("new thing");
            fail("I think I modified the list");
        } catch (UnsupportedOperationException ex) {
            // good result
        }
    }
    
    public void testRemoveJdbcJar() {
        int jarCount = superType.getJdbcJarList().size();
        superType.removeJdbcJar("your.jar");
        assertEquals(jarCount - 1, superType.getJdbcJarList().size());
        superType.removeJdbcJar("your.jar");
    }
    
    public void testRetrieveURLParsing() {
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl("<Database>:<Port>:<Hostname>");
        Map<String, String> map = dsType.retrieveURLParsing("data:1234:");
        assertEquals("data", map.get("Database"));
        assertEquals("1234", map.get("Port"));
        assertEquals("", map.get("Hostname") );
    }
    
    public void testRetrieveURLParsingWithDefaults() {
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl("<Database:db>:<Port:2222>:<Hostname:home>");
        Map<String, String> map = dsType.retrieveURLParsing("data:1234:");
        assertEquals("data", map.get("Database"));
        assertEquals("1234", map.get("Port"));
        assertEquals("", map.get("Hostname"));
    }
    
    public void testRetrieveURLParsingNullTemplateURL() {
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl(null);
        Map<String, String> map = dsType.retrieveURLParsing("data:1234:");
        assertEquals(0, map.size());
    }
    
    public void testRetrieveURLParsingURLDoesntmMatchTemplate() {
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl("hello:<Database:db>:<Port:2222>:<Hostname:home>");
        Map<String, String> map = dsType.retrieveURLParsing("hello");
        assertEquals(0, map.size());
    }
    
    public void testRetrieveURLDefaults(){
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl("<Database>:<Port:1234>:<Hostname:home>");
        Map<String, String> map = dsType.retrieveURLDefaults();
        assertEquals("", map.get("Database"));
        assertEquals("1234", map.get("Port"));
        assertEquals("home", map.get("Hostname"));
    }
    
    public void testRetrieveURLDefaultsNoTemplate(){
        ArchitectDataSourceType dsType = new ArchitectDataSourceType();
        dsType.setJdbcUrl(null);
        Map<String, String> map = dsType.retrieveURLDefaults();
        assertEquals(0, map.size());
    }
}
