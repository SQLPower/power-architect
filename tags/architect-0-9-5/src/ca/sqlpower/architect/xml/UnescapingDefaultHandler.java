package ca.sqlpower.architect.xml;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  An implementation of DefaultHandler that wraps around another DefaultHandler instance,
 *  or around ndividual DTDHandler, ContentHandler, ErrorHandler, and EntityResolver 
 *  instances. It is used by the UnescapingSaxParser for reading XML documents generated using
 *  the XMLHelper class.  
 *
 */
public class UnescapingDefaultHandler extends DefaultHandler {

    private static final Logger logger = Logger.getLogger(UnescapingDefaultHandler.class);
    
    /**
     * The EntityResolver that this instance of UnescapingDefaultHandler wraps around. 
     */
    private final EntityResolver entityResolver;
    
    /**
     * The DTDHandler that this instance of UnescapingDefaultHandler wraps around. 
     */
    private final DTDHandler dtdHandler;
    
    /**
     * The ContentHandler that this instance of UnescapingDefaultHandler wraps around. 
     */
    private final ContentHandler contentHandler;
    
    /**
     * The ErrorHandler that this instance of UnescapingDefaultHandler wraps around. 
     */
    private final ErrorHandler errorHandler;

    
    public UnescapingDefaultHandler(DefaultHandler defaultHandler) {
        this(defaultHandler, defaultHandler, defaultHandler, defaultHandler);
    }

    public UnescapingDefaultHandler(DTDHandler dtdHandler) {
        this(null, dtdHandler, null, null);
    }

    public UnescapingDefaultHandler(ContentHandler contentHandler) {
        this(null, null, contentHandler, null);
    }

    public UnescapingDefaultHandler(ErrorHandler errorHandler) {
        this(null, null, null, errorHandler);
    }

    public UnescapingDefaultHandler(EntityResolver entityResolver) {
        this(entityResolver, null, null, null);
    }

    /**
     * This is the constructor to which all others delegate.  It sets all four interface
     * implementations on this instance.
     * 
     * @param entityResolver
     * @param dtdHandler
     * @param contentHandler
     * @param errorHandler
     */
    public UnescapingDefaultHandler(
            EntityResolver entityResolver,
            DTDHandler dtdHandler,
            ContentHandler contentHandler,
            ErrorHandler errorHandler) {
        super();
        this.entityResolver = entityResolver;
        this.dtdHandler = dtdHandler;
        this.contentHandler = contentHandler;
        this.errorHandler = errorHandler;
    }
    
    
    // ================ ContentHandler delegates ===================

    /**
     * Takes the passed in char[] escapedCh and unescapes it using XMLHelper.
     * Then passes the unescaped string into the wrapped ContentHandler's implmentation
     * of the method.
     * 
     * @param ch A character array containing escaped characters from an XML document.
     * @param start 
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] escapedCh, int start, int length) throws SAXException {
        logger.debug("Unescaping character data");
        
        String original = new String(escapedCh, start, length);
        String unescaped = XMLHelper.unescape(original);
                
        contentHandler.characters(unescaped.toCharArray(), 0, unescaped.length());
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     * 
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method. 
     * 
     * @param uri
     * @param localName
     * @param qName
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        contentHandler.endElement(uri, localName, qName);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     * 
     * @param prefix
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        contentHandler.endPrefixMapping(prefix);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method. 
     * 
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     *  
     * @param target
     * @param data
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        contentHandler.processingInstruction(target, data);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     *  
     * @param locator
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     *  
     * @param name
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        contentHandler.skippedEntity(name);
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     *  
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        contentHandler.startDocument();
    }

    /**
     * Takes the Attributes atts and wraps within an UnescapingAttributes instance.
     * Passes the UnescapingAttributes and the other arguments into the wrapped 
     * ContentHandler's implementation of the method.
     * 
     * @param uri
     * @param localName
     * @param qName
     * @param atts
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {        
        logger.debug("Wrapping Attribute data");
        contentHandler.startElement(uri, localName, qName, new UnescapingAttributes(atts));
    }

    /**
     * Delegates to the wrapped ContentHandler's implementation of the method.
     * 
     * @param prefix
     * @param uri
     * @throws SAXException
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        contentHandler.startPrefixMapping(prefix, uri);
    }

    
    // ================ DTDHandler delegates ===================

    /**
     * Delegates to the wrapped DTDHandler's implementation of the method
     * 
     * @param arg0
     * @param arg1
     * @param arg2
     * @throws SAXException
     * @see org.xml.sax.DTDHandler#notationDecl(java.lang.String, java.lang.String, java.lang.String)
     */
    public void notationDecl(String arg0, String arg1, String arg2) throws SAXException {
        dtdHandler.notationDecl(arg0, arg1, arg2);
    }

    /**
     * Delegates to the wrapped DTDHandler's implementation of the method
     * 
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     * @throws SAXException
     * @see org.xml.sax.DTDHandler#unparsedEntityDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void unparsedEntityDecl(String arg0, String arg1, String arg2, String arg3) throws SAXException {
        dtdHandler.unparsedEntityDecl(arg0, arg1, arg2, arg3);
    }

    
    // ================ EntityResolver delegate ===================

    /**
     * Delegates to the wrapped EntityResolver's implementation of the method
     * 
     * @param publicId
     * @param systemId
     * @return
     * @throws SAXException
     * @throws IOException
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return entityResolver.resolveEntity(publicId, systemId);
    }

    
    // ================ ErrorHandler delegates ===================

    /**
     * Delegates to the wrapped ErrorHandler's implementation of the method
     * 
     * @param exception
     * @throws SAXException
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException exception) throws SAXException {
        errorHandler.error(exception);
    }

    /**
     * Delegates to the wrapped ErrorHandler's implementation of the method
     * 
     * @param exception
     * @throws SAXException
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        errorHandler.fatalError(exception);
    }

    /**
     * Delegates to the wrapped ErrorHandler's implementation of the method
     * 
     * @param exception
     * @throws SAXException
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException exception) throws SAXException {
        errorHandler.warning(exception);
    }

}
