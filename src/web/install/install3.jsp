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

java.util.ArrayList users = 
    (java.util.ArrayList) request.getAttribute("users");
String user;

boolean isAdmin = 
    ((Boolean) request.getAttribute("isAdmin")).booleanValue();

boolean error = 
    ((Boolean) request.getAttribute("error")).booleanValue();
boolean errorUsername = 
    ((Boolean) request.getAttribute("errorUsername")).booleanValue();
boolean errorPassword = 
    ((Boolean) request.getAttribute("errorPassword")).booleanValue();
boolean errorVerify = 
    ((Boolean) request.getAttribute("errorVerify")).booleanValue();
boolean errorVerification = 
    ((Boolean) request.getAttribute("errorVerification")).booleanValue();
boolean errorUserExists = 
    ((Boolean) request.getAttribute("errorUserExists")).booleanValue();
boolean errorConnection = 
    ((Boolean) request.getAttribute("errorConnection")).booleanValue();

String userColor = "";
String paswdColor = "";
String verifyColor = "";
if (errorUsername) {
    userColor = " class=\"incorrect\"";
}
if (errorPassword || errorVerification) {
    paswdColor = " class=\"incorrect\"";
}
if (errorVerify || (errorVerification && usersel.equals(""))) {
    verifyColor = " class=\"incorrect\"";
}

String usrchecked = "";
String crchecked = "";
if (!isAdmin) {
    usersel = (String) users.get(0);
} else if (isAdmin && usersel.equals("")) {
    crchecked = " checked";
}
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (Step 3 of 5)</h2>

  <p>Indicate the Liquid Site database user. Note that selecting an 
  existing user will not alter the permissions it may already have, but 
  will only add access permissions to the Liquid Site database to it.</p>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. You may be experiencing errors with your database
    server.</p>
<% } else if (errorUsername || errorPassword || errorVerify) { %>
  <p class="incorrect">Insert information in the marked fields.</p>
<% } else if (errorUserExists) { %>
  <p class="incorrect">The username entered already exists. If you
    want to choose this user, select it from the list.</p>
<% } else if (errorVerification && usersel.equals("")) { %>
  <p class="incorrect">The password could not be verified. Type your
    password again.</p>
<% } else if (errorVerification) { %>
  <p class="incorrect">The given password is incorrect. 
    Type it again.</p>
<% } %>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="3" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="database" value="<%= database %>" />
    <input type="hidden" name="dbsel" value="<%= dbsel %>" />

    <table>
<% for (int i=0; i<users.size(); i++) {
       user = (String) users.get(i);
       if (usersel.equals(user)) {
           usrchecked = " checked";
       } else {
           usrchecked = "";
       }
%>
      <tr>
        <td width="1%">
          <input<%= usrchecked %> type="radio" name="usersel" 
            value="<%= user %>" /></td>
        <td colspan="2"><%= user %></td>
      </tr>
<% }

    if (isAdmin) {
%>
      <tr>
        <td>
          <input<%= crchecked %> type="radio" name="usersel" value="" />
        </td>
        <td width="1%">
          <span<%= userColor %>>Create&nbsp;new&nbsp;user:</span>
        </td>
        <td>
          <input type="input" name="username" value="<%= username %>" />
        </td>
      </tr>
      
      <tr>
        <td colspan="3">Enter the password of the selected user, or a
          new password if you chose to create a user.</td>
      </tr>

      <tr>
	<td></td>
        <td><span<%= paswdColor %>>Password:</span></td>
        <td>
          <input type="password" name="password" value="<%= password %>" />
        </td>
      </tr>

      <tr>
        <td colspan="3">Re-type the password if you chose to create a 
          user.</td>
      </tr>

      <tr>
        <td></td>
        <td><span<%= verifyColor %>>Re-type&nbsp;password:</span></td>
        <td>
          <input type="password" name="verify" value="<%= verify %>" />
        </td>
      </tr>
<% } %>

      <tr>
        <td colspan="3">
          <input type="button" name="submit" value="&lt;&lt; Previous" 
            onclick="submit();" />
          <input type="submit" name="submit" value="Next &gt;&gt;" /></td>
      </tr>
    </table>
  </form>

<%@ include file="footer.jsp" %>
