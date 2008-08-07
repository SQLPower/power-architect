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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class MondrianXMLWriter {

    private static final Logger logger = Logger.getLogger(MondrianXMLWriter.class);

    public static void write(File f, MondrianModel.Schema schema) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(f));
        write(out, schema);
        out.flush();
        out.close();
    }

    public static void write(PrintWriter out, MondrianModel.Schema schema) {
        MondrianXMLWriter writer = new MondrianXMLWriter(out);
        writer.writeSchema(schema);
    }
    
    private final PrintWriter out;
    
    private int indent;
    
    public MondrianXMLWriter(PrintWriter out) {
        this.out = out;
    }

    private void writeStartTag(String elemName, Map&lt;String, Object&gt; atts) {
        out.print("&lt;" + elemName);
        for (Map.Entry&lt;String, Object&gt; att : atts.entrySet()) {
           if (att.getValue() != null) {
               out.print(" "+att.getKey()+"=\""+att.getValue()+"\""); <!-- TODO: escape attribute value! -->
           }
        }
        out.println("&gt;");
    }
<xsl:apply-templates/>
}
</xsl:template>

<xsl:template match="Element">

    public void write<xsl:value-of select="@type"/>(MondrianModel.<xsl:value-of select="@type"/> elem) {
        Map&lt;String, Object&gt; atts = new LinkedHashMap&lt;String, Object&gt;();
        <xsl:for-each select="Attribute">
        atts.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>
        <xsl:if test="@class">
        populate<xsl:value-of select="@class"/>Attributes(elem, atts);
        </xsl:if>
        writeStartTag("<xsl:value-of select="@type"/>", atts);
        
        indent++;
        // content here
        indent--;
        
        out.println("&lt;/<xsl:value-of select="@type"/>&gt;");
    }
       
</xsl:template>

<xsl:template match="Class">
    private void populate<xsl:value-of select="@class"/>Attributes(MondrianModel.<xsl:value-of select="@class"/> elem, Map&lt;String, Object&gt; atts) {
        <xsl:for-each select="Attribute">
        atts.put("<xsl:value-of select="@name"/>", elem.get<xsl:call-template name="name-initcap"/>());
        </xsl:for-each>
        <xsl:if test="@superclass">
        populate<xsl:value-of select="@superclass"/>Attributes(elem, atts);
        </xsl:if>
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
