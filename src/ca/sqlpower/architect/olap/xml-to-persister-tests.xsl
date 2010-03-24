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
package ca.sqlpower.architect.olap;


import ca.sqlpower.architect.olap.MondrianModel.*;
import ca.sqlpower.architect.util.ArchitectNewValueMaker;
import ca.sqlpower.object.PersistedSPObjectTest;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;


/**
 * This is class is generated from xml-to-persister-tests.xsl!  Do not alter it directly.
 */
public class MondrianModelTest {

<xsl:apply-templates/>

} // end of entire model
</xsl:template>

<!--  Classes are abstract, Elements are actual instances -->
<!--  The Column and CaptionExpression are not connected to the tree in any way.
      If we need to persist these elements in the future they will need to be
      parented somewhere. -->
<xsl:template match="Element[@type != 'Column' and @type != 'CaptionExpression']">

public static class <xsl:value-of select="@type"/>Test extends PersistedSPObjectTest {

        private <xsl:value-of select="@type"/> objectUnderTest;

        public <xsl:value-of select="@type"/>Test(String name) {
            super(name);
        }
        
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            objectUnderTest = (<xsl:value-of select="@type"/>) new ArchitectNewValueMaker(getRootObject(), getPLIni()).
                makeNewValue(<xsl:value-of select="@type"/>.class, null, "object under test");
        }

        @Override
        protected Class&lt;? extends SPObject&gt; getChildClassType() {
            return 
            <xsl:choose>
                <xsl:when test="Array">
                    <xsl:call-template name="find-instance-start">
                        <xsl:with-param name="child-type" select="Array/@type"/>
                    </xsl:call-template>
                </xsl:when>            
                <xsl:when test="Object">
                    <xsl:call-template name="find-instance-start">
                        <xsl:with-param name="child-type" select="Object/@type"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="@class">
                    <xsl:call-template name="find-superclass-with-child">
                        <xsl:with-param name="parent-type" select="@class"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                null;
                </xsl:otherwise>
            </xsl:choose>
        }

        @Override
        public SPObject getSPObjectUnderTest() {
            return objectUnderTest;
        }
        
        @Override
        public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection&lt;SPDataSource&gt; dsCollection) {
            return new ArchitectNewValueMaker(root, dsCollection);
        }
        
    }
</xsl:template>

<xsl:template name="find-superclass-with-child">
<xsl:param name="parent-type"/>
//superclass: <xsl:value-of select="$parent-type"/><xsl:text>
</xsl:text>
<xsl:choose>
    <xsl:when test="/Model/Class[@class = $parent-type]/Array">
        <xsl:call-template name="find-instance-start">
            <xsl:with-param name="child-type" select="/Model/Class[@class = $parent-type]/Array/@type"/>
        </xsl:call-template>
    </xsl:when>            
    <xsl:when test="/Model/Class[@class = $parent-type]/Object">
        <xsl:call-template name="find-instance-start">
            <xsl:with-param name="child-type" select="/Model/Class[@class = $parent-type]/Object/@type"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:when test="/Model/Class[@class = $parent-type]/@superclass">
        <xsl:call-template name="find-superclass-with-child">
            <xsl:with-param name="parent-type" select="/Model/Class[@class = $parent-type]/@superclass"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
        null;
    </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="find-instance-start">
<xsl:param name="child-type"/>
//child: <xsl:value-of select="$child-type"/><xsl:text>
</xsl:text>
<xsl:choose>
     <xsl:when test="/Model/Element[@type = $child-type]">
        <xsl:value-of select="/Model/Element[@type = $child-type]/@type"/>.class;
     </xsl:when>
     <xsl:when test="/Model/Class[@class = $child-type]">
        <xsl:call-template name="find-instance-loop">
            <xsl:with-param name="child-type" select="/Model/Class[@class = $child-type]/@class"/>
        </xsl:call-template>
    </xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template name="find-instance-loop">
<xsl:param name="child-type"/>
//child descendant: <xsl:value-of select="$child-type"/><xsl:text>
</xsl:text>
<xsl:choose>
    <xsl:when test="/Model/Element[@class = $child-type]">
        <xsl:value-of select="/Model/Element[@class = $child-type]/@type"/>.class;
    </xsl:when>
    <xsl:when test="/Model/Class[@superclass = $child-type]">
        <xsl:call-template name="find-instance-loop">
            <xsl:with-param name="child-type" select="/Model/Class[@superclass = $child-type]/Array/@type"/>
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
