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
    <xsl:text>Database Schema for </xsl:text>
    <xsl:value-of select="$name" />
    <xsl:text> version </xsl:text>
    <xsl:value-of select="$version" />
    <xsl:text> (</xsl:text>
    <xsl:value-of select="$date" />
    <xsl:text>).</xsl:text>
    &newline;&comment;&newline;&comment;
    <xsl:text>This file has been automatically generated. DO NOT EDIT!</xsl:text>
    &newline;&comment;&newline;
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="table">
    &newline;&newline;
    <xsl:text>CREATE TABLE </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text> (</xsl:text>
    &newline;
    <xsl:apply-templates />
    <xsl:text>);</xsl:text>
    &newline;
  </xsl:template>

  <xsl:template match="column">
    &indent;
    <xsl:value-of select="@name" />
    <xsl:apply-templates />
    <xsl:if test="not(position()=last())">
      <xsl:text>,</xsl:text>
    </xsl:if>
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

  <xsl:template match="primarykey">
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
