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

boolean isAdmin = 
    ((Boolean) request.getAttribute("isAdmin")).booleanValue();

boolean error = 
    ((Boolean) request.getAttribute("error")).booleanValue();
boolean errorDbExists = 
    ((Boolean) request.getAttribute("errorDbExists")).booleanValue();
boolean errorConnection = 
    ((Boolean) request.getAttribute("errorConnection")).booleanValue();
    
String infoColor = "";
String info1 = "";
String info2 = "";
String dbchecked = "";
String crColor = "";
String crchecked = "";
String crenabled = "";
String nextenabled = "";
if (!isAdmin) {
    crenabled = " disabled";
    crColor = " class=\"unimportant\"";
} else if (dbsel.equals("")) {
    crchecked = " checked";
}
if (!isAdmin && dbsInfo.size() == 0) {
    nextenabled = " disabled";
}
if (error && !errorConnection && !errorDbExists) {
    crColor = " class=\"incorrect\"";
}
%>
<%@ include file="header.jsp" %>

  <h2>Liquid Site Installation (Step 2 of 5)</h2>

  <p>Indicate the Liquid Site database. Note that if you select an 
  existing database, all its information will be lost.</td>

<% if (errorConnection) { %>
  <p class="incorrect">A connection could not be established to the
    database. You may be experiencing errors with your database
    server.</p>
<% } else if (errorDbExists) { %>
  <p class="incorrect">The database to create already exists. If you
    want to choose this database, select it from the list.</p>
<% } else if (error) { %>
  <p class="incorrect">Enter the database name.</p>
<% } else if (!isAdmin && dbsInfo.size() == 0) { %>
  <p class="incorrect">There are no available databases. Either go back
    to the previous step and enter the information of a database user
    with administration rights, or ask your admin to create a database
    for your user.</p>
<% } %>

  <form method="post" action="install.html">
    <input type="hidden" name="step" value="2" />
    <input type="hidden" name="host" value="<%= host %>" />
    <input type="hidden" name="rootUsername" value="<%= rootUsername %>" />
    <input type="hidden" name="rootPassword" value="<%= rootPassword %>" />
    <input type="hidden" name="usersel" value="<%= usersel %>" />
    <input type="hidden" name="username" value="<%= username %>" />
    <input type="hidden" name="password" value="<%= password %>" />
    <input type="hidden" name="verify" value="<%= verify %>" />
  
    <table>
<% for (int i=0; i<dbsInfo.size(); i++) {
       dbInfo = (java.util.Hashtable) dbsInfo.get(i);
       db = (String) dbInfo.get("name");
       noTables = ((Integer) dbInfo.get("noTables")).intValue();
       type = (String) dbInfo.get("type");
       if (type.equals("conflict")) {
           infoColor = " class=\"incorrect\"";
           info1 = "(" + String.valueOf(noTables) + " tables found)";
           info2 = "Conflict in tables";
       } else if (type.equals("noaccess")) {
           infoColor = " class=\"unimportant\"";
           info1 = "";
           info2 = "Not accessible";
       } else if (type.equals("normal")) {
           infoColor = "";
           info1 = "(" + String.valueOf(noTables) + " tables found)";
           info2 = "";
       } else {
           infoColor = "";
           info1 = "(" + String.valueOf(noTables) + " tables found)";
           info2 = "Liquid Site " + type;
       }
       if (dbsel.equals(db)) {
           dbchecked = " checked";
       } else {
           dbchecked = "";
       }
%>
      <tr>
        <td>
          <input<%= dbchecked %> type="radio" name="dbsel" 
            value="<%= db %>" /> <span<%= infoColor %>><%= db %></span>
        </td>
        <td><span<%= infoColor %>><%= info1 %></span></td>
        <td><span<%= infoColor %>><%= info2 %></span></td>
      </tr>
<% } %>

      <tr>
        <td colspan="3">
          <input<%= crchecked %><%= crenabled %> type="radio" 
            name="dbsel" value="" />
          <span<%= crColor %>>Create new database:</span>
          <input<%= crenabled %> type="input" name="database" 
            value="<%= database %>" />
        </td>
      </tr>

      <tr>
        <td colspan="3">
          <input type="button" name="submit" value="&lt;&lt; Previous" 
            onclick="submit();" />
          <input<%= nextenabled %> type="submit" name="submit" 
            value="Next &gt;&gt;" /></td>
      </tr>
    </table>
  </form>

<%@ include file="footer.jsp" %>
