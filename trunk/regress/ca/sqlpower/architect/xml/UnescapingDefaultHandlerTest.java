package ca.sqlpower.architect.xml;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class UnescapingDefaultHandlerTest extends TestCase {

    /**
     * The DefaultHandler implementation that we're testing here
     */
    private UnescapingDefaultHandler defaultHandler;
    
    /**
     * A stub implementaion of DefaultHandler that we're wrapping with the UnescapingDefaultHandler
     */
    private StubDefaultHandler stubHandler;
    
    protected void setUp() throws Exception {
        super.setUp();
        stubHandler = new StubDefaultHandler();
        defaultHandler = new UnescapingDefaultHandler(stubHandler);
    }

    public void testCharacters() throws SAXException {
        String testString = "abc\\u0000123";
        
        defaultHandler.characters(testString.toCharArray(), 2, 10);
        
        // Note, these test for correct behaviour given the current
        // implementation.  If we modify or improve the implementation,
        // this test may fail even though the implementation is correct.
        assertEquals("c\u0000123", stubHandler.string);
        assertEquals(0, stubHandler.start);
        assertEquals(5, stubHandler.length);
    }

    public void testStartElementStringStringStringAttributes() throws SAXException {
        StubAttributes attr = new StubAttributes();
        defaultHandler.startElement("", "", "", attr);
        
        assertEquals(UnescapingAttributes.class, stubHandler.attr.getClass());
    }

}
