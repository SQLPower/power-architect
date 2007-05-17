package ca.sqlpower.architect.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StubDefaultHandler extends DefaultHandler {
    
    /**
     * The Attributes that are passed into the stub implementation of the
     * startElement method. For example, the tests will check the class
     * of the attribute in UnescapingDefaultHandlerTest to ensure that a
     * UnescapingAttributes is being passed into the startElements method.
     * Access is package-private so that the tests can access it directly.
     */
    Attributes attr;
    
    /**
     * The arguments that get passed into the stub implementation of the
     * characters method. For example, we will check the values of these in
     * UnescapingDefaultHandlerTest to make sure the right values are being passed
     * in UnescapingDefaultHandler's implementation of the characters method.
     * Access is package-private so that the tests can access it directly.
     */
    String string;
    
    /**
     * The most recent start arg value passed to characters().
     * Access is package-private so that the tests can access it directly.
     */
    int start;
    
    /**
     * The most recent length arg value passed to characters().
     * Access is package-private so that the tests can access it directly.
     */
    int length;
  
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        this.string = new String(ch);
        this.start = start;
        this.length = length;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        attr = attributes;
    }
}
