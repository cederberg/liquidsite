<%@ include file="header.jsp" %>
<% 
String    error = (String) request.getAttribute("error");
String[]  userNames = (String[]) request.getAttribute("userNames");
Boolean   enableCreate = (Boolean) request.getAttribute("enableCreate");
String    user = (String) request.getAttribute("user");
String    password = (String) request.getAttribute("password");
String    options;
boolean   found = false;
%>
    <script type="text/javascript">
        function initialize() {
            var  user1 = document.getElementsByName("user1").item(0);
            var  user2 = document.getElementsByName("user2").item(0);
            var  pwd2 = document.getElementsByName("password2").item(0);

            if (user1.value == "") {
                user2.disabled = "";
                pwd2.disabled = "";
            } else {
                user2.disabled = "disabled";
                pwd2.disabled = "disabled";
            }
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="3" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="5">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Select Database User (Step 3 of 5)</h2>

            <p>Select the database user to use when running Liquid 
            Site normally. It is highly recommended to create a new
            user with minimal privileges.</p>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <th>
            User&nbsp;Name:
          </th>
          <td class="field">
            <select name="user1" onchange="initialize()">
<%
   for (int i = 0; i < userNames.length; i++) {
       options = "";
       if (userNames[i].equals(user)) {
           options = " selected=\"selected\"";
           found = true;
       }
%>
              <option value="<%=userNames[i]%>"<%=options%>><%=userNames[i]%></option>
<% 
   }
   options = (found) ? "" : " selected=\"selected\"";
   if (enableCreate.booleanValue()) {
%>
              <option value=""<%=options%>>Create New --&gt;</option>
<% } %>
            </select>
            <input type="text" name="user2" value="<%=user%>" size="20" />
            <p>This is the database user name to use for accessing 
            the Liquid Site database.</p>
          </td>
        </tr>
        <tr>
          <th>
            Password:
          </th>
          <td class="field">
            <input type="password" name="password1" 
                   value="<%=password%>" size="12" />
            <p>The password for the database user above.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" name="password2" 
                   value="<%=password%>" size="12" />
            <p>Verify the password for the database user. This field 
            is only used when creating new users.</p>
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <button type="submit" name="prev">
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
