<?xml version="1.0"?>

<!--                                                                                -->
<!--  Default XSL stylesheet for use by com.lotus.xsl.server#DefaultApplyXSL.       -->
<!--                                                                                -->
<!--  This stylesheet mimics the default behavior of IE when XML data is displayed  -->
<!--  without a corresponding XSL stylesheet.  This stylesheet uses no JavaScript   -->
<!--  and displays all nodes as fully expanded.                                     -->
<!--                                                                                -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns="http://www.w3.org/TR/REC-html40">
                
<xsl:output method="html" indent="no"/>
<xsl:strip-space elements="*"/>

<xsl:template match="/">
  <HTML>
    <HEAD>
      <STYLE type="text/css">
        BODY {font:x-small 'Verdana'; margin-right:1.5em}
      <!-- container for expanding/collapsing content -->
        .c  {cursor:hand}
      <!-- button - contains +/-/nbsp -->
        .b  {color:red; font-family:'Courier New'; font-weight:bold; text-decoration:none}
      <!-- element container -->
        .e  {margin-left:1em; text-indent:-1em; margin-right:1em}
      <!-- comment or cdata -->
        .k  {margin-left:1em; text-indent:-1em; margin-right:1em}
      <!-- tag -->
        .t  {color:#990000}
      <!-- tag in xsl namespace -->
        .xt {color:#990099}
      <!-- attribute in xml or xmlns namespace -->
        .ns {color:red}
      <!-- markup characters -->
        .m  {color:blue}
      <!-- text node -->
        .tx {font-weight:bold}
      <!-- multi-line (block) cdata -->
        .db {text-indent:0px; margin-left:1em; margin-top:0px; margin-bottom:0px;
             padding-left:.3em; border-left:1px solid #CCCCCC; font:small Courier}
      <!-- single-line (inline) cdata -->
        .di {font:small Courier}
      <!-- DOCTYPE declaration -->
        .d  {color:blue}
      <!-- pi -->
        .pi {color:blue}
      <!-- multi-line (block) comment -->
        .cb {text-indent:0px; margin-left:1em; margin-top:0px; margin-bottom:0px;
             padding-left:.3em; font:small Courier; color:#888888}
      <!-- single-line (inline) comment -->
        .ci {font:small Courier; color:#888888}
        PRE {margin:0px; display:inline}
      </STYLE>
    </HEAD>

    <BODY class="st"><xsl:apply-templates/></BODY>

  </HTML>
</xsl:template>

<!-- Templates for each node type follows.  The output of each template has a similar structure
  to enable script to walk the result tree easily for handling user interaction. -->
  
<!-- Template for pis not handled elsewhere -->
<xsl:template match="processing-instruction()">
  <DIV class="e">
  <SPAN class="b">&#160;</SPAN>
  <SPAN class="m">&lt;?</SPAN><SPAN class="pi"><xsl:value-of select="name(.)"/> <xsl:value-of select="."/></SPAN><SPAN class="m">?&gt;</SPAN>
  </DIV>
</xsl:template>

<!-- Template for the XML declaration.  Need a separate template because the pseudo-attributes
    are actually exposed as attributes instead of just element content, as in other pis 
<xsl:template match="processing-instruction('xml')">
  <DIV class="e">
  <SPAN class="b">&#160;</SPAN>
  <SPAN class="m">&lt;?</SPAN><SPAN class="pi">xml <xsl:for-each select="@*"><xsl:value-of select="name(.)"/>="<xsl:value-of select="."/>" </xsl:for-each></SPAN><SPAN class="m">?&gt;</SPAN>
  </DIV>
</xsl:template>
-->

<!-- Template for attributes not handled elsewhere -->
<xsl:template match="@*"><SPAN class="t"><xsl:text> </xsl:text><xsl:value-of select="name(.)"/></SPAN><SPAN class="m">="</SPAN><B><xsl:value-of select="."/></B><SPAN class="m">"</SPAN></xsl:template>

<!-- Template for attributes in the xmlns or xml namespace
<xsl:template match="@xmlns:*|@xmlns|@xml:*"><SPAN class="ns"> <xsl:value-of select="name(.)"/></SPAN><SPAN class="m">="</SPAN><B class="ns"><xsl:value-of select="."/></B><SPAN class="m">"</SPAN></xsl:template>
-->

<!-- Template for text nodes -->
<xsl:template match="text()">
  <xsl:choose><xsl:when test="name(.) = '#cdata-section'"><xsl:call-template name="cdata"/></xsl:when>
  <xsl:otherwise><DIV class="e">
  <SPAN class="b">&#160;</SPAN>
  <SPAN class="tx"><xsl:value-of select="."/></SPAN>
  </DIV></xsl:otherwise></xsl:choose>
</xsl:template>
  
<!-- Template for comment nodes -->
<xsl:template match="comment()">
  <DIV class="k">
  <SPAN><SPAN class="b" STYLE="visibility:hidden">-</SPAN> <SPAN class="m">&lt;!--</SPAN></SPAN>
  <SPAN class="cb"><PRE><xsl:value-of select="."/></PRE></SPAN>
  <SPAN class="b">&#160;</SPAN> <SPAN class="m">--&gt;</SPAN>
  </DIV>
</xsl:template>

<!-- Template for cdata nodes -->
<xsl:template name="cdata">
  <DIV class="k">
  <SPAN><SPAN class="b" STYLE="visibility:hidden">-</SPAN> <SPAN class="m">&lt;![CDATA[</SPAN></SPAN>
  <SPAN class="db"><PRE><xsl:value-of select="."/></PRE></SPAN>
  <SPAN class="b">&#160;</SPAN> <SPAN class="m">]]&gt;</SPAN>
  </DIV>
</xsl:template>

<!-- Template for elements not handled elsewhere (leaf nodes) -->
<xsl:template match="*">
  <DIV class="e"><DIV STYLE="margin-left:1em;text-indent:-2em">
  <SPAN class="b">&#160;</SPAN>
  <SPAN class="m">&lt;</SPAN><SPAN class="t"><xsl:value-of select="name(.)"/></SPAN> <xsl:apply-templates select="@*"/><SPAN class="m"> /&gt;</SPAN>
  </DIV></DIV>
</xsl:template>
  
<!-- Template for elements with comment, pi and/or cdata children
<xsl:template match="*[comment() or processing-instruction() or cdata()]">
  <DIV class="e">
  <DIV class="c"><A href="#" onclick="return false" onfocus="h()" class="b">-</A> <SPAN class="m">&lt;</SPAN><SPAN><xsl:attribute name="class"><xsl:if test="xsl:*">x</xsl:if>t</xsl:attribute><xsl:value-of select="name(.)"/></SPAN><xsl:apply-templates select="@*"/> <SPAN class="m">&gt;</SPAN></DIV>
  <DIV><xsl:apply-templates/>
  <DIV><SPAN class="b">&#160;</SPAN> <SPAN class="m">&lt;/</SPAN><SPAN><xsl:attribute name="class"><xsl:if test="xsl:*">x</xsl:if>t</xsl:attribute><xsl:value-of select="name(.)"/></SPAN><SPAN class="m">&gt;</SPAN></DIV>
  </DIV></DIV>
</xsl:template> -->

<!-- Template for elements with only text children -->
<xsl:template match="*[text() and not(comment() or processing-instruction() or *)]">
  <DIV class="e"><DIV STYLE="margin-left:1em;text-indent:-2em">
  <SPAN class="b">&#160;</SPAN> <SPAN class="m">&lt;</SPAN><SPAN class="t"><xsl:value-of select="name(.)"/></SPAN><xsl:apply-templates select="@*"/>
  <SPAN class="m">&gt;</SPAN><SPAN class="tx"><xsl:value-of select="."/></SPAN><SPAN class="m">&lt;/</SPAN><SPAN class="t"><xsl:value-of select="name(.)"/></SPAN><SPAN class="m">&gt;</SPAN>
  </DIV></DIV>
</xsl:template>

<!-- Template for elements with element children -->
<xsl:template match="*[*]">
  <DIV class="e">
  <DIV class="c" STYLE="margin-left:1em;text-indent:-2em"><SPAN class="b">-</SPAN><SPAN class="m">&lt;</SPAN><SPAN class="t"><xsl:value-of select="name(.)"/></SPAN><xsl:apply-templates select="@*"/> <SPAN class="m">&gt;</SPAN></DIV>
  <DIV><xsl:apply-templates/>
  <DIV><SPAN class="b">&#160;</SPAN> <SPAN class="m">&lt;/</SPAN><SPAN class="t"><xsl:value-of select="name(.)"/></SPAN><SPAN class="m">&gt;</SPAN></DIV>
  </DIV></DIV>
</xsl:template>

</xsl:stylesheet>