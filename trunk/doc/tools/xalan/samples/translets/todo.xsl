<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:variable name="title" select="concat(todo/@project, ' ', todo/@major-version)"/>
  <xsl:template match="/">
    <HTML>
      <HEAD>
         <TITLE><xsl:value-of select="$title"/></TITLE>
      </HEAD>
      <BODY>

        <H2><xsl:value-of select="concat($title, ': ', todo/@title)"/></H2>
        <p><font size="-1">See a 
          <xsl:element name="a">
            <xsl:attribute name="href">#developer-list</xsl:attribute>
            <xsl:text>list of developers/initials.</xsl:text>
          </xsl:element>
        </font></p>
        <font size="-1"><p>Planned releases: 
            <BR/><xsl:for-each select="todo/actions/target-release-description">
              <xsl:element name="a">
                <xsl:attribute name="href">#release-date-<xsl:value-of select="date"/></xsl:attribute>
                <xsl:value-of select="date"/>
              </xsl:element><xsl:text> </xsl:text><xsl:text> </xsl:text>
            </xsl:for-each>
            <xsl:element name="a">
                <xsl:attribute name="href">#release-date-completed</xsl:attribute>
                <xsl:text>Completed</xsl:text>
              </xsl:element>

        </p></font>
        <xsl:for-each select="todo">
          <xsl:for-each select="actions">
              <xsl:for-each select="target-release-description">
                <p>
                  <xsl:apply-templates/>
                </p>
              </xsl:for-each>
              <xsl:for-each select="action">
                <xsl:if test="normalize-space(.)">
                  <p>
                   <xsl:number/>) <xsl:apply-templates/>
                   <xsl:if test="@*">
                    <BR/>
                   </xsl:if>
                   <xsl:apply-templates select="@*"/>
                  </p>
                </xsl:if>
            </xsl:for-each>
            <HR/>
          </xsl:for-each>

          <xsl:for-each select="completed">
              <xsl:element name="a">
                <xsl:attribute name="name">release-date-completed</xsl:attribute>
                <H3>Completed: </H3>
              </xsl:element>
            <xsl:for-each select="action">
              <xsl:if test="normalize-space(.)">
                <p>
                 <xsl:number/>) <xsl:apply-templates/>
                 <xsl:if test="@*">
                  <BR/>
                 </xsl:if>
                 <xsl:apply-templates select="@*"/>
                </p>
              </xsl:if>
          </xsl:for-each>
          <HR/>
        </xsl:for-each>

        <xsl:call-template name="developer-list"/>
       </xsl:for-each>

      </BODY>
    </HTML>
  </xsl:template>

  <xsl:template match="action/@*">
  <!-- Add link to the who attributes to corresponding item in developer-list -->
    <b><xsl:value-of select="name(.)"/>:</b><xsl:text> </xsl:text>
      <xsl:choose>
        <xsl:when test="name(.)='who'">
          <xsl:element name="a">
            <xsl:attribute name="href">#personref-<xsl:value-of select="."/></xsl:attribute>
            <xsl:value-of select="."/>
          </xsl:element>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    <xsl:if test="not (position()=last())">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="target-release-description/date">
      <xsl:element name="a">
        <xsl:attribute name="name">release-date-<xsl:value-of select="."/></xsl:attribute>
        <b><xsl:text>For release: </xsl:text><xsl:value-of select="."/></b>
      </xsl:element>
    
  </xsl:template>

  <xsl:template match="issue">
    <BR/><b>Issue </b><xsl:text>[</xsl:text><xsl:value-of select="@id"/>
    <xsl:text>]: </xsl:text>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="target-release-description/level">
    <xsl:text>, </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="target-release-description/goal">
    <BR/><b>Goal </b><xsl:text>[</xsl:text><xsl:value-of select="@type"/>
    <xsl:text>]: </xsl:text>
    <xsl:apply-templates/>
  </xsl:template>


  <xsl:template name="developer-list">
    <H3>
      <xsl:element name="a">
        <xsl:attribute name="name">developer-list</xsl:attribute>
        <xsl:text>Developers:</xsl:text>
      </xsl:element>
    </H3>
    <p>A list of some of the people currently working on <xsl:value-of select="/todo/@project"/>:</p>
    <ul>
    <xsl:for-each select="devs/person[not(@status = 'emeritus')]">
      <li>
        <xsl:apply-templates select="."/>
      </li>
    </xsl:for-each>
    </ul>
    <xsl:if test="boolean(devs/person[@status = 'emeritus'])">
      <p>And the Hall-of-Fame list of past developers:</p>
      <ul>
      <xsl:for-each select="devs/person[@status = 'emeritus']">
        <li>
          <xsl:apply-templates select="."/>
        </li>
      </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template match="person">
    <a href="mailto:{@email}">
      <xsl:value-of select="@name"/>
    </a>
    <xsl:element name="a">
      <xsl:attribute name="name"><xsl:text>personref-</xsl:text><xsl:value-of select="@id"/></xsl:attribute>
      <xsl:text> (</xsl:text><xsl:value-of select="@id"/><xsl:text>)</xsl:text>
    </xsl:element>
     <BR/><xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet>
