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

java.util.Vector databases  = 
    (java.util.Vector) request.getAttribute("databases");
String db;

boolean error = 
    ((Boolean) request.getAttribute("error")).booleanValue();
boolean errorDbExists = 
    ((Boolean) request.getAttribute("errorDbExists")).booleanValue();
boolean errorConnection = 
    ((Boolean) request.getAttribute("errorConnection")).booleanValue();
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (2 of 5)</h2>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. You may be experiencing errors with your database
    server.</p>
<% } else if (errorDbExists) { %>
  <p class="incorrect">The database entered already exists. If you
    want to choose this database, select it from the list.</p>
<% } else if (error) { %>
  <p class="incorrect">Select the radio button according to your
    preferences, and either select a database from the list, or enter
    a new database name.</p>
<% } %>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="3" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="userchoice" value="<%= userchoice %>" />
    <input type="hidden" name="usersel" value="<%= usersel %>" />
    <input type="hidden" name="username" value="<%= username %>" />
    <input type="hidden" name="password" value="<%= password %>" />
    <input type="hidden" name="verify" value="<%= verify %>" />
  
    <table>
      <tr>
        <td>
<% if (dbchoice.compareTo("select") == 0) { %>
	  <input checked type="radio" name="dbchoice" value="select" />
<% } else { %>
          <input type="radio" name="dbchoice" value="select" />
<% } %>
        </td>
        <td>Select a database:</td>
        <td>
	  <select name="dbsel">
            <option value="">Select a database</option>
<% if (databases != null) {
       for (int i=0; i<databases.size(); i++) {
           db = (String) databases.elementAt(i);
           if (db.compareTo(dbsel) == 0) {
%>
            <option selected value="<%= db %>"><%= db %></option>
<%         } else { %>
            <option value="<%= db %>"><%= db %></option>
<%         } 
       }
   }
%>
          </select></td>
      </tr>

      <tr>
        <td>
<% if (dbchoice.compareTo("create") == 0) { %>
          <input checked type="radio" name="dbchoice" value="create" />
<% } else { %>
          <input type="radio" name="dbchoice" value="create" />
<% } %>
        </td>
        <td>Create new database:</td>
        <td><input type="text" name="database" value="<%= database %>" /></td>
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
