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

java.util.Vector users = (java.util.Vector) request.getAttribute("users");
String user;

boolean error = 
    ((Boolean) request.getAttribute("error")).booleanValue();
boolean errorVerify = 
    ((Boolean) request.getAttribute("errorVerify")).booleanValue();
boolean errorUserExists = 
    ((Boolean) request.getAttribute("errorUserExists")).booleanValue();
boolean errorConnection = 
    ((Boolean) request.getAttribute("errorConnection")).booleanValue();
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (3 of 5)</h2>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. You may be experiencing errors with your database
    server.</p>
<% } else if (errorUserExists) { %>
  <p class="incorrect">The username entered already exists. If you
    want to choose this user, select it from the list.</p>
<% } else if (errorVerify) { %>
  <p class="incorrect">The password could not be verified. Type your
    password and verify it again.</p>
<% } else if (error) { %>
  <p class="incorrect">Select the radio button according to your
    preferences, and either select a user from the list, or enter a
    new user's information.</p>
<% } %>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="4" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="dbchoice" value="<%= dbchoice %>" />
    <input type="hidden" name="database" value="<%= database %>" />
    <input type="hidden" name="dbsel" value="<%= dbsel %>" />

    <table>
      <tr>
        <td>
<% if (userchoice.compareTo("select") == 0) { %>
          <input checked type="radio" name="userchoice" value="select" />
<% } else { %>
          <input type="radio" name="userchoice" value="select" />
<% } %>
        </td>
        <td class="fieldname">Select a user:</td>
        <td>
	  <select name="usersel">
            <option value="">Select a user</option>
<% if (users != null) {
       for (int i=0; i<users.size(); i++) {
           user = (String) users.elementAt(i);
           if (user.compareTo(usersel) == 0) {
%>
            <option selected value="<%= user %>"><%= user %></option>
<%         } else { %>
            <option value="<%= user %>"><%= user %></option>
<%         }
       } 
   }
%>
          </select></td>
      </tr>

      <tr>
        <td>
<% if (userchoice.compareTo("create") == 0) { %>
          <input checked type="radio" name="userchoice" value="create" />
<% } else { %>
          <input type="radio" name="userchoice" value="create" />
<% } %>
        </td>
        <td>Create new user:</td>
        <td>
          <input type="text" name="username" value="<%= username %>" />
        </td>
      </tr>

      <tr>
        <td></td>
        <td>
<% if (errorVerify) { %>
          <span class="incorrect">Password:</span>
<% } else { %>
          Password:
<% } %>
        </td>
        <td>
          <input type="password" name="password" value="<%= password %>" />
        </td>
      </tr>

      <tr>
        <td></td>
        <td>
<% if (errorVerify) { %>
          <span class="incorrect">Re-type password:</span>
<% } else { %>
          Re-type password:
<% } %>
        </td>
        <td>
          <input type="password" name="verify" value="<%= verify %>" />
        </td>
      </tr>

      <tr>
        <td colspan="3">
          <input type="button" name="submit" value="&lt;&lt; Previous" 
            onclick="submit();" />
          <input type="submit" name="submit" value="Next &gt;&gt;" /></td>
      </tr>
    </table>
  </form>

<%@ include file="footer.jsp" %>
