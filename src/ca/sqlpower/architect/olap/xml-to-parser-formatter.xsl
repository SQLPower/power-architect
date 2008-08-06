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
import java.util.Stack;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class MondrianXMLReader {

    private static final Logger logger = Logger.getLogger(MondrianXMLReader.class);

    public static MondrianModel.Schema parse(File f) throws IOException, SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        MondrianSAXHandler handler = new MondrianSAXHandler();
        reader.setContentHandler(handler);
        InputSource is = new InputSource(new FileInputStream(f));
        reader.parse(is);
        return handler.root;
    }
    
    private enum ElementTypes {
        <xsl:for-each select="Element">
            <xsl:value-of select="@type"/>,
        </xsl:for-each>
        <xsl:for-each select="Class">
            <xsl:value-of select="@class"/>,
        </xsl:for-each>
        UNKNOWN;
    }

    private static class MondrianSAXHandler extends DefaultHandler {
    
       private Stack&lt;OLAPObject&gt; context = new Stack&lt;OLAPObject&gt;();
       private Locator locator;
       private MondrianModel.Schema root;
       
       @Override
       public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
           try {
	           OLAPObject currentElement;
	           if (false) {
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
	               throw new MondrianFileFormatException(locator, "Unknown element type \""+qName+"\"");
	           }
	           if (!context.isEmpty()) {
                   context.peek().addChild(currentElement);
               } else {
	               root = (MondrianModel.Schema) currentElement;
               }
	           context.push(currentElement);
	           logger.debug("Pushed " + currentElement);
	       } catch (Exception ex) {
	           throw new SAXException("Error at Line: "+locator.getLineNumber()+", Column: "+locator.getColumnNumber(), ex);
	       }
       }
       
       @Override
       public void endElement(String uri, String localName, String qName) {
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
