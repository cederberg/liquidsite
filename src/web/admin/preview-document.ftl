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
    <title>Liquid Site Document Preview</title>
  </head>

  <body>

    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="../../images/liquidsite.jpeg" alt="Liquid Site" />
        </td>
        <td class="title" colspan="12">
          <h1>Liquid Site Document Preview</h1>
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


    <table class="border">
<#list properties as prop>
      <tr>
        <th>
          ${prop.name}:
        </th>
        <td>
          ${liquidsite.doc[prop.id]}
        </td>
      </tr>
</#list>
    </table>

  </body>
</html>
