<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
<!ENTITY nbsp "<xsl:text disable-output-escaping='yes'><![CDATA[&nbsp;]]></xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="table" select="'UNDEFINED'" />


  <!-- ### INCLUDE COMMON STYLESHEET ### -->
  <xsl:include href="doc-common-html.xsl" />


  <!-- ### DOCUMENT ELEMENTS ### -->
  <xsl:template match="database">
    <xsl:apply-templates select="table[@name = $table]" />
  </xsl:template>

  <xsl:template match="table">
    &newline;&newline;&indent;&indent;
    <h1>Table <xsl:value-of select="@name" /></h1>
    &newline;&newline;&indent;&indent;
    <p><xsl:value-of select="description" /></p>
    &newline;&newline;&indent;&indent;
    <table>
      &newline;&indent;&indent;&indent;
      <tr>
        <th colspan="2">Column Summary</th>
      </tr>
      <xsl:for-each select="column">
        &newline;&indent;&indent;&indent;
        <tr>
          &newline;&indent;&indent;&indent;&indent;
          <td>
            <code>
              <a>
                <xsl:attribute name="href">
                  <xsl:text>#</xsl:text>
                  <xsl:value-of select="@name" />
                </xsl:attribute>
                <xsl:value-of select="@name" />
              </a>
            </code>
          </td>
          &newline;&indent;&indent;&indent;&indent;
          <td>
            <code><xsl:apply-templates select="type" /></code><br />
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <xsl:value-of select="substring-before(description,'.')" />
            <xsl:text>.</xsl:text>
          </td>
        </tr>
      </xsl:for-each>
      &newline;&indent;&indent;
    </table>
    &newline;&newline;&indent;&indent;
    <h2>Column Detail</h2>
    &newline;
    <xsl:apply-templates select="column" />
  </xsl:template>

  <xsl:template match="column">
    &newline;&newline;&indent;&indent;
    <h4>
      <xsl:attribute name="id">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      <xsl:value-of select="@name" />
    </h4>
    &newline;&newline;&indent;&indent;
    <pre>
      <xsl:value-of select="@name" />
      <xsl:apply-templates select="type" />
    </pre>
    <xsl:if test="description != ''">
      &newline;&newline;&indent;&indent;&indent;
      <blockquote><xsl:value-of select="description" /></blockquote>
    </xsl:if>
    &newline;
    <xsl:if test="reference">
      &indent;&indent;
      <h3>References:</h3>
      &newline;
      <xsl:apply-templates select="reference" />
    </xsl:if>
    <xsl:if test="not(position()=last())">
      &newline;&indent;&indent;
      <hr />
      &newline;
    </xsl:if>
  </xsl:template>

  <xsl:template match="type">
    <xsl:text> </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:if test="@size != ''">
      <xsl:text>(</xsl:text>
      <xsl:value-of select="@size" />
      <xsl:text>)</xsl:text>
    </xsl:if>
    <xsl:if test="@required != 'false'">
      <xsl:text> NOT NULL</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="reference">
    &indent;&indent;
    <blockquote>
      <a>
        <xsl:attribute name="href">
          <xsl:text>table.</xsl:text>
          <xsl:value-of select="@table" />
          <xsl:text>.html#</xsl:text>
          <xsl:value-of select="@column" />
        </xsl:attribute>
        <xsl:value-of select="@table" />
        <xsl:text>.</xsl:text>
        <xsl:value-of select="@column" />
      </a>
    </blockquote>
    <xsl:if test="not(position()=last())">
      <xsl:text>,</xsl:text>
    </xsl:if>
    &newline;
  </xsl:template>

  <xsl:template match="primarykey">
    &newline;
    <xsl:call-template name="sql-comment">
      <xsl:with-param name="text" select="description" />
      <xsl:with-param name="indent" select="'    '" />
    </xsl:call-template>
    &indent;
    <xsl:text>PRIMARY KEY (</xsl:text>
    <xsl:for-each select="column">
      <xsl:value-of select="@name" />
      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
    <xsl:if test="not(position()=last())">
      <xsl:text>,</xsl:text>
    </xsl:if>
    &newline;
  </xsl:template>

  <xsl:template match="index">
    &newline;
    <xsl:call-template name="sql-comment">
      <xsl:with-param name="text" select="description" />
      <xsl:with-param name="indent" select="'    '" />
    </xsl:call-template>
    &indent;
    <xsl:if test="@unique = 'true'">
      <xsl:text>UNIQUE </xsl:text>
    </xsl:if>
    <xsl:text>INDEX </xsl:text>
    <xsl:if test="@name != ''">
      <xsl:value-of select="@name" />
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:text>(</xsl:text>
    <xsl:for-each select="column">
      <xsl:value-of select="@name" />
      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>
    <xsl:if test="not(position()=last())">
      <xsl:text>,</xsl:text>
    </xsl:if>
    &newline;
  </xsl:template>

  <xsl:template match="*|text()">
  </xsl:template>

</xsl:stylesheet>
