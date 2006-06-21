<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns="http://www.w3.org/TR/REC-html40">

  <!-- FileName: misc-evans -->
  <!-- Document: http://www.w3.org/TR/xslt -->
  <!-- DocVersion: 19991116 -->
  <!-- Purpose: COPY of perf test;"A Practical Suggestion for XSLT Performance Improvement" by Clark Evans. -->

   <xsl:variable name="day-list" 
     select="//dow[not(.=following::dow)]" />

   <xsl:variable name="product-list" 
            select="//product[not(.=following::product)]" />
     
   <xsl:output indent="yes"/>
<xsl:template match="/">
  <xsl:variable name="my-test"><a><b/></a></xsl:variable>
  <html>
    <xsl:copy-of select="$my-test"/>
    <body>
    <table>
      <tr>
        <td><xsl:text> </xsl:text></td>
        <xsl:for-each select="$day-list">
          <xsl:sort order="ascending" select="." />
          <th><xsl:value-of select="."/></th>
        </xsl:for-each>
      </tr>
      <xsl:for-each select="$product-list">
        <xsl:sort    order="ascending" select="." />
        <xsl:variable name="product" select="." />
        <tr>
          <td>
            <xsl:value-of select="$product" />
          </td>
          <xsl:for-each select="$day-list">
            <xsl:sort order="ascending" select="." />
            <xsl:variable name="day" select="." />
            <td>
              <xsl:value-of 
             select="sum(//price[../product=$product][../../../dow=$day])"
/> .
            </td>
          </xsl:for-each>
          <td>
            <xsl:value-of 
              select="sum(//price[../product=$product])" /> .
           </td>
        </tr>
      </xsl:for-each>
      <tr>
        <td><xsl:text> </xsl:text></td>
        <xsl:for-each select="$day-list">
          <xsl:sort    order="ascending" select="." />
          <xsl:variable name="day" select="." />
          <td>
            <xsl:value-of 
              select="sum(//price[../../../dow=$day])" />
          </td>
        </xsl:for-each>
        <td>
          <xsl:value-of select="sum(//price)" />
        </td>
      </tr>
    </table>
    </body>
  </html>
</xsl:template>
</xsl:stylesheet>
