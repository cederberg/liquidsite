<?xml version="1.0" encoding="ISO-8859-1" ?>
 
<!-- ### ENTITY DECLARATIONS ### -->
<!DOCTYPE stylesheet [
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
    <xsl:call-template name="sql-comment">
      <xsl:with-param name="text" select="description" />
      <xsl:with-param name="indent" select="''" />
    </xsl:call-template>
    <xsl:text>CREATE TABLE </xsl:text>
    <xsl:value-of select="@name" />
    <xsl:text> (</xsl:text>
    &newline;
    <xsl:apply-templates />
    <xsl:text>);</xsl:text>
    &newline;
  </xsl:template>

  <xsl:template match="column">
    &newline;
    <xsl:call-template name="sql-comment">
      <xsl:with-param name="text" select="description" />
      <xsl:with-param name="indent" select="'    '" />
    </xsl:call-template>
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


  <!-- ### HELPER FUNCTIONS ### -->

  <xsl:template name="sql-comment">
    <xsl:param name="text" />
    <xsl:param name="indent" />
    <xsl:call-template name="linebreak-string">
      <xsl:with-param name="str" select="normalize-space($text)" />
      <xsl:with-param name="lineStart">
        <xsl:value-of select="$indent" />
        <xsl:text>-- </xsl:text>
      </xsl:with-param>
      <xsl:with-param name="lineLength" select="70" />
    </xsl:call-template>
    &newline;
  </xsl:template>

  <xsl:template name="linebreak-string">
    <!-- Adapted from a function by Kevin Manley published in
         http://www-106.ibm.com/developerworks/xml/library/x-tiplnbrk.html -->
    <xsl:param name="str" />
    <xsl:param name="lineStart" />
    <xsl:param name="lineLength" />
    <xsl:variable name="maxStrLength" 
                  select="$lineLength - string-length($lineStart)" />
    <xsl:choose>
      <xsl:when test="string-length($str) &lt;= $maxStrLength">
        <xsl:value-of select="$lineStart" />
        <xsl:value-of select="$str" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="strFirst">
          <xsl:call-template name="max-substring-ending-with-break">
            <xsl:with-param name="str" 
                            select="substring($str,1,$maxStrLength)" />
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="strRest" 
                      select="substring-after($str,$strFirst)" />
        <xsl:value-of select="$lineStart" />
        <xsl:value-of select="$strFirst" />
        &newline;
        <xsl:call-template name="linebreak-string">
          <xsl:with-param name="str" select="$strRest" />
          <xsl:with-param name="lineStart" select="$lineStart" />
          <xsl:with-param name="lineLength" select="$lineLength" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="max-substring-ending-with-break">
    <!-- Slightly adapted from a function by Kevin Manley published in
         http://www-106.ibm.com/developerworks/xml/library/x-tiplnbrk.html -->
    <xsl:param name="str" />
    <xsl:variable name="len" select="string-length($str)" />
    <xsl:choose>
      <xsl:when test="len &lt;= 1 or substring($str,$len)=' ' or contains($str,' ')=false">
        <xsl:value-of select="$str" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="max-substring-ending-with-break">
          <xsl:with-param name="str" select="substring($str, 1, $len - 1)" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
