package ca.sqlpower.architect.xml;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Simple stub implementation of XMLReader for unit test support.
 */
public class StubReader implements XMLReader {

    /**
     * Every method that takes a ContentHandler arguement
     * will store a reference to it here.  Test cases can
     * then examine it and make sure (for instance) that it
     * was the same implementation of ContentHandler that
     * they expect.
     */
    ContentHandler contentHandler;
    
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public DTDHandler getDTDHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public EntityResolver getEntityResolver() {
        // TODO Auto-generated method stub
        return null;
    }

    public ErrorHandler getErrorHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        // TODO Auto-generated method stub

    }

    public void parse(String systemId) throws IOException, SAXException {
        // TODO Auto-generated method stub

    }

    public void setContentHandler(ContentHandler handler) {
       contentHandler = handler;
    }

    public void setDTDHandler(DTDHandler handler) {
        // TODO Auto-generated method stub

    }

    public void setEntityResolver(EntityResolver resolver) {
        // TODO Auto-generated method stub

    }

    public void setErrorHandler(ErrorHandler handler) {
        // TODO Auto-generated method stub

    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub

    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub

    }
}
