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

java.util.ArrayList dbsInfo = 
    (java.util.ArrayList) request.getAttribute("dbsInfo");
java.util.Hashtable dbInfo;
String db;
int noTables;
String type;

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
    <input type="hidden" name="usersel" value="<%= usersel %>" />
    <input type="hidden" name="username" value="<%= username %>" />
    <input type="hidden" name="password" value="<%= password %>" />
    <input type="hidden" name="verify" value="<%= verify %>" />
  
    <table>
      <tr>
        <td>Select a database:</td>
      </tr>
      <tr>
        <td>
<% for (int i=0; i<dbsInfo.size(); i++) {
       dbInfo = (java.util.Hashtable) dbsInfo.get(i);
       db = (String) dbInfo.get("name");
       noTables = ((Integer) dbInfo.get("noTables")).intValue();
       type = (String) dbInfo.get("type");
       if (dbsel.equals(db)) {
%>
          <input checked type="radio" name="dbsel" 
            value="<%= db %>" /> <%= db %>
<%     } else { %>
          <input type="radio" name="dbsel" 
            value="<%= db %>" /> <%= db %>
<%     } 
       if (type.equals("conflict")) {
%>
          <span style="color: red">(<%= noTables %> tables found) 
          CONFLICT IN TABLES</span>
<%     } else if (type.equals("noaccess")) { %>
          <span style="color: gray">NOT ACCESSIBLE</span>
<%     } else if (type.equals("normal")) { %>
          (<%= noTables %> tables found)
<%     } else { %>
          (<%= noTables %> tables found) LIQUID SITE <%= type %>
<%     } %>
          <br />
<% }
   if (dbsel.equals("")) { 
%>
          <input checked type="radio" name="dbsel" value="" />
<% } else { %>
          <input type="radio" name="dbsel" value="" />
<% } %>
          Create new database: 
          <input type="input" name="database" value="<%= database %>" />
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
