package ca.sqlpower.architect.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;

import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class StubSAXParser extends SAXParser {

    /**
     * Every method that takes a DefaultHandler arguement
     * will store a reference to it here.  Test cases can
     * then examine it and make sure (for instance) that it
     * was the same implementation of DefaultHandler that
     * they expect.
     */
    DefaultHandler handler;
    
    @Override
    public Parser getParser() throws SAXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XMLReader getXMLReader() throws SAXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNamespaceAware() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValidating() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // TODO Auto-generated method stub

    }

    @Override
    public void parse(File f, DefaultHandler dh) throws SAXException, IOException {
        handler = dh;
    }
    
    @Override
    public void parse(InputSource is, DefaultHandler dh) throws SAXException, IOException {
        handler = dh;
    }
    
    @Override
    public void parse(InputStream is, DefaultHandler dh, String systemId) throws SAXException, IOException {
        handler = dh;
    }
    
    @Override
    public void parse(InputStream is, DefaultHandler dh) throws SAXException, IOException {
        handler = dh;
    }
    
    @Override
    public void parse(String uri, DefaultHandler dh) throws SAXException, IOException {
        handler = dh;
    }
}
