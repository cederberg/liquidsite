<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("password0").item(0).focus();
    }
    </script>

    <form method="post">
      <input type="hidden" name="editpassword" value="true" />
      <input type="hidden" name="step" value="1" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/login.png" alt="Change Password" />
          </td>
          <td colspan="2">
            <h2>Change Password (Step 1 of 1)</h2>

<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Current&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" name="password0" size="20" />
            <p>Enter your current password.</p>
          </td>
        </tr>
        <tr>
          <th>
            New&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" name="password1" size="20" />
            <p>Enter the new password.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" name="password2" size="20" />
            <p>Verify the new password by entering it again.</p>
          </td>
        </tr>

        <tr>
          <td class="buttons" colspan="2">
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
