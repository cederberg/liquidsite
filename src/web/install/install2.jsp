<%@ include file="header.jsp" %>
<% 
String    error = (String) request.getAttribute("error");
String    database = (String) request.getAttribute("database");
String[]  databaseNames = (String[]) request.getAttribute("databaseNames");
int[]     databaseStatus = (int[]) request.getAttribute("databaseStatus");
int[]     databaseTables = (int[]) request.getAttribute("databaseTables");
String[]  databaseInfo = (String[]) request.getAttribute("databaseInfo");
Boolean   enableCreate = (Boolean) request.getAttribute("enableCreate");
Boolean   enableNext = (Boolean) request.getAttribute("enableNext");
boolean   found = false;
String    style;
String    options;
String    str;
%>
    <script type="text/javascript">
        function initialize() {
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="2" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="3">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td>
            <h2>Select Database (Step 2 of 5)</h2>

            <p>Select the database where you wish to store the Liquid
            Site data. Note that no information will be lost, even if
            an existing database is chosen.</p>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <td class="field">
            <table class="border">
              <tr>
                <th>Database</th>
                <th>Statistics</th>
                <th>Information</th>
              </tr>
<%
   for (int i=0; i < databaseNames.length; i++) {
       style = "";
       options = "";
       if (database.equals(databaseNames[i])) {
           found = true;
       }
       if (databaseStatus[i] == 0) {
           style = " class=\"unimportant\"";
           options = "disabled=\"disabled\"";
       } else if (database.equals(databaseNames[i])) {
           options = "checked=\"checked\"";
       }
%>
              <tr<%=style%>>
                <td>
                  <input type="radio" name="database1" <%=options%>
                         value="<%=databaseNames[i]%>" /> 
                  <%=databaseNames[i]%>
                </td>
                <td>
                  <%=databaseTables[i]%> Tables
                </td>
                <td>
                  <%=databaseInfo[i]%>
                </td>
              </tr>
<% } %>
<%
   style = "";
   options = "";
   str = "";
   if (!enableCreate.booleanValue()) {
       style = " class=\"unimportant\"";
       options = "disabled=\"disabled\"";
   } else if (!found && !database.equals("")) {
       str = database;
       options = "checked=\"checked\"";
   }
%>
              <tr<%=style%>>
                <td>
                  <input type="radio" name="database1" value="" <%=options%> />
                  <input type="input" name="database2" <%=options%>
                         value="<%=str%>" />
                </td>
                <td colspan="2">
                  Creates a new database with the specified name.
                </td>
              </tr>
            </table>
          </td>
        </tr>
<%
   options = "";
   if (!enableNext.booleanValue()) {
       options = " disabled=\"disabled\"";
   }
%>
        <tr>
          <td>
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit"<%=options%>>
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<%@ include file="footer.jsp" %>
