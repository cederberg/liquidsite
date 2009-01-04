<#assign onload = "document.getElementsByName('liquidsite.login').item(0).focus()">
<#include "header.ftl">

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.action" value="login" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/login.png" alt="Login" />
          </td>
          <td colspan="2">
            <h2>Login</h2>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>Name:</th>
          <td>
            <input type="text" tabindex="1" size="20"
                   name="liquidsite.login" />
          </td>
        </tr>
        <tr>
          <th>Password:</th>
          <td>
            <input type="password" tabindex="2" size="20"
                   name="liquidsite.password" />
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="submit" tabindex="3">
              <img src="images/icons/24x24/ok.png" />
              Login
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
