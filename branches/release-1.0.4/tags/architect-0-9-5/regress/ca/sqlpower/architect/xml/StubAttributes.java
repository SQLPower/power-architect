package ca.sqlpower.architect.xml;

import org.xml.sax.Attributes;

/**
 * A stub implementation of the Attributes class for unit testing purposes.
 *
 */
public class StubAttributes implements Attributes {
    
    public int getIndex(String qName) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIndex(String uri, String localName) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getLocalName(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getQName(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getType(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getType(String qName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getType(String uri, String localName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getURI(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Always returns a null character, regardless of the argument value.
     */
    public String getValue(int index) {
        return "abc\\u0000123";
    }

    /**
     * Always returns a null character, regardless of the argument value.
     */
    public String getValue(String qName) {
        return "abc\\u0000123";
    }

    /**
     * Always returns a null character, regardless of the argument value.
     */
    public String getValue(String uri, String localName) {
        return "abc\\u0000123";
    }

}
