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
<#if liquidsite.user?has_content>
        <td class="space">&nbsp;</td>
        <td class="active"
            onclick="window.location='home.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <a href="home.html">Home</a>
        </td>
        <td class="space">&nbsp;</td>
        <td class="inactive"
            onclick="window.location='site.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='inactive'">
          <a href="site.html">Site</a>
        </td>
        <td class="space">&nbsp;</td>
        <td class="inactive"
            onclick="window.location='content.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='inactive'">
          <a href="content.html">Content</a>
        </td>
        <td class="space">&nbsp;</td>
        <td class="inactive"
            onclick="window.location='users.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='inactive'">
          <a href="users.html">Users</a>
        </td>
        <td class="space">&nbsp;</td>
        <td class="inactive"
            onclick="window.location='system.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='inactive'">
          <a href="system.html">System</a>
        </td>
<#else>
        <td class="space">&nbsp;</td>
        <td class="active"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <strong>Login</strong>
        </td>
</#if>
        <td class="filler">&nbsp;</td>
      </tr>
    </table>
