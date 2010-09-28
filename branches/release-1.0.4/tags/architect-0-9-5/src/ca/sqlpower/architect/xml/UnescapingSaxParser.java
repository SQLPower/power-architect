package ca.sqlpower.architect.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**\
 *  An implementation of SAXParser that wraps around another SAXParster.
 *  It is used to parse XML documents that were generated using the XMLHelper class. 
 *
 */
public class UnescapingSaxParser extends SAXParser {

    private static Logger logger = Logger.getLogger(UnescapingSaxParser.class);
    
    /**
     * The SAXParser that this instance of UnescapingSaxParser wraps around.
     */
    private final SAXParser parser;
    
    /**
     * An instance of UnescapingXMLReader that wraps around an XMLReader instance
     */
    private final UnescapingXMLReader reader;
    
    /**
     * Constructor which gets the wrapped SAXParser from a SAXParserFactory.
     */
    public UnescapingSaxParser() throws ParserConfigurationException, SAXException {
        this(SAXParserFactory.newInstance().newSAXParser());
    }

    /**
     * Constructor which sets the wrapped SAXParser to the given parser.
     * 
     * @param parser SAXParser that will be wrapped
     */
    public UnescapingSaxParser(SAXParser parser) throws ParserConfigurationException, SAXException {
        if (parser == null) {
            throw new NullPointerException("Passed a null parser into UnescapingSaxParser constructor");
        }
        
        logger.debug("Calling UnescapingSaxParser constructer");
        this.parser = parser;
        reader = new UnescapingXMLReader(parser.getXMLReader());
    }
    
    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean equals(Object obj) {
        return parser.equals(obj);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Parser getParser() throws SAXException {
        return parser.getParser();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return parser.getProperty(name);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public Schema getSchema() {
        return parser.getSchema();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public XMLReader getXMLReader() throws SAXException {
        return reader;
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public int hashCode() {
        return parser.hashCode();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isNamespaceAware() {
        return parser.isNamespaceAware();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isValidating() {
        return parser.isValidating();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public boolean isXIncludeAware() {
        return parser.isXIncludeAware();
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the File 'f' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(File f, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(f, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputSource 'is' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(InputSource is, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputSource 'is', the UnescapingDefaultHandler, and the String 'systemid'
     * into the parse() method of the wrapped SAXParser.
     */
    public void parse(InputStream is, DefaultHandler dh, String systemId) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh), systemId);
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the InputStream 'is' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(is, new UnescapingDefaultHandler(dh));
    }

    /**
     * Takes the passed-in DefaultHandler and wraps it in an UnescapingDefaultHandler.
     * Passes the String 'uri' and UnescapingDefaultHandler into the parse() method of the 
     * wrapped SAXParser.
     */
    public void parse(String uri, DefaultHandler dh) throws SAXException, IOException {
        parser.parse(uri, new UnescapingDefaultHandler(dh));
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public void reset() {
        parser.reset();
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        parser.setProperty(name, value);
    }

    /**
     * Delegates to the wrapped SAXParser's implementation of the method.
     */
    public String toString() {
        return parser.toString();
    }
}
