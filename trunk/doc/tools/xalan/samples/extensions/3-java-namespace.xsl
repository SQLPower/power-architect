<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java"
                version="1.0">
                
  <xsl:template match="/">
    <xsl:apply-templates select="/doc/date"/> 
  </xsl:template>
 
  <xsl:template match="date">
    <xsl:variable name="year" select="string(@year)"/>
    <xsl:variable name="month" select="string(@month)"/> 
    <xsl:variable name="day" select="string(@day)"/>          
    <xsl:variable name="format" select="string(@format)"/>
    <xsl:variable name="formatter"       
         select="java:java.text.SimpleDateFormat.new($format)"/>
    <xsl:variable name="date" 
         select="java:IntDate.getDate($year,$month,$day)"/>
    <p>Date: <xsl:value-of select="java:format($formatter, $date)"/></p>
  </xsl:template>
 
</xsl:stylesheet>
