<?xml version="1.0" encoding="ISO-8859-1"?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/xhtml; charset=ISO-8859-1" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Language" content="en" />
    <meta name="robots" content="noindex,nofollow" />
    <link rel="stylesheet" href="style.css" type="text/css" />
    <title>Liquid Site Administration</title>
  </head>

<#if onload?has_content>
  <body onload="${onload}">
<#else>
  <body>
</#if>

    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="images/liquidsite.jpeg" alt="Liquid Site" />
        </td>
        <td class="title" colspan="12">
          <h1>Liquid Site Administration</h1>
        </td>
        <td class="end" rowspan="2">
          Version&nbsp;${liquidsite.version}<br />
          ${liquidsite.date}<br />
          <br />
<#if liquidsite.user?has_content>
          <a href="logout.html">Logout</a>
<#else>
          &nbsp;
</#if>
        </td>
      </tr>
      <tr>
        <td class="space">&nbsp;</td>
<#macro menutab name page isindex=false>
  <#local url = liquidsite.page.path>
        <td class="space">&nbsp;</td>
  <#if url?ends_with(page)>
        <td class="active"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <strong>${name}</strong>
        </td>
  <#elseif isindex && (url?ends_with("/") || url?ends_with("index.html"))>
        <td class="active"
            onclick="window.location='${page}'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <strong>${name}</strong>
        </td>
  <#else>
        <td class="inactive"
            onclick="window.location='${page}'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <a href="${page}">${name}</a>
        </td>
  </#if>
</#macro>
<#if liquidsite.user?has_content>
  <@menutab name="Home" page="home.html" isindex=true />
  <@menutab name="Site" page="site.html" />
  <@menutab name="Content" page="content.html" />
  <@menutab name="Users" page="users.html" />
  <@menutab name="System" page="system.html" />
<#else>
  <@menutab name="Login" page=liquidsite.page.path />
</#if>
        <td class="filler">&nbsp;</td>
      </tr>
    </table>
