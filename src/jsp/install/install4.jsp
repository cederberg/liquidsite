<% 
String host = (String) request.getAttribute("host");
String rootUsername = (String) request.getAttribute("rootUsername");
String rootPassword = (String) request.getAttribute("rootPassword");
String dbchoice = (String) request.getAttribute("dbchoice");
String database = (String) request.getAttribute("database");
String dbsel = (String) request.getAttribute("dbsel");
String userchoice = (String) request.getAttribute("userchoice");
String usersel = (String) request.getAttribute("usersel");
String username = (String) request.getAttribute("username");
String password = (String) request.getAttribute("password");
String verify = (String) request.getAttribute("verify");
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (4 of 5)</h2>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="5" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="dbchoice" value="<%= dbchoice %>" />
    <input type="hidden" name="dbsel" value="<%= dbsel %>" />
    <input type="hidden" name="database" value="<%= database %>" />
    <input type="hidden" name="userchoice" value="<%= userchoice %>" />
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
        <td>Root password:</td>
        <td><%= rootPassword %></td>
      </tr>
  
      <tr>
        <td>Database:</td>
        <td><%= database %></td>
      </tr>
  
      <tr>
        <td>Username:</td>
        <td><%= username %></td>
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
