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

<!-- TODO: copy xml subtree of Doc elements verbatim (to preserve HTML in comments)
-->

<xsl:template match="/">
package ca.sqlpower.architect.util;


import ca.sqlpower.architect.olap.MondrianModel.*;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.GenericNewValueMaker;


/**
 * This is class is generated from xml-to-new-value-maker.xsl!  Do not alter it directly.
 */
public class MondrianNewValueMaker extends GenericNewValueMaker {

    public MondrianNewValueMaker(final SPObject root, DataSourceCollection&lt;SPDataSource&gt; dsCollection) {
        super(root, dsCollection);
    }
    
    public Object makeNewValue(Class&lt;?&gt; valueType, Object oldVal, String propName) {
        if (valueType == Schema.class) {
            Schema schema = new Schema();
            getRootObject().addChild(schema, 0);
            return schema;
        } else
<xsl:apply-templates/>
        {
            throw new IllegalStateException("Unknown class type " + valueType);
        }
    }
} // end of entire model
</xsl:template>

<!--  Classes are abstract, Elements are actual instances -->
<xsl:template match="Class">
    if (valueType == <xsl:value-of select="@class"/>.class) {
        <xsl:variable name="super-class" select="@class"/>
        return makeNewValue(<xsl:value-of select="/Model/Element[@class = $super-class]/@type"/>.class, oldVal, propName);
    } else
</xsl:template>

<!--  Schema is handled as a special case above as it is parented differently as the
      root object. -->
<!--  The Column and CaptionExpression are not connected to the tree in any way.
      If we need to persist these elements in the future they will need to be
      parented somewhere. -->
<xsl:template match="Element[@type != 'Schema' and @type != 'Column' and @type != 'CaptionExpression']">
    if (valueType == <xsl:value-of select="@type"/>.class) {
        <xsl:call-template name="find-parent">
            <xsl:with-param name="class-type" select="@type"/>
        </xsl:call-template>
        <xsl:value-of select="@type"/> agg = new <xsl:value-of select="@type"/>();
        parent.addChild(agg);
        return agg;
    } else
</xsl:template>

<xsl:template name="find-parent">
<xsl:param name="class-type"/>
<xsl:choose>
    <xsl:when test="count(/Model/Element[Array/@type = $class-type or Object/@type = $class-type]) > 0">
        <xsl:for-each select="/Model/Element[Array/@type = $class-type or Object/@type = $class-type]">
        <xsl:if test="position() = 1">
        <xsl:value-of select="@type"/> parent = (<xsl:value-of select="@type"/>) makeNewValue(<xsl:value-of select="@type"/>.class, null, "Parent of <xsl:value-of select="$class-type"/>");
        </xsl:if>
        </xsl:for-each>
    </xsl:when>
    <xsl:when test="count(/Model/Class[Array/@type = $class-type or Object/@type = $class-type]) > 0">
        <xsl:variable name="abstract-parent-class" select="/Model/Class[Array/@type = $class-type or Object/@type = $class-type]/@class"/>
        <xsl:for-each select="/Model/Element[@class = $abstract-parent-class]">
        <xsl:if test="position() = 1">
        <xsl:value-of select="@type"/> parent = (<xsl:value-of select="@type"/>) makeNewValue(<xsl:value-of select="@type"/>.class, null, "Parent of <xsl:value-of select="$class-type"/>");
        </xsl:if>
        </xsl:for-each>
    </xsl:when>
    <xsl:when test="$class-type != ''">
        <xsl:call-template name="find-parent">
            <xsl:with-param name="class-type" select="/Model/Element[@type = $class-type]/@class"/>
        </xsl:call-template>
    </xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="Doc">
  <!-- this is handled directly by parent element's template -->
</xsl:template>

<xsl:template match="Value">
  <!-- this is handled directly by parent element's template -->
</xsl:template>

<xsl:template match="Code">
<!-- the inline code makes assumptions about the code generator that aren't true for this generator.
     so we omit the inline code.
 -->
</xsl:template>

</xsl:transform>
