<?xml version="1.0" encoding="UTF-8"?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "DTD/xhtml1-strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv="Content-Type" content="text/xhtml; charset=UTF-8" />
    <meta http-equiv="Content-Style-Type" content="text/css" />
    <meta http-equiv="Content-Language" content="en" />
    <meta name="robots" content="noindex,nofollow" />
    <link rel="stylesheet" href="../../style.css" type="text/css" />
    <title>Liquid Site Section Preview</title>
  </head>

  <body>

    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="../../images/liquidsite.jpeg" alt="Liquid Site" />
        </td>
        <td class="title" colspan="12">
          <h1>Liquid Site Section Preview</h1>
        </td>
        <td class="end" rowspan="2">
          Version&nbsp;${liquidsite.version}<br />
          ${liquidsite.date}<br />
          <br />
          &nbsp;
        </td>
      </tr>
      <tr>
        <td class="filler">&nbsp;</td>
      </tr>
    </table>


    <h2>Document Properties</h2>
    
    <p>The properties for documents in this section.</p>

    <table class="border">
      <tr>
        <th>Identifier</th>
        <th>Name</th>
        <th>Type</th>
        <th>Description</th>
      </tr>      
<#list properties as prop>
      <tr>
        <td>
          ${prop.id}
        </td>
        <td>
          ${prop.name}
        </td>
        <td>
  <#if prop.type == 1>
          Single-line Text
  <#elseif prop.type == 2>
          Multi-line Text
  <#else>
          Formatted Text (HTML)
  </#if>
        </td>
        <td>
          ${prop.description}
        </td>
      </tr>
</#list>
    </table>

  </body>
</html>
