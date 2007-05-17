package ca.sqlpower.architect.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class UnescapingSaxParserTest extends TestCase {

    /**
     * This is the escaping parser implementation we're testing.
     */
    private UnescapingSaxParser unescapingParser;
    
    /**
     * This is a stub that we create for the escaping parser to
     * wrap.  We can examine this stub in the test cases to make
     * sure the escaping parser wrapper is behaving properly.
     */
    private StubSAXParser stubParser;
    
    protected void setUp() throws Exception {
        super.setUp();
        stubParser = new StubSAXParser();
        unescapingParser = new UnescapingSaxParser(stubParser);
    }

    public void testGetXMLReader() throws SAXException {
        assertEquals(UnescapingXMLReader.class, unescapingParser.getXMLReader().getClass());
    }

    public void testParseFileDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((File) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputSourceDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputSource) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputStreamDefaultHandlerString() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputStream) null, handler, "");
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseInputStreamDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((InputStream) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

    public void testParseStringDefaultHandler() throws SAXException, IOException {
        DefaultHandler handler = new DefaultHandler();        
        unescapingParser.parse((String) null, handler);
        
        assertEquals(UnescapingDefaultHandler.class, stubParser.handler.getClass());
    }

}
