<% 
String host = (String) request.getAttribute("host");
String rootUsername = (String) request.getAttribute("rootUsername");
String rootPassword = (String) request.getAttribute("rootPassword");
String dbsel = (String) request.getAttribute("dbsel");
String database = (String) request.getAttribute("database");
String usersel = (String) request.getAttribute("usersel");
String username = (String) request.getAttribute("username");
String password = (String) request.getAttribute("password");
String verify = (String) request.getAttribute("verify");

String db = (dbsel.equals("")) ? database : dbsel;
String user = (usersel.equals("")) ? username : usersel;
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (Step 4 of 5)</h2>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="4" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="dbsel" value="<%= dbsel %>" />
    <input type="hidden" name="database" value="<%= database %>" />
    <input type="hidden" name="usersel" value="<%= usersel %>" />
    <input type="hidden" name="username" value="<%= username %>" />
    <input type="hidden" name="password" value="<%= password %>" />
    <input type="hidden" name="verify" value="<%= verify %>" />
  
    <table>
      <tr>
        <td>Host:</td>
        <td><%= host %></td>
      </tr>
  
      <tr>
        <td>Root username:</td>
        <td><%= rootUsername %></td>
      </tr>
  
      <tr>
        <td>Database:</td>
        <td><%= db %></td>
      </tr>
  
      <tr>
        <td>Username:</td>
        <td><%= user %></td>
      </tr>

      <tr>
        <td colspan="2">
          <input type="button" name="submit" value="&lt;&lt; Previous" 
            onclick="submit();" />
          <input type="submit" name="submit" value="Install" /></td>
      </tr>
    </table>
  </form>

<%@ include file="footer.jsp" %>
