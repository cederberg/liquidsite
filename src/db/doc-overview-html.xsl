<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>  </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INCLUDE COMMON STYLESHEET ### -->
  <xsl:include href="doc-common-html.xsl" />


  <!-- ### DOCUMENT ELEMENTS ### -->
  <xsl:template match="database">
    &newline;&indent;&indent;
    <h1><xsl:value-of select="$name" /> Database Overview</h1>
    &newline;&indent;&indent;
    <p><xsl:value-of select="description" /></p>
    &newline;&indent;&indent;
    <table>
      &newline;&indent;&indent;&indent;
      <tr>
        &newline;&indent;&indent;&indent;&indent;
        <th colspan="2">
          <xsl:value-of select="$name" />
          <xsl:text> Tables</xsl:text>
        </th>
        &newline;&indent;&indent;&indent;
      </tr>
      &newline;
      <xsl:apply-templates />
      &indent;&indent;
    </table>
    &newline;
  </xsl:template>

  <xsl:template match="table">
    &indent;&indent;&indent;
    <tr>
      &newline;&indent;&indent;&indent;&indent;
      <td>
        <a>
          <xsl:attribute name="href">
            <xsl:text>table.</xsl:text>
            <xsl:value-of select="@name" />
            <xsl:text>.html</xsl:text>
          </xsl:attribute>
          <xsl:value-of select="@name" />
        </a>
      </td>
      &newline;&indent;&indent;&indent;&indent;
      <td>
        <xsl:value-of select="substring-before(description,'.')" />
        <xsl:text>.</xsl:text>
      </td>
      &newline;&indent;&indent;&indent;
    </tr>
    &newline;
  </xsl:template>

</xsl:stylesheet>
