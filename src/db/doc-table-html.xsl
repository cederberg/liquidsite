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
    &newline;&newline;
    <xsl:call-template name="column-summary" />
    &indent;&indent;
    <p></p>
    &newline;&newline;
    <xsl:call-template name="index-summary" />
    &indent;&indent;
    <p></p>
    &newline;&newline;
    <xsl:call-template name="column-detail" />
    <xsl:call-template name="index-detail" />
  </xsl:template>

  <xsl:template name="column-summary">
    &indent;&indent;
    <table class="summary">
      &newline;&indent;&indent;&indent;
      <tr>
        <th colspan="2">Column Summary</th>
      </tr>
      &newline;
      <xsl:apply-templates select="column" mode="summary" />
      &indent;&indent;
    </table>
    &newline;&newline;
  </xsl:template>

  <xsl:template name="column-detail">
    &indent;&indent;
    <h2>Column Detail</h2>
    &newline;
    <xsl:apply-templates select="column" mode="detail" />
  </xsl:template>

  <xsl:template name="index-summary">
    &indent;&indent;
    <table class="summary">
      &newline;&indent;&indent;&indent;
      <tr>
        <th colspan="2">Index Summary</th>
      </tr>
      &newline;
      <xsl:apply-templates select="primarykey" mode="summary" />
      <xsl:apply-templates select="index" mode="summary" />
      &indent;&indent;
    </table>
    &newline;&newline;
  </xsl:template>

  <xsl:template name="index-detail">
    &indent;&indent;
    <h2>Index Detail</h2>
    &newline;
    <xsl:apply-templates select="primarykey" mode="detail" />
    <xsl:apply-templates select="index" mode="detail" />
  </xsl:template>

  <xsl:template match="column" mode="name">
    <xsl:value-of select="@name" />
    <xsl:if test="not(position()=last())">
      <xsl:text>, </xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="column" mode="summary">
    &indent;&indent;&indent;
    <tr>
      &newline;&indent;&indent;&indent;&indent;
      <td class="name">
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
    &newline;
  </xsl:template>

  <xsl:template match="column" mode="detail">
    &newline;&newline;&indent;&indent;
    <h3>
      <xsl:attribute name="id">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      <xsl:value-of select="@name" />
    </h3>
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
    &newline;&indent;&indent;
    <hr />
    &newline;
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

  <xsl:template match="primarykey" mode="summary">
    &indent;&indent;&indent;
    <tr>
      &newline;&indent;&indent;&indent;&indent;
      <td class="name">
        <code>
          <a href="#primarykey">PRIMARY KEY</a>
        </code>
      </td>
      &newline;&indent;&indent;&indent;&indent;
      <td>
        <code>
          <xsl:text> (</xsl:text>
          <xsl:apply-templates select="column" mode="name" />
           <xsl:text>)</xsl:text>
        </code><br />
        &newline;&indent;&indent;&indent;&indent;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <xsl:value-of select="substring-before(description,'.')" />
        <xsl:text>.</xsl:text>
      </td>
      &newline;&indent;&indent;&indent;
    </tr>
    &newline;
  </xsl:template>

  <xsl:template match="primarykey" mode="detail">
    &newline;&newline;&indent;&indent;
    <h3 id="primarykey">PRIMARY KEY</h3>
    &newline;&newline;&indent;&indent;
    <pre>
      <xsl:text>PRIMARY KEY (</xsl:text>
      <xsl:apply-templates select="column" mode="name" />
      <xsl:text>)</xsl:text>
    </pre>
    <xsl:if test="description != ''">
      &newline;&newline;&indent;&indent;&indent;
      <blockquote><xsl:value-of select="description" /></blockquote>
    </xsl:if>
    &newline;&newline;&indent;&indent;
    <hr />
    &newline;
  </xsl:template>

  <xsl:template match="index" mode="summary">
    &indent;&indent;&indent;
    <tr>
      &newline;&indent;&indent;&indent;&indent;
      <td class="name">
        <code>
          <a>
            <xsl:attribute name="href">
              <xsl:text>#index</xsl:text>
              <xsl:value-of select="position()" />
            </xsl:attribute>
            <xsl:if test="@unique = 'true'">
              <xsl:text>UNIQUE </xsl:text>
            </xsl:if>
            <xsl:text>INDEX </xsl:text>
            <xsl:if test="@name != ''">
              <xsl:value-of select="@name" />
              <xsl:text> </xsl:text>
            </xsl:if>
          </a>
        </code>
      </td>
      &newline;&indent;&indent;&indent;&indent;
      <td>
        <code>
          <xsl:text>(</xsl:text>
          <xsl:apply-templates select="column" mode="name" />
          <xsl:text>)</xsl:text>
        </code><br />
        &newline;&indent;&indent;&indent;&indent;
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
        <xsl:value-of select="substring-before(description,'.')" />
        <xsl:text>.</xsl:text>
      </td>
      &newline;&indent;&indent;&indent;
    </tr>
    &newline;
  </xsl:template>

  <xsl:template match="index" mode="detail">
    &newline;&newline;&indent;&indent;
    <h3>
      <xsl:attribute name="id">
        <xsl:text>index</xsl:text>
        <xsl:value-of select="position()" />
      </xsl:attribute>
      <xsl:text>INDEX </xsl:text>
      <xsl:value-of select="@name" />
    </h3>
    &newline;&newline;&indent;&indent;
    <pre>
      <xsl:if test="@unique = 'true'">
        <xsl:text>UNIQUE </xsl:text>
      </xsl:if>
      <xsl:text>INDEX </xsl:text>
      <xsl:if test="@name != ''">
        <xsl:value-of select="@name" />
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:text>(</xsl:text>
      <xsl:apply-templates select="column" mode="name" />
      <xsl:text>)</xsl:text>
    </pre>
    <xsl:if test="description != ''">
      &newline;&newline;&indent;&indent;&indent;
      <blockquote><xsl:value-of select="description" /></blockquote>
    </xsl:if>
    &newline;&newline;&indent;&indent;
    <hr />
    &newline;
  </xsl:template>

  <xsl:template match="*|text()">
  </xsl:template>

</xsl:stylesheet>
