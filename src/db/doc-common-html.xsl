<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE xsl:stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="name" select="'UNDEFINED'" />
  <xsl:param name="version" select="'UNDEFINED'" />


  <!-- ### OUTPUT DECLARATION ### -->
  <xsl:output method="xml"
              version="1.0"
              encoding="ISO-8859-1"
              doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
              doctype-system="DTD/xhtml1-strict.dtd" />

  <xsl:strip-space elements="database" />


  <!-- ### DOCUMENT ELEMENTS ### -->
  <xsl:template match="/">
    &newline;
    <xsl:comment> This file was automatically generated. DO NOT EDIT! </xsl:comment>
    &newline;&newline;
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      &newline;&indent;
      <head>
      &newline;&indent;&indent;
      <meta http-equiv="Content-Type" content="text/xhtml; charset=ISO-8859-1" />
      &newline;&indent;&indent;
      <meta http-equiv="Content-Style-Type" content="text/css" />
      &newline;&indent;&indent;
      <meta http-equiv="Content-Language" content="en" />
      &newline;&indent;&indent;
      <link rel="stylesheet" href="style.css" type="text/css" />
      &newline;&indent;&indent;
      <title><xsl:value-of select="$name" /> Database</title>
      &newline;&indent;
    </head>
    &newline;&newline;&indent;
    <body>
      &newline;&newline;
      <xsl:call-template name="header" />
      &newline;
      <xsl:apply-templates />
      &newline;&newline;&indent;&indent;
      <p></p>
      &newline;&newline;
      <xsl:call-template name="header" />
      &newline;&newline;&indent;&indent;
      <p class="footer">
        <xsl:text disable-output-escaping="yes">Copyright &amp;copy; 2003 Per Cederberg. Permission
    is granted to copy this document verbatim in any medium, provided
    that this copyright notice is left intact.</xsl:text>
      </p>
      &newline;&newline;&indent;
    </body>
    &newline;
    </html>
  </xsl:template>

  <xsl:template match="*|text()">
  </xsl:template>

  <xsl:template name="header">
    &indent;&indent;
    <table class="navOuter">
      &newline;&indent;&indent;&indent;
      <tr>
        &newline;&indent;&indent;&indent;&indent;
        <td>
          &newline;&indent;&indent;&indent;&indent;&indent;
          <table class="navInner">
            &newline;&indent;&indent;&indent;&indent;&indent;&indent;
            <tr>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;&indent;
              <th>Overview</th>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;&indent;
              <td>Table</td>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;&indent;
              <td>Reference</td>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;&indent;
              <td>Diagram</td>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;&indent;
              <td>Index</td>
              &newline;&indent;&indent;&indent;&indent;&indent;&indent;
            </tr>
            &newline;&indent;&indent;&indent;&indent;&indent;
          </table>
          &newline;&indent;&indent;&indent;&indent;
        </td>
        &newline;&indent;&indent;&indent;&indent;
        <td class="last">
          <xsl:value-of select="$name" />
          <xsl:text> Database Documentation</xsl:text>
        </td>
        &newline;&indent;&indent;&indent;
      </tr>
      &newline;&indent;&indent;
    </table>
    &newline;&newline;&indent;
    <hr />
    &newline;&newline;
  </xsl:template>

</xsl:stylesheet>
