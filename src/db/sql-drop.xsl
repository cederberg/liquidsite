<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE xsl:stylesheet [
<!ENTITY newline "<xsl:text>
</xsl:text>">
<!ENTITY indent "<xsl:text>    </xsl:text>">
<!ENTITY comment "<xsl:text>-- </xsl:text>">
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- ### INPUT PARAMETERS ### -->
  <xsl:param name="date" select="'UNDEFINED'" />
  <xsl:param name="name" select="'UNDEFINED'" />
  <xsl:param name="version" select="'UNDEFINED'" />


  <!-- ### OUTPUT DECLARATION ### -->
  <xsl:output method="text"
              encoding="ISO-8859-1" />

  <xsl:strip-space elements="database" />


  <!-- ### DOCUMENT ELEMENTS ### -->
  <xsl:template match="/database">
    &comment;&newline;&comment;
    <xsl:text>Database Cleanup for </xsl:text>
    <xsl:value-of select="$name" />
    <xsl:text> version </xsl:text>
    <xsl:value-of select="$version" />
    <xsl:text> (</xsl:text>
    <xsl:value-of select="$date" />
    <xsl:text>).</xsl:text>
    &newline;&comment;&newline;&comment;
    <xsl:text>This file has been automatically generated. DO NOT EDIT!</xsl:text>
    &newline;&comment;
    &newline;&newline;&newline;
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="table">
    <xsl:text>DROP TABLE IF EXISTS </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text>;</xsl:text>
    &newline;
  </xsl:template>

  <xsl:template match="*|text()">
  </xsl:template>

</xsl:stylesheet>
