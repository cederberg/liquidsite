<%@ include file="header.jsp" %>
<% 
String  error = (String) request.getAttribute("error");
String  host = (String) request.getAttribute("host");
String  user = (String) request.getAttribute("user");
String  password = (String) request.getAttribute("password");
%>
    <script type="text/javascript">
        function initialize() {
            document.getElementsByName("host").item(0).focus();
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="1" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="5">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Select Database Server (Step 1 of 5)</h2>

            <p>Welcome to Liquid Site! By following the steps in this 
            installation guide, you will create a Liquid Site data
            repository and perform the basic setup.</p>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;Host:
          </th>
          <td class="field">
            <input type="text" name="host" value="<%=host%>" size="20" />
            <p>This is the machine name or IP address of the database
            to use for the Liquid Site repository.</p>
          </td>
        </tr>
        <tr>
          <th>
            User&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" name="user" value="<%=user%>" size="12" />
            <p>This database user is ONLY used during the 
            installation. If you wish to create a new database, use 
            a user with full administration privileges here. The 
            database user for normal usage by Liquid Site is entered
            in a later step.</p>
          </td>
        </tr>
        <tr>
          <th>
            Password:
          </th>
          <td class="field">
            <input type="password" name="password" 
                   value="<%=password%>" size="12" />
            <p>The password for the database user above.</p>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <button type="button" name="prev" disabled="disabled">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<%@ include file="footer.jsp" %>
