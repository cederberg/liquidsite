<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("password0").item(0).focus();
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post">
      <input type="hidden" name="editpassword" value="true" />
      <input type="hidden" name="step" value="1" />
      <input type="hidden" name="prev" value="" />
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
            <input type="password" tabindex="1" size="20" 
                   name="password0" />
            <p>Enter your current password.</p>
          </td>
        </tr>
        <tr>
          <th>
            New&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" tabindex="2" size="20"
                   name="password1" />
            <p>Enter the new password.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" tabindex="3" size="20"
                   name="password2" />
            <p>Verify the new password by entering it again.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="5" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="4">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
