<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/xhtml; charset=UTF-8" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Language" content="en" />
    <meta name="robots" content="noindex,nofollow" />
    <link rel="stylesheet" href="style.css" type="text/css" />
<#if title?has_content>
    <title>${title}</title>
<#else>
    <title>Liquid Site Administration</title>
</#if>
  </head>

<#if onload?has_content>
  <body onload="${onload}">
<#else>
  <body>
</#if>
