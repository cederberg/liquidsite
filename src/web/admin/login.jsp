<% 
    String  error = (String) request.getAttribute("error");
%>
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

  <body onload="document.getElementsByName('liquidsite.login').item(0).focus()">

    <table class="menu">
      <tr>
        <td class="logo" rowspan="2">
          <img src="images/liquidsite.jpeg" alt="Liquid Site" />
        </td>
        <td clsss="title" colspan="4">
          <h1>Liquid Site Administration</h1>
        </td>
        <td class="extra">
          Version&nbsp;0.1<br />
          2003-10-01
        </td>
      </tr>
      <tr>
        <td class="space">&nbsp;</td>
        <td class="space">&nbsp;</td>
        <td class="active"
            onclick="window.location='index.html'"
            onmouseover="this.className='hoover'"
            onmouseout="this.className='active'">
          <a href="index.html">Login</a>
        </td>
        <td class="filler">&nbsp;</td>
        <td class="end">&nbsp;</td>
      </tr>
    </table>

    <form method="post">
      <table class="form">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/login.png" alt="Login" />
          </td>
          <td colspan="2">
            <h2>Login</h2>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <th>Name:</th>
          <td><input type="text" name="liquidsite.login" value="" /></td>
        </tr>
        <tr>
          <th>Password:</th>
          <td><input type="password" name="liquidsite.password" value="" /></td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="submit">
              <img src="images/icons/24x24/ok.png" />
              Login
            </button>
          </td>
        </tr>
      </table>
    </form>

<%@ include file="footer.jsp" %>
