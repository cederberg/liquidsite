<%@ include file="header.jsp" %>
<% 
String   error = (String) request.getAttribute("error");
String   host = (String) request.getAttribute("host");
String   database = (String) request.getAttribute("database");
String   databaseUser = (String) request.getAttribute("databaseuser");
String   dataDir = (String) request.getAttribute("datadir");
String   adminUser = (String) request.getAttribute("adminuser");
Boolean  createDatabase = (Boolean) request.getAttribute("createdatabase");
Boolean  createDatabaseUser = (Boolean) request.getAttribute("createuser");
%>
    <script type="text/javascript">
        function initialize() {
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="5" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="7">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Verify Information (Step 5 of 5)</h2>

            <p>Please verify that all the information presented below
            is correct.</p>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;Host:
          </th>
          <td>
            <%=host%>
          </td>
        </tr>
        <tr>
          <th>
            Database:
          </th>
          <td>
            <%=database%>
<% if (createDatabase.booleanValue()) { %>
            (create database)
<% } else { %>
            (existing database)
<% } %>
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;User:
          </th>
          <td>
            <%=databaseUser%>
<% if (createDatabaseUser.booleanValue()) { %>
            (create user)
<% } else { %>
            (existing user)
<% } %>
          </td>
        </tr>
        <tr>
          <th>
            Data&nbsp;Directory:
          </th>
          <td>
            <%=dataDir%>
          </td>
        </tr>
        <tr>
          <th>
            Admin&nbsp;User:
          </th>
          <td>
            <%=adminUser%>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit">
              <img src="images/icons/24x24/save.png" />
              Install
            </button>
          </td>
        </tr>
      </table>
    </form>

<%@ include file="footer.jsp" %>
