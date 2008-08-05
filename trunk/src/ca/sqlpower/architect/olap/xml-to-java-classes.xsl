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

<xsl:template match="/">
package ca.sqlpower.architect.olap;

public class MondrianModel {
<xsl:apply-templates/>
} // end of entire model
</xsl:template>

<xsl:template match="Element">
/** <xsl:value-of select="Doc"/> */ <!-- TODO: copy xml subtree verbatim (to preserve HTML in comments) -->
public static class <xsl:value-of select="@type"/> extends <xsl:call-template name="superclass-of-element"/> {
    
    /**
     * Creates a new <xsl:value-of select="@type"/> with all attributes
     * set to their defaults.
     */
    public <xsl:value-of select="@type"/>() {
    }
    
<xsl:apply-templates/>
} // end of element <xsl:value-of select="@type"/>
</xsl:template>

<xsl:template match="Class">
/** <xsl:value-of select="Doc"/> */ <!-- TODO: copy xml subtree verbatim (to preserve HTML in comments) -->
public static class <xsl:value-of select="@class"/> extends <xsl:call-template name="superclass-of-class"/> {
    
    /**
     * Creates a new <xsl:value-of select="@class"/> with all attributes
     * set to their defaults.
     */
    public <xsl:value-of select="@class"/>() {
    }
    
<xsl:apply-templates/>
} // end of class <xsl:value-of select="@class"/>
</xsl:template>

<!-- Private instance variable with getter/setter pair. (i.e. a bound JavaBean property) -->
<xsl:template match="Attribute">
    private <xsl:call-template name="attribute-type"/> /* */ <xsl:value-of select="@name"/>;
    
    public <xsl:call-template name="attribute-type"/> /* */ get<xsl:call-template name="name-initcap"/>() {
        return <xsl:value-of select="@name"/>;
    }
    
    public void set<xsl:call-template name="name-initcap"/>(<xsl:call-template name="attribute-type"/> /* */ newval) {
        <xsl:call-template name="attribute-type"/> /* */ oldval = <xsl:value-of select="@name"/>;
        <xsl:value-of select="@name"/> = newval;
        pcs.firePropertyChange("<xsl:value-of select="@name"/>", oldval, newval);
    }
</xsl:template>

<xsl:template name="attribute-type">
  <xsl:choose>
    <xsl:when test="@type"><xsl:value-of select="@type"/></xsl:when>
    <xsl:otherwise>String</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Returns the correct superclass for the current Element element -->
<xsl:template name="superclass-of-element">
  <xsl:choose>
    <xsl:when test="@class"><xsl:value-of select="@class"/></xsl:when>
    <xsl:otherwise>OLAPObject</xsl:otherwise>
  </xsl:choose>
</xsl:template> 

<!-- Returns the correct superclass for the current Class element -->
<xsl:template name="superclass-of-class">
  <xsl:choose>
    <xsl:when test="@superclass"><xsl:value-of select="@superclass"/></xsl:when>
    <xsl:otherwise>OLAPObject</xsl:otherwise>
  </xsl:choose>
</xsl:template> 

<!-- Returns the initcap version of the "name" attribute of the current element -->
<xsl:template name="name-initcap">
  <xsl:value-of select="concat(translate(substring(@name,1,1), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), substring(@name, 2))"/>
</xsl:template> 

<xsl:template match="Doc">
  <!-- this is handled directly by parent element's template -->
</xsl:template>

<xsl:template match="Code">
<!-- the inline code makes assumptions about the code generator that aren't true for this generator.
     so we omit the inline code.
 -->
</xsl:template>

</xsl:transform>
