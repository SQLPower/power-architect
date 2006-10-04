<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
                
   <xsl:template match="*">
      <xsl:copy>
         <xsl:for-each select="@*">
            <xsl:copy/>
         </xsl:for-each>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="imagedata">
      <imagedata fileref="{@fileref}" format="{@format}"/>
   </xsl:template>
   
</xsl:stylesheet>
