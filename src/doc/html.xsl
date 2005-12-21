<?xml version="1.0" encoding="ISO-8859-1" ?>

<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
<!ENTITY nbsp "<xsl:text disable-output-escaping='yes'>&amp;nbsp;</xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="date" select="'UNDEFINED'" />
  <xsl:param name="style" select="''" />
  <xsl:param name="logo" select="''" />
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
      <table class="menu">
        &newline;&indent;&indent;&indent;
        <tr>
          &newline;&indent;&indent;&indent;&indent;
          <td class="logo" rowspan="2">
            &newline;&indent;&indent;&indent;&indent;&indent;
            <a>
              <xsl:attribute name="href">
                <xsl:value-of select="$url" />
              </xsl:attribute>
              <img>
                <xsl:attribute name="src">
                  <xsl:value-of select="$logo" />
                </xsl:attribute>
                <xsl:attribute name="alt">
                  <xsl:value-of select="$name" />
                </xsl:attribute>
              </img>
            </a>
            &newline;&indent;&indent;&indent;&indent;
          </td>
          &newline;&indent;&indent;&indent;&indent;
          <td class="title">
            &newline;&indent;&indent;&indent;&indent;&indent;
            <h1><xsl:value-of select="/doc/head/title" /></h1>
            &newline;&indent;&indent;&indent;&indent;
          </td>
          &newline;&indent;&indent;&indent;&indent;
          <td class="end">
            &newline;&indent;&indent;&indent;&indent;&indent;
            <xsl:text>Version</xsl:text>
            &nbsp;
            <xsl:value-of select="$version" />
            <br />
            &newline;&indent;&indent;&indent;&indent;&indent;
            <xsl:value-of select="$date" />
            <br />
            &newline;&indent;&indent;&indent;&indent;&indent;
            <br />
            &newline;&indent;&indent;&indent;&indent;&indent;
            &nbsp;
            &newline;&indent;&indent;&indent;&indent;
          </td>
          &newline;&indent;&indent;&indent;
        </tr>
        &newline;&indent;&indent;&indent;
        <tr>
          &newline;&indent;&indent;&indent;&indent;
          <td class="filler"></td>
          &newline;&indent;&indent;&indent;
        </tr>
        &newline;&indent;&indent;
      </table>
      &newline;&newline;
      <div class="content">
        <xsl:apply-templates />
      </div>
      &newline;&indent;&indent;
      <hr/>
      &newline;&newline;&indent;&indent;
      <p class="footer">
        <xsl:text disable-output-escaping="yes">Copyright &amp;copy; 2004-2006 by</xsl:text>
        &newline;&indent;&indent;
        <a href="http://www.percederberg.net/software/">Per Cederberg</a>
        <xsl:text>.</xsl:text>
        &newline;&indent;&indent;
        <xsl:text>All rights reserved. Distributed under the</xsl:text>
        &newline;&indent;&indent;
        <a href="http://www.gnu.org/licenses/gpl.html">GNU GPL</a>
        <xsl:text>.</xsl:text>
      </p>
      &newline;&newline;&indent;
    </body>
  </xsl:template>

  <xsl:template match="h1">
    <h1><xsl:apply-templates select="@*|*|text()" /></h1>
  </xsl:template>

  <xsl:template match="h2">
    <h2><xsl:apply-templates select="@*|*|text()" /></h2>
  </xsl:template>

  <xsl:template match="h3">
    <h3><xsl:apply-templates select="@*|*|text()" /></h3>
  </xsl:template>

  <xsl:template match="separator">
    <hr>
      <xsl:apply-templates select="@*" />
    </hr>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates select="@*|*|text()" /></p>
  </xsl:template>

  <xsl:template match="pre">
    <pre><xsl:apply-templates select="@*|*|text()" /></pre>
  </xsl:template>

  <xsl:template match="list">
    <ul>
      <xsl:apply-templates select="@*|*|text()" />
    </ul>
  </xsl:template>

  <xsl:template match="item">
    <li><xsl:apply-templates select="@*|*|text()" /></li>
  </xsl:template>

  <xsl:template match="item/title">
    <strong><xsl:apply-templates select="@*|*|text()" /></strong>
    <br/>
  </xsl:template>

  <xsl:template match="item/text">
    <span><xsl:apply-templates select="@*|*|text()" /></span>
  </xsl:template>

  <xsl:template match="em">
    <em><xsl:apply-templates select="@*|*|text()" /></em>
  </xsl:template>

  <xsl:template match="strong">
    <strong><xsl:apply-templates select="@*|*|text()" /></strong>
  </xsl:template>

  <xsl:template match="code">
    <code><xsl:apply-templates select="@*|*|text()" /></code>
  </xsl:template>

  <xsl:template match="ref">
    <xsl:choose>
      <xsl:when test="@id != '' and @file != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="substring-before(@file,'.')" />
            <xsl:text>.html</xsl:text>
            <xsl:text>#</xsl:text>
            <xsl:value-of select="@id" />
          </xsl:attribute>
          <xsl:apply-templates />
        </a>
      </xsl:when>
      <xsl:when test="@id != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:text>#</xsl:text>
            <xsl:value-of select="@id" />
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
      <xsl:when test="@url != ''">
        <a>
          <xsl:attribute name="href">
            <xsl:value-of select="@url" />
          </xsl:attribute>
          <xsl:apply-templates />
        </a>
      </xsl:when>
      <xsl:otherwise>
        <em><xsl:apply-templates select="@*|*|text()" /></em>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="object">
    <xsl:choose>
      <xsl:when test="@type = 'image'">
        <img>
          <xsl:attribute name="src">
            <xsl:value-of select="@url" />
          </xsl:attribute>
          <xsl:attribute name="alt">
            <xsl:value-of select="@description" />
          </xsl:attribute>
        </img>
      </xsl:when>
      <xsl:otherwise>
        <strong>Unsupported object type</strong>
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

  <xsl:template match="example">
    &newline;&indent;&indent;
    <div class="example">
      <xsl:apply-templates />
      &newline;&indent;&indent;
    </div>
  </xsl:template>

  <xsl:template match="preformat">
    &newline;&indent;&indent;
    <pre>
      <xsl:apply-templates />
    </pre>
  </xsl:template>

  <xsl:template match="@id">
    <xsl:attribute name="id">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@style">
    <xsl:attribute name="class">
      <xsl:value-of select="." />
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*">
  </xsl:template>

</xsl:stylesheet>
