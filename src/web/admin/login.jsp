<%@ include file="header.jsp" %>
<% 
String  error = (String) request.getAttribute("error");
%>

    <form method="post" action="index.html">
      <table class="form">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/login.png" alt="Login" />
          </td>
          <td colspan="2">
            <h2>Login</h2>
<% if (error != null) { %>
            <p class="incorrect">Error: <%=error%></p>
<% } %>  
          </td>
        </tr>
        <tr>
          <th>Name:</th>
          <td><input type="text" name="user" value="" /></td>
        </tr>
        <tr>
          <th>Password:</th>
          <td><input type="password" name="password" value="" /></td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="submit">
              <img src="images/icons/24x24/ok.png" />
              Login
            </button>
          </td>
        </tr>
      </table>
    </form>

<%@ include file="footer.jsp" %>
