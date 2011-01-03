<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:transform 
     version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output 
  encoding="iso-8859-15" 
  method="html" 
  indent="yes"
  standalone="yes"
  omit-xml-declaration="yes"
  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
/>

<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <html>
  <head>
    <style>
      .table {
        background-color:#F5F5FF;
        border-left:4px solid gray;
        border-bottom:4px solid gray;
        border-top:4px solid gray;
        border-right:4px solid gray;
        margin-bottom:3em;
        margin-left:2em;
        margin-right:2em;
        padding:1em;
      }
      .tableNameHeading h1 {
          display: inline;
        font-family: Segoe UI, Arial, sans-serif;
        font-size:125%;
        font-weight:bold;
      }
        
      .tableNameHeading h2 {
        display: inline;
        font-family: Segoe UI, Arial, sans-serif;
        font-size:115%;
        font-weight:normal;
      }
      
      .tableComment { 
        background-color:#e4efff; margin-bottom:20px;
      }
      
      .tableDefinition { 
        padding:2px;
        border-collapse:collapse;
        margin-top:1em;
      }

      .tdTableDefinition {
        padding-right:10px;
        vertical-align:top;
        border-top:1px solid #C0C0C0;
      }

      .tdLogicalColName {
        width:20em;
      }
      
      .tdPhysicalColName {
        width:20em;
      }
      
      .tdDataType {
        width:10em;
      }

      .tdPkFlag {
        width:4em;
      }

      .tdNullFlag {
        width:9em;
      }

      .tdTableHeading {
        padding:2px;
        font-family: Segoe UI, Arial, sans-serif;
        font-weight: bold;
        vertical-align:top;
        border-bottom: 1px solid #C0C0C0;
        background-color: rgb(240,240,240);
      }
      
      .subTitle {
        font-family: Segoe UI, Arial, sans-serif;
        font-weight: bold;
      }

      .references {
      }
      .comment {
        color:#666666;
        margin-left:3em;
        padding:0.25em;
      }
    </style>
    
  <title><xsl:call-template name="write-name"/></title>
  </head>
  <body>
    <xsl:call-template name="create-toc"/>
    <xsl:call-template name="table-definitions"/>
  </body>
  </html>
</xsl:template>

<xsl:template name="write-name">
  <xsl:variable name="title" select="//architect-project/project-name"/>
  <xsl:choose>
    <xsl:when test="string-length($title) &gt; 0">
      <xsl:value-of select="$title"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="'SQL Power Architect Datamodel'"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="create-toc">
    <center><h2><xsl:call-template name="write-name"/></h2></center>
    <h3><xsl:text>List of tables</xsl:text></h3>
    
    <ul>
      <xsl:for-each select="/architect-project/target-database/table">
        <xsl:sort select="@name"/>
        <li>
          <xsl:variable name="table" select="@name"/>
          <a href="#{$table}"><xsl:value-of select="$table"/></a>
        </li>
      </xsl:for-each>
    </ul>
    
</xsl:template>


<xsl:template name="table-definitions">

  <xsl:for-each select="/architect-project/target-database/table">

    <xsl:sort select="@name"/>
    <xsl:variable name="table" select="@name"/>
    <xsl:variable name="table-id" select="@id"/>
    <xsl:variable name="physicalName" select="@physicalName"/>
    <div class="tableNameHeading">
      <h1>
        <xsl:value-of select="$table"/>
        <a name="{$table}"></a>
      </h1>
      <h2>
      (Physical Name: <xsl:value-of select="$physicalName"/>)
      </h2>
      <xsl:if test="string-length(remarks) &gt; 0">
        <p class="comment">
        <xsl:apply-templates select="remarks" />
        </p>
      </xsl:if>
    </div>

    <div class="table">
      <table class="tableDefinition" width="100%">
        <tr>
          <td class="tdTableHeading tdLogicalColName"><xsl:text>Logical Column Name</xsl:text></td>
          <td class="tdTableHeading tdPhysicalColName"><xsl:text>Physical Column Name</xsl:text></td>
          <td class="tdTableHeading tdDataType"><xsl:text>Type</xsl:text></td>
          <td class="tdTableHeading tdPkFlag"><xsl:text>PK</xsl:text></td>
          <td class="tdTableHeading tdNullFlag"><xsl:text>Nullable</xsl:text></td>
        </tr>

        <xsl:for-each select="folder//column">
          <xsl:sort select="substring(@id, 4)" data-type="number"/>
          <xsl:variable name="col-id" select="@id"/>
          <tr valign="top">
            <td class="tdTableDefinition"><xsl:value-of select="@name"/>
              <xsl:if test="string-length(@primaryKeySeq) &gt; 0">
                <xsl:text> (PK)</xsl:text>
              </xsl:if>
              <xsl:for-each select="/architect-project/target-database/relationships/relationship[@fk-table-ref=$table-id]/column-mapping[@fk-column-ref=$col-id]">
                <xsl:variable name="pk-id" select="../@pk-table-ref"/>
                <xsl:variable name="targetTable" select="/architect-project/target-database/table[@id=$pk-id]/@name"/>
                &#160;(<a href="#{$targetTable}"><xsl:value-of select="'FK'"/></a>)
              </xsl:for-each>
            </td>
            <td class="tdTableDefinition"><xsl:value-of select="@physicalName"/></td>
            <td class="tdTableDefinition">
              <xsl:call-template name="write-data-type">
                <xsl:with-param name="type-id" select="@type"/>
                <xsl:with-param name="precision" select="@precision"/>
                <xsl:with-param name="scale" select="@scale"/>
              </xsl:call-template>
             </td>
            <td class="tdTableDefinition" nowrap="nowrap">
              <xsl:if test="string-length(@primaryKeySeq) &gt; 0">
                <xsl:text>PK</xsl:text>
              </xsl:if>
            </td>
            <td class="tdTableDefinition" nowrap="nowrap">
              <xsl:if test="@nullable='0'">
                <xsl:text>NOT NULL</xsl:text>
              </xsl:if>
            </td>
          </tr>
          <xsl:if test="string-length(remarks) &gt; 0">
          <tr>
            <td colspan="4">
              <div class="comment"><xsl:apply-templates select="remarks" /></div>
            </td>
          </tr>
          </xsl:if>
        </xsl:for-each>
      </table>

      <div class="references">
      <xsl:if test="count(/architect-project/target-database/relationships/relationship[@fk-table-ref=$table-id]) &gt; 0">
          <p class="subTitle"><xsl:text>References</xsl:text></p>
          <ul>
          <xsl:for-each select="/architect-project/target-database/relationships/relationship[@fk-table-ref=$table-id]">
            <xsl:variable name="pk-id" select="@pk-table-ref"/>
            <xsl:variable name="targetTable" select="/architect-project/target-database/table[@id=$pk-id]/@name"/>
            <li>
              <a href="#{$targetTable}"><xsl:value-of select="$targetTable"/></a><xsl:text> through (</xsl:text>
              <xsl:for-each select="column-mapping">
                <xsl:variable name="fk-col-id" select="@fk-column-ref"/>
                <xsl:variable name="fk-col-name" select="//column[@id=$fk-col-id]/@name"/>
                <xsl:value-of select="$fk-col-name"/>
                <xsl:if test="position() &lt; last()"><xsl:text>, </xsl:text></xsl:if>
              </xsl:for-each><xsl:text>)</xsl:text>
            </li>
          </xsl:for-each>
          </ul>
      </xsl:if>

      <xsl:if test="count(/architect-project/target-database/relationships/relationship[@pk-table-ref=$table-id]) &gt; 0">
          <p class="subTitle"><xsl:text>Referenced By</xsl:text></p>
          <ul>
          <xsl:for-each select="/architect-project/target-database/relationships/relationship[@pk-table-ref=$table-id]">
            <xsl:variable name="fk-id" select="@fk-table-ref"/>
            <xsl:variable name="targetTable" select="/architect-project/target-database/table[@id=$fk-id]/@name"/>
            <li><a href="#{$targetTable}"><xsl:value-of select="$targetTable"/></a><xsl:text> referencing (</xsl:text>
            <xsl:for-each select="column-mapping">
              <xsl:variable name="pk-col-id" select="@pk-column-ref"/>
              <xsl:variable name="pk-col-name" select="//column[@id=$pk-col-id]/@name"/>
              <xsl:value-of select="$pk-col-name"/>
              <xsl:if test="position() &lt; last()"><xsl:text>, </xsl:text></xsl:if>
            </xsl:for-each><xsl:text>)</xsl:text>
            </li>
          </xsl:for-each>
          </ul>
      </xsl:if>
      </div>
    </div>
  </xsl:for-each>

</xsl:template>

<xsl:template name="write-data-type">
  <xsl:param name="type-id"/>
  <xsl:param name="precision"/>
  <xsl:param name="scale"/>
  <xsl:choose>
    <xsl:when test="$type-id = 2005">
      <xsl:text>CLOB</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 2011">
      <xsl:text>NCLOB</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 2004">
      <xsl:text>BLOB</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -3">
      <xsl:text>VARBINARY</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -4">
      <xsl:text>LONGVARBINARY</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -1">
      <xsl:text>LONGVARCHAR</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 93">
      <xsl:text>TIMESTAMP</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 92">
      <xsl:text>TIME</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 91">
      <xsl:text>DATE</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 1">
      <xsl:text>CHAR</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 4">
      <xsl:text>INTEGER</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -5">
      <xsl:text>BIGINT</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 5">
      <xsl:text>SMALLINT</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -6">
      <xsl:text>TINYINT</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 8">
      <xsl:text>DOUBLE</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 7">
      <xsl:text>REAL</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 6">
      <xsl:text>FLOAT</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 16">
      <xsl:text>BOOLEAN</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -7">
      <xsl:text>BIT</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 2">
      <xsl:text>NUMERIC(</xsl:text><xsl:value-of select="$precision"/><xsl:text>,</xsl:text><xsl:value-of select="$scale"/><xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 3">
      <xsl:text>DECIMAL(</xsl:text><xsl:value-of select="$precision"/><xsl:text>,</xsl:text><xsl:value-of select="$scale"/><xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -15">
      <xsl:text>NCHAR(</xsl:text><xsl:value-of select="$precision"/><xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = 12">
      <xsl:text>VARCHAR(</xsl:text><xsl:value-of select="$precision"/><xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:when test="$type-id = -9">
      <xsl:text>NVARCHAR(</xsl:text><xsl:value-of select="$precision"/><xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:otherwise>
        <xsl:text>[</xsl:text><xsl:value-of select="$type-id"/><xsl:text>]</xsl:text>
    </xsl:otherwise>  
  </xsl:choose>
</xsl:template>

<xsl:template name="makelinebreak" match="remarks">
   <xsl:param name="text" select="."/>
   <xsl:choose>
   <xsl:when test="contains($text, '&#xa;')">
      <xsl:value-of select="substring-before($text, '&#xa;')"/>
      <br/>
      <xsl:call-template name="makelinebreak">
          <xsl:with-param name="text" select="substring-after($text,'&#xa;')"/>
      </xsl:call-template>
   </xsl:when>
   <xsl:when test="contains($text, '  ')">
      <xsl:value-of select="substring-before($text, '  ')"/>
      <br/>
      <xsl:call-template name="makelinebreak">
          <xsl:with-param name="text" select="substring-after($text,'  ')"/>
      </xsl:call-template>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$text"/>
   </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:transform>