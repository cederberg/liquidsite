<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/xhtml; charset=UTF-8" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Language" content="en" />
    <meta name="robots" content="noindex" />
    <link rel="stylesheet" type="text/css"
          href="${liquidsite.linkTo('/liquidsite/system/style.css')}" />
<#if title?has_content>
    <title>${title}</title>
<#else>
    <title>Liquid Site</title>
</#if>
  </head>

<#if onload?has_content>
  <body onload="${onload}">
<#else>
  <body>
</#if>

<#macro menu entries=menudefault info=menuinfo>
    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="${liquidsite.linkTo('/liquidsite/system/images/liquidsite.jpeg')}"
               alt="Liquid Site" />
        </td>
        <td class="title" colspan="12">
  <#if title?has_content>
          <h1>${title}</h1>
  <#else>
          <h1>Liquid Site</h1>
  </#if>
        </td>
        <td class="end" rowspan="2">
<@info />
        </td>
      </tr>
      <tr>
        <td class="space">&nbsp;</td>
<@entries />
        <td class="filler">&nbsp;</td>
      </tr>
    </table>
</#macro>

<#macro menuentry name path=liquidsite.request.path isindex=false>
  <#local url = liquidsite.request.path>
  <#local link = liquidsite.linkTo(path)>
        <td class="space">&nbsp;</td>
  <#if url?ends_with(path) ||
       (isindex && (url?ends_with("/") || url?ends_with("index.html")))>
        <td class="active"
            onclick="location='${link}'"
            onmouseover="this.className='hover'"
            onmouseout="this.className='active'">
          <a href="${link}">${name}</a>
        </td>
  <#else>
        <td class="inactive"
            onclick="location='${link}'"
            onmouseover="this.className='hover'"
            onmouseout="this.className='inactive'">
          <a href="${link}">${name}</a>
        </td>
  </#if>
</#macro>

<#macro menudefault>
</#macro>

<#macro menuinfo>
          Version&nbsp;${liquidsite.version}<br />
          ${liquidsite.date}<br />
          <br />
          &nbsp;
</#macro>
