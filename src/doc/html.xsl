<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="date" select="'UNDEFINED'" />
  <xsl:param name="style" select="''" />
  <xsl:param name="name" select="'UNDEFINED'" />
  <xsl:param name="version" select="'UNDEFINED'" />
  <xsl:param name="url" select="'UNDEFINED'" />


  <!-- ### OUTPUT DECLARATION ### -->
  <xsl:output method="xml"
              version="1.0"
              encoding="ISO-8859-1"
              doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
              doctype-system="DTD/xhtml1-strict.dtd" />


  <!-- ### DOCUMENT HEADER ### -->
  <xsl:template match="/">
    &newline;
    <xsl:comment> This file was automatically generated. DO NOT EDIT! </xsl:comment>
    &newline;&newline;
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <xsl:apply-templates />
    </html>
    &newline;
  </xsl:template>

  <xsl:template match="head">
    <head>
      &newline;&indent;&indent;
      <meta http-equiv="Content-Type" content="text/xhtml; charset=ISO-8859-1" />
      &newline;&indent;&indent;
      <meta http-equiv="Content-Style-Type" content="text/css" />
      &newline;&indent;&indent;
      <meta http-equiv="Content-Language" content="en" />
      <xsl:if test="$style != ''">
        &newline;&indent;&indent;
        <link rel="stylesheet">
          <xsl:attribute name="href">
            <xsl:value-of select="$style" />
          </xsl:attribute>
          <xsl:attribute name="type">text/css</xsl:attribute>
        </link>
      </xsl:if>
      &newline;&indent;&indent;
      <title><xsl:value-of select="title" /></title>
      &newline;&indent;
    </head>
  </xsl:template>


  <!-- ### DOCUMENT BODY ### -->
  <xsl:template match="body">
    <body>
      &newline;&newline;&indent;&indent;
      <h1><xsl:value-of select="/doc/head/title" /></h1>
      &newline;
      <xsl:apply-templates />
      &newline;&indent;&indent;
      <hr/>
      &newline;&newline;&indent;&indent;
      <p class="footer">
        <xsl:value-of select="$name" />
        <xsl:text> </xsl:text>
        <xsl:value-of select="$version" />
        <xsl:text> (</xsl:text>
        <xsl:value-of select="$date" />
        <xsl:text>). See the</xsl:text>
        &newline;&indent;&indent;
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="$url" />
          </xsl:attribute>
          <xsl:value-of select="$name" />
          <xsl:text> web site</xsl:text>
        </a>
        &newline;&indent;&indent;
        <xsl:text>for more information.</xsl:text>
      </p>
      &newline;&newline;&indent;&indent;
      <p class="footer">
        <xsl:text disable-output-escaping="yes">Copyright &amp;copy; 2003 Per Cederberg. Permission
    is granted to copy this document verbatim in any medium, provided
    that this copyright notice is left intact.</xsl:text>
      </p>
      &newline;&newline;&indent;
    </body>
  </xsl:template>

  <xsl:template match="h1">
    <h2><xsl:apply-templates /></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates /></p>
  </xsl:template>

  <xsl:template match="pre">
    <pre><xsl:apply-templates /></pre>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates />
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <li><xsl:apply-templates /></li>
  </xsl:template>

  <xsl:template match="item/title">
    <strong><xsl:apply-templates /></strong>
    <br/>
  </xsl:template>

  <xsl:template match="item/text">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates /></code>
  </xsl:template>
  
  <xsl:template match="ref">
    <xsl:choose>
      <xsl:when test="@url != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="@url" />
          </xsl:attribute>
          <xsl:apply-templates />
        </a>
      </xsl:when>
      <xsl:when test="@file != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="substring-before(@file,'.')" />
            <xsl:text>.html</xsl:text>
          </xsl:attribute>
          <xsl:apply-templates />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <em><xsl:apply-templates /></em>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="param">
    <xsl:choose>
      <xsl:when test="@name = 'date'">
        <xsl:value-of select="$date" />
      </xsl:when>
      <xsl:when test="@name = 'name'">
        <xsl:value-of select="$name" />
      </xsl:when>
      <xsl:when test="@name = 'version'">
        <xsl:value-of select="$version" />
      </xsl:when>
      <xsl:when test="@name = 'url'">
        <xsl:value-of select="$url" />
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="figure">
    &newline;&indent;&indent;
    <div class="figure">
      <xsl:apply-templates select="content" />
      &newline;&indent;&indent;&indent;
      <p><strong>Figure <xsl:number/>.</strong>
        &newline;&indent;&indent;&indent;
        <xsl:apply-templates select="caption" />
      </p>
      &newline;&indent;&indent;
    </div>
  </xsl:template>

</xsl:stylesheet>
