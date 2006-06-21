<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"

                version="1.0"

                xmlns:sql="org.apache.xalan.lib.sql.XConnection"

                extension-element-prefixes="sql">



<xsl:output method="html" indent="yes"/>


<!-- parameter setting to connect to Database -->

<xsl:param name="driver" select="'org.apache.derby.jdbc.EmbeddedDriver'"/>

<xsl:param name="datasource" select="'jdbc:derby:sampleDB'"/>

<xsl:param name="query" select="'SELECT * FROM import1'"/>

<xsl:param name="username" select="''"/>

<xsl:param name="passwd" select="''"/>

<xsl:template match="/">

    

    <xsl:variable name="db" select="sql:new()"/>

    

    <!-- Connect to the database with minimal error detection -->

		<xsl:if test="not(sql:connect($db, $driver, $datasource, $username, $passwd))" >

    	<xsl:message>Error Connecting to the Database</xsl:message>

      <xsl:copy-of select="sql:getError($db)/ext-error" />

    </xsl:if>

    

    <HTML>

      <HEAD>

        <TITLE>List of products</TITLE>

      </HEAD>

      <BODY>

        <TABLE border="1">

          <xsl:variable name="table" select='sql:query($db, $query)'/>

          

          <!-- 

          	Let's include Error Checking, the error is actually stored 

            in the connection since $table will be either data or null

          -->

             

          <xsl:if test="not($table)" >

          	<xsl:message>Error in Query</xsl:message>

            <xsl:copy-of select="sql:getError($db)/ext-error" />

          </xsl:if>

          

          <TR>

             <xsl:for-each select="$table/sql/metadata/column-header">

               <TH><xsl:value-of select="@column-label"/></TH>

             </xsl:for-each>

          </TR>

          <xsl:apply-templates select="$table/sql/row-set"/>

        </TABLE>

      </BODY>

    </HTML>

    <xsl:value-of select="sql:close($db)"/>

</xsl:template>



<xsl:template match="row">

  <TR><xsl:apply-templates select="col"/></TR>

</xsl:template>



<xsl:template match="col">

  <TD><xsl:value-of select="text()"/></TD>

</xsl:template>



</xsl:stylesheet>