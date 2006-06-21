<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output indent="yes"/>
  <xsl:param name="param1" select="'default value'"/>
  <xsl:template match="doc">
    <out><xsl:value-of select="$param1"/></out>
  </xsl:template>
</xsl:stylesheet>
