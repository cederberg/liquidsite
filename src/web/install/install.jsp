<% 
String host = (String) request.getAttribute("host");
String rootUsername = (String) request.getAttribute("rootUsername");
String rootPassword = (String) request.getAttribute("rootPassword");
String dbchoice = (String) request.getAttribute("dbchoice");
String dbsel = (String) request.getAttribute("dbsel");
String database = (String) request.getAttribute("database");
String userchoice = (String) request.getAttribute("userchoice");
String usersel = (String) request.getAttribute("usersel");
String username = (String) request.getAttribute("username");
String password = (String) request.getAttribute("password");
String verify = (String) request.getAttribute("verify");

boolean error = 
    ((Boolean) request.getAttribute("error")).booleanValue();
boolean errorHost = 
    ((Boolean) request.getAttribute("errorHost")).booleanValue();
boolean errorUsername = 
    ((Boolean) request.getAttribute("errorUsername")).booleanValue();
boolean errorPassword = 
    ((Boolean) request.getAttribute("errorPassword")).booleanValue();
boolean errorConnection = 
    ((Boolean) request.getAttribute("errorConnection")).booleanValue();
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (1 of 5)</h2>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. Control that the information entered is correct.</p>
<% } else if (error) { %>
  <p class="incorrect">Insert information in marked fields.</p>
<% } %>  

  <form method="post" action="install.html" id="form">
    <input type="hidden" name="step" value="2" />
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
<% if (errorHost) { %>
        <td><span class="incorrect">Host:</span></td>
<% } else { %>
        <td>Host:</td>
<% } %>
        <td><input type="text" name="host" value="<%= host %>" 
          size="20" /></td>
      </tr>
  
      <tr>
<% if (errorUsername) { %>
        <td><span class="incorrect">Root username:</span></td>
<% } else { %>
        <td>Root username:</td>
<% } %>
        <td><input type="text" name="rootUsername" value="<%= rootUsername %>"
          size="12" /></td>
      </tr>
  
      <tr>
<% if (errorPassword) { %>
        <td><span class="incorrect">Root password:</span></td>
<% } else { %>
        <td>Root password:</td>
<% } %>
        <td><input type="password" name="rootPassword"
          value="<%= rootPassword %>" size="12" /></td>
      </tr>
  
      <tr>
        <td colspan="2">
          <input type="button" name="submit" value="&lt;&lt; Previous" 
            onclick="submit();" />
          <input type="submit" name="submit" value="Next &gt;&gt;" /></td>
      </tr>
    </table>
  </form>

<%@ include file="footer.jsp" %>
