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
    <title>Liquid Site Installation</title>
  </head>

  <body onload="initialize()">

    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="images/liquidsite.jpeg" alt="Liquid Site" />
        </td>
        <td clsss="title" colspan="4">
          <h1>Liquid Site Installation</h1>
        </td>
        <td class="extra">
          Version&nbsp;${liquidsite.version}<br />
          ${liquidsite.date}
        </td>
      </tr>
      <tr>
        <td class="space">&nbsp;</td>
        <td class="space">&nbsp;</td>
        <td class="active"
            onclick="window.location='install.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <a href="install.html">Install</a>
        </td>
        <td class="filler">&nbsp;</td>
        <td class="end">&nbsp;</td>
      </tr>
    </table>
