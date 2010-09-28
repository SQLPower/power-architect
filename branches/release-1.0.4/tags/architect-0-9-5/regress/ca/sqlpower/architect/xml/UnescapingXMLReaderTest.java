package ca.sqlpower.architect.xml;

import junit.framework.TestCase;

import org.xml.sax.helpers.DefaultHandler;

public class UnescapingXMLReaderTest extends TestCase {

    private UnescapingXMLReader reader;
    
    protected void setUp() throws Exception {
        super.setUp();
        reader = new UnescapingXMLReader(new StubReader());
    }

    public void testWrapContentHandler() {
        DefaultHandler defaultHandler = new DefaultHandler();
        reader.setContentHandler(defaultHandler);
        
        // no matter what we put in, we should get an escaping content handler back
        assertEquals(UnescapingDefaultHandler.class, reader.getContentHandler().getClass());
    }

}
