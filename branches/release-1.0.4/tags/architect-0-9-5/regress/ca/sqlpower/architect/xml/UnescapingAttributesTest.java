package ca.sqlpower.architect.xml;

import junit.framework.TestCase;

public class UnescapingAttributesTest extends TestCase {

    /**
     * The UnescapingAttributes implementation we're testing here. 
     */
    private UnescapingAttributes unescapingAttr;
    
    /**
     * A stub implementation of Attributes that UnescapingAttributes
     * will wrap.
     */
    private StubAttributes stubAttr;

    protected void setUp() throws Exception {
        super.setUp();
        stubAttr = new StubAttributes();
        unescapingAttr = new UnescapingAttributes(stubAttr);
    }

    public void testGetValueInt() {        
        assertEquals(XMLHelper.unescape(stubAttr.getValue(0)), unescapingAttr.getValue(0));
    }

    public void testGetValueStringString() {
        assertEquals(XMLHelper.unescape(stubAttr.getValue("","")), unescapingAttr.getValue("",""));
    }

    public void testGetValueString() {
        assertEquals(XMLHelper.unescape(stubAttr.getValue("")), unescapingAttr.getValue(""));
    }

}
