<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="param1" select="'default value'"/>
  <xsl:template match="doc">
    <html>
      <head><title>Stylesheet parameter</title></head>
      <body>
        <h2>XML source</h2>
          <p><xsl:value-of select="."/></p>
        <h2>Stylesheet parameter</h2>
          <p>The param1 stylesheet parameter has been set to <xsl:value-of select="$param1"/>.</p>
      </body>
     </html>          
  </xsl:template>
</xsl:stylesheet>
