<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:sql="org.apache.xalan.lib.sql.XConnection"
                extension-element-prefixes="sql">

<xsl:output method="html" indent="yes"/>

<xsl:param name="driver" select="'org.apache.derby.jdbc.EmbeddedDriver'"/>
<xsl:param name="datasource" select="'jdbc:derby:sampleDB'"/>
<xsl:param name="query" select="'SELECT * FROM import1'"/>

<xsl:template match="/">
    <xsl:variable name="db" select="sql:new($driver, $datasource)"/>
    <xsl:variable name="table" select='sql:query($db, $query)'/>
    <xsl:copy-of select="$table" />
    <xsl:value-of select="sql:close($db)"/>
</xsl:template>

</xsl:stylesheet>