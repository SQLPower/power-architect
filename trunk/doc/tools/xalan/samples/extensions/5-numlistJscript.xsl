<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:counter="MyCounter"
                extension-element-prefixes="counter"
                version="1.0">

  <xalan:component prefix="counter"
                   elements="init incr" functions="read">
    <xalan:script lang="javascript">
      var counters = new Array();

      function init (xslproc, elem) {
        name = elem.getAttribute ("name");
        value = parseInt(elem.getAttribute ("value"));
        counters[name] = value;
        return null;
      }

      function read (name) {
        return "" + (counters[name]);
      }

      function incr (xslproc, elem)
      {
        name = elem.getAttribute ("name");
        counters[name]++;
        return null;
      }
    </xalan:script>
  </xalan:component>

  <xsl:template match="/">
    <HTML>
      <H1>JavaScript Example.</H1>
      <counter:init name="index" value="1"/>
      <p>Here are the names in alphabetical order by last name:</p>
      <xsl:for-each select="doc/name">
        <xsl:sort select="@last"/>
        <xsl:sort select="@first"/>
        <p>
        <xsl:text>[</xsl:text>
        <xsl:value-of select="counter:read('index')"/>
        <xsl:text>]. </xsl:text>
        <xsl:value-of select="@last"/>
        <xsl:text>, </xsl:text>
        <xsl:value-of select="@first"/>
        </p>
        <counter:incr name="index"/>
      </xsl:for-each>
    </HTML>
  </xsl:template>
 
</xsl:stylesheet>
