<#include "header.ftl">

    <form method="post" action="edit-home.html">
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
            <button type="submit" tabindex="1" name="edituser">
              <img src="images/icons/24x24/edit.png" />
              Edit
            </button>
            <button type="submit" tabindex="2" name="editpassword">
              <img src="images/icons/24x24/edit.png" />
              Change Password
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
