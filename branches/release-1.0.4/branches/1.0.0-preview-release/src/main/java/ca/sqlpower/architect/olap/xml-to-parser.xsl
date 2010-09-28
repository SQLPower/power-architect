<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:transform 
     version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output 
  encoding="iso-8859-1" 
  method="text" 
  indent="no"
  standalone="yes"
  omit-xml-declaration="yes"
/>

<xsl:strip-space elements="*"/>

<xsl:template match="/Model">
package ca.sqlpower.architect.olap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * This is class is generated from xml-to-parser.xsl!  Do not alter it directly.
 */
public class MondrianXMLReader {

    private static final Logger logger = Logger.getLogger(MondrianXMLReader.class);

    /**
     * Imports an OLAP schema from a Mondrian schema xml file.
     * 
     * @param f
     *            The file to read from.
     * @return The Schema that will be populated with the objects from the file.
     * @throws IOException
     *             If the file could not be read.
     * @throws SAXException
     *             If the xml in the file is malformed.
     */
    public static OLAPObject importXML(File f) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler();
        reader.setContentHandler(handler);
        InputSource is = new InputSource(new FileInputStream(f));
        reader.parse(is);
        return handler.root;
    }

    /**
     * Reads in the OLAPObjects from an Architect file. This is essentially the
     * same as calling {@link #parse(InputStream, OLAPRootObject, Map, Map)}.
     * 
     * @param f
     *            The file to load from.
     * @param rootObj
     *            The OLAPRootObject that will be populated with all the
     *            OLAPObjects from the file, must not be null.
     * @param sessionDbMap
     *            A map that will be populated with entries of OLAPSession to
     *            the id of the SQLDatabase that they reference, must not be null.
     * @param olapIdMap
     *            A map that will be populated with the OLAPObjects from the
     *            file and their generated ids, must not be null.
     * @return The OLAPRootObject that will be populated with the objects from
     *         the file.
     * @throws IOException
     *             If the file could not be read.
     * @throws SAXException
     *             If the xml in the file is malformed.
     */
    public static OLAPObject parse(File f, OLAPRootObject rootObj,
            Map&lt;OLAPSession, String&gt; sessionDbMap, Map&lt;String, OLAPObject&gt; olapIdMap) throws IOException, SAXException {
        return parse(new FileInputStream(f), rootObj, sessionDbMap, olapIdMap);
    }

    /**
     * Reads in OLAPObjects from an InputStream in the Architect OLAP format.
     * 
     * @param in
     *            The InputStream to read from, must support mark.
     * @param rootObj
     *            The OLAPRootObject that will be populated with all the
     *            OLAPObjects from the file, must not be null.
     * @param sessionDbMap
     *            A map that will be populated with entries of OLAPSession to
     *            the id of the SQLDatabase that they reference, must not be null.
     * @param olapIdMap
     *            A map that will be populated with the OLAPObjects from the
     *            file and their generated ids, must not be null.
     * @return The OLAPRootObject that will be populated with the objects from
     *         the file.
     * @throws IOException
     *             If the input stream could not be read.
     * @throws SAXException
     *             If the xml in the input stream is malformed.
     */
    public static OLAPObject parse(InputStream in, OLAPRootObject rootObj, Map&lt;OLAPSession, String&gt; sessionDbMap,
            Map&lt;String, OLAPObject&gt; olapIdMap) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler(rootObj, sessionDbMap, olapIdMap);
        reader.setContentHandler(handler);
        InputSource is = new InputSource(in);
        reader.parse(is);
        return handler.root;
    }

    private static class MondrianSAXHandler extends DefaultHandler {
        private Stack&lt;OLAPObject&gt; context = new Stack&lt;OLAPObject&gt;();
        private Locator locator;
        private OLAPObject root;
        private StringBuilder text;
        
        private Map&lt;String, String&gt;  currentOSessionAtts;
        
        private boolean inOlap;
       
        private final boolean importMode;
        
        private Map&lt;OLAPSession, String&gt; sessionDbMap;
        private Map&lt;String, OLAPObject&gt; olapIdMap;
        
        public MondrianSAXHandler() {
            this.importMode = true;
        }
       
        public MondrianSAXHandler(OLAPRootObject rootObj, Map&lt;OLAPSession, String&gt; sessionDbMap, Map&lt;String,
        		OLAPObject&gt; olapIdMap) {
            this.importMode = false;
            this.root = rootObj;
            this.sessionDbMap = sessionDbMap;
            this.olapIdMap = olapIdMap;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            try {
	            boolean pushElem = true;
	            OLAPObject currentElement;
	            
	            if (qName.equals("olap") || qName.equals("Schema")) {
	            	inOlap = true;
	           	}
	           	if (!inOlap) return;
	            
	            if (qName.equals("olap")) {
	                currentElement = root;
	                inOlap = true;
	            } else if (qName.equals("olap-session")) {
	                currentOSessionAtts = new HashMap&lt;String, String&gt;();
	                for (int i = 0; i &lt; atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        currentOSessionAtts.put(aname, aval);
	                }
	                pushElem = false;
	                currentElement = null;                  
	           <xsl:for-each select="Element">
	            } else if (qName.equals("<xsl:value-of select="@type"/>")) {
                    MondrianModel.<xsl:value-of select="@type"/> elem = new MondrianModel.<xsl:value-of select="@type"/>();
                    currentElement = elem;
                    for (int i = 0; i &lt; atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
                        if (olapIdMap != null &amp;&amp; aname.equals("id")) {
                       		olapIdMap.put(aval, elem);
                        <xsl:for-each select="Attribute">
                        } else if (aname.equals("<xsl:value-of select="@name"/>")) {
                             <xsl:choose>
                              <xsl:when test="@type = 'Boolean'">
                                elem.set<xsl:call-template name="name-initcap"/>(Boolean.valueOf(aval));
                              </xsl:when>
                              <xsl:otherwise>
                                elem.set<xsl:call-template name="name-initcap"/>(aval);
                              </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        } else {
			               <xsl:choose>
			                 <xsl:when test="@class">
			                   handle<xsl:value-of select="@class"/>Attribute(elem, aname, aval);
			                 </xsl:when>
			                 <xsl:otherwise>
			                   logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
			                 </xsl:otherwise>
			               </xsl:choose>
                        }
                    }
            </xsl:for-each>
	            } else {
	                pushElem = false;
	                currentElement = null;
	                if (inOlap) {
	                	logger.warn("Unknown element type \"" + qName + "\" at locator: " + locator);
	                } else {
	                	logger.debug("Unknown element type \"" + qName + "\" at locator: " + locator);	
	                }
	            }
	            if (pushElem) {
                    if (!context.isEmpty()) {
                        if (currentElement instanceof MondrianModel.Schema) {
                            OLAPSession osession = new OLAPSession((MondrianModel.Schema) currentElement);
                            for (String aname : currentOSessionAtts.keySet()) {
                                String aval = currentOSessionAtts.get(aname);
                                if (sessionDbMap != null &amp;&amp; aname.equals("db-ref")) {
                                    sessionDbMap.put(osession, aval);
                                } else if (olapIdMap != null &amp;&amp; aname.equals("id")) {
                       				olapIdMap.put(aval, osession);
                                } else {
                                    logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+OLAPSession.class+"\"");
                                }
                            }
                            context.peek().addChild(osession);
                            context.push(osession);
                        } else {
                            context.peek().addChild(currentElement);
                        }
                    } else {
                        if (importMode) {
                            root = (MondrianModel.Schema) currentElement;
                        }
                    }
                    context.push(currentElement);
                    logger.debug("Pushed " + currentElement);
	            }
	        } catch (Exception ex) {
	            throw new SAXException("Error at Line: "+locator.getLineNumber()+", Column: "+locator.getColumnNumber(), ex);
	        }
        }

        @Override
        public void characters (char ch[], int start, int length)
        throws SAXException
        {
            if (context.isEmpty()) return;
            if (text == null) {
                text = new StringBuilder();
            }
            OLAPObject elem = context.peek();
            if (elem instanceof MondrianModel.Value || elem instanceof MondrianModel.SQL || elem instanceof MondrianModel.Formula) {
                for (int i = start; i &lt; length+start; i++) {
                    text.append(ch[i]);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (context.isEmpty()) return;
        	if (context.peek() instanceof MondrianModel.Value) {
                ((MondrianModel.Value) context.peek()).setText(text.toString().trim());
            } else if (context.peek() instanceof MondrianModel.SQL) {
                ((MondrianModel.SQL) context.peek()).setText(text.toString().trim());
            } else if (context.peek() instanceof MondrianModel.Formula) {
                ((MondrianModel.Formula) context.peek()).setText(text.toString().trim());
            } else if ((context.peek() instanceof OLAPRootObject &amp;&amp; !importMode)||
            		(context.peek() instanceof MondrianModel.Schema &amp;&amp; importMode)) {
            	inOlap = false;
            }
            text = null;
            OLAPObject popped = context.pop();
            logger.debug("Popped " + popped);
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }
       <xsl:apply-templates select="Class"/>
    }
}
</xsl:template>

<xsl:template match="Class">
	    private void handle<xsl:value-of select="@class"/>Attribute(MondrianModel.<xsl:value-of select="@class"/> elem, String aname, String aval) {
	        if (false) {
	        <xsl:for-each select="Attribute">
	        } else if (aname.equals("<xsl:value-of select="@name"/>")) {
	            <xsl:choose>
	              <xsl:when test="@type = 'Boolean'">
	                elem.set<xsl:call-template name="name-initcap"/>(Boolean.valueOf(aval));
	              </xsl:when>
	              <xsl:otherwise>
	                elem.set<xsl:call-template name="name-initcap"/>(aval);
	              </xsl:otherwise>
	            </xsl:choose>
	        </xsl:for-each>
	        } else {
	          <xsl:choose>
	            <xsl:when test="@superclass">
	              handle<xsl:value-of select="@superclass"/>Attribute(elem, aname, aval);
	            </xsl:when>
	            <xsl:otherwise>
	              logger.warn("Skipping unknown attribute \""+aname+"\" of element \""+elem.getClass()+"\"");
	            </xsl:otherwise>
	          </xsl:choose>
	        }
	    }
</xsl:template>

<!-- Returns the initcap version of the "name" attribute of the current element -->
<xsl:template name="name-initcap">
  <xsl:value-of select="concat(translate(substring(@name,1,1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring(@name, 2))"/>
</xsl:template>

<!-- Returns the initcap version of the depluralized version of the "name" attribute of the current element -->
<xsl:template name="name-initcap-nonplural">
  <xsl:variable name="initcap" select="concat(translate(substring(@name,1,1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring(@name, 2))"/>
  <xsl:choose>
    <xsl:when test="$initcap = 'Hierarchies'">Hierarchy</xsl:when>
    <xsl:when test="$initcap = 'Properties'">Property</xsl:when>
    <xsl:otherwise><xsl:value-of select="substring($initcap, 1, string-length($initcap)-1)"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="Doc">
  <!-- not applicable -->
</xsl:template>

<xsl:template match="Code">
  <!-- not applicable -->
</xsl:template>

</xsl:transform>
