<#include "header.ftl">

    <table class="form">
      <tr>
        <td class="decoration" rowspan="10">
          <img src="images/icons/48x48/login.png" alt="User" />
        </td>
        <td colspan="2">
          <h2>Welcome to Liquid Site!</h2>
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
      <tr>
        <th>
          Real&nbsp;Name:
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
        <th>
          Superuser:
        </th>
        <td>
<#if liquidsite.user.superuser>
          Yes
<#else>
          No
</#if>
        </td>
      </tr>
    </table>

<#include "footer.ftl">
