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
    
String hostColor = "";
String userColor = "";
String passwordColor = "";
if (errorHost) {
    hostColor = " class=\"incorrect\"";
}
if (errorUsername) {
    userColor = " class=\"incorrect\"";
}
if (errorPassword) {
    passwordColor = " class=\"incorrect\"";
}
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (Step 1 of 5)</h2>

  <p>Welcome to the Liquid Site installation guide! By following
  the steps in this guide you will install the Liquide Site data
  repository and perform the basic setup. Help will be available
  throughout this guide, providing explainations and 
  recommendations.</p>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. Control that the information entered is correct.</p>
<% } else if (error) { %>
  <p class="incorrect">Insert information in the marked fields.</p>
<% } %>  

  <form method="post" action="install.html" id="form">
    <input type="hidden" name="step" value="1" />
    <input type="hidden" name="dbsel" value="<%= dbsel %>" />
    <input type="hidden" name="database" value="<%= database %>" />
    <input type="hidden" name="usersel" value="<%= usersel %>" />
    <input type="hidden" name="username" value="<%= username %>" />
    <input type="hidden" name="password" value="<%= password %>" />
    <input type="hidden" name="verify" value="<%= verify %>" />

    <table>
	  <tr>
	    <td colspan="2">Enter the name of the host where to install 
	      Liquid Site.</td>
	  </tr>
	  
      <tr>
        <td width="1%"><span<%= hostColor %>>Host:</span></td>
        <td><input type="text" name="host" value="<%= host %>" 
          size="20" /></td>
      </tr>

      <tr>
        <td colspan="2">Enter the information of the database user with
          which to perform the installation.</td>
      </tr>
  
      <tr>
        <td><span<%= userColor %>>Username:</span></td>
        <td><input type="text" name="rootUsername" 
          value="<%= rootUsername %>" size="12" /></td>
      </tr>
  
      <tr>
        <td><span<%= passwordColor %>>Password:</span></td>
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
