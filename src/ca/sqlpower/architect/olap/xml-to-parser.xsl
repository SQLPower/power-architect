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

import ca.sqlpower.architect.SQLDatabase;

/**
 * This is class is generated from xml-to-parser.xsl!  Do not alter it directly.
 */
public class MondrianXMLReader {

    private static final Logger logger = Logger.getLogger(MondrianXMLReader.class);

    public static OLAPObject parse(File f, boolean mondrianMode) throws IOException, SAXException {
        return parse(null, null, new FileInputStream(f), mondrianMode);
    }

    public static OLAPObject parse(OLAPRootObject rootObj, Map dbIdMap, File f, boolean mondrianMode) throws IOException, SAXException {
        return parse(rootObj, dbIdMap, new FileInputStream(f), mondrianMode);
    }

    public static OLAPObject parse(OLAPRootObject rootObj, Map dbIdMap, InputStream in, boolean mondrianMode) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler(rootObj, dbIdMap, mondrianMode);
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
        
        private Attributes currentOSessionAtts;
        
        private boolean inOlap;
       
        private final Map dbIdMap;
        private final boolean mondrianMode;
       
        public MondrianSAXHandler(OLAPRootObject rootObj, Map dbIdMap, boolean mondrianMode) {
            if (rootObj != null) {
                this.root = rootObj;
            }
            this.dbIdMap = dbIdMap;
            this.mondrianMode = mondrianMode;
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
	                currentOSessionAtts = atts;
	                pushElem = false;
	                currentElement = null;               
	           <xsl:for-each select="Element">
	            } else if (qName.equals("<xsl:value-of select="@type"/>")) {
                    MondrianModel.<xsl:value-of select="@type"/> elem = new MondrianModel.<xsl:value-of select="@type"/>();
                    currentElement = elem;
                    for (int i = 0; i &lt; atts.getLength(); i++) {
                        String aname = atts.getQName(i);
                        String aval = atts.getValue(i);
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
                            for (int i = 0; i &lt; currentOSessionAtts.getLength(); i++) {
                                String aname = currentOSessionAtts.getQName(i);
                                String aval = currentOSessionAtts.getValue(i);
                                if (aname.equals("dbcs-ref")) {
                                    osession.setDatabase((SQLDatabase) dbIdMap.get(aval));
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
                        if (mondrianMode) {
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
            } else if ((context.peek() instanceof OLAPRootObject &amp;&amp; !mondrianMode)||
            		(context.peek() instanceof MondrianModel.Schema &amp;&amp; mondrianMode)) {
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
