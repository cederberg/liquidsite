<#include "header.ftl">

    <script type="text/javascript">
    function editUser() {
        document.getElementsByName("edituser").item(0).value = "true";
        document.forms.item(0).submit();
    }

    function editPassword() {
        document.getElementsByName("editpassword").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" action="edit-home.html" accept-charset="UTF-8">
      <input type="hidden" name="edituser" value="" />
      <input type="hidden" name="editpassword" value="" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/home.png" alt="Home" />
          </td>
          <td colspan="2">
            <h2>Welcome to Liquid Site!</h2>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td>
            ${liquidsite.user.realName}
          </td>
        </tr>
        <tr>
          <th>
            E-mail:
          </th>
          <td>
            ${liquidsite.user.email}
          </td>
        </tr>
        <tr>
          <th style="width: 7em;">
            Login:
          </th>
          <td>
            ${liquidsite.user.login}
          </td>
        </tr>
<#if liquidsite.user.superuser>
        <tr>
          <th>
            Superuser:
          </th>
          <td>
            Yes
          </td>
        </tr>
</#if>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="1" onclick="editUser()">
              <img src="images/icons/24x24/edit.png" />
              Edit
            </button>
            <button type="button" tabindex="2" onclick="editPassword()">
              <img src="images/icons/24x24/edit.png" />
              Change Password
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
