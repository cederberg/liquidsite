<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("dir").item(0).focus();
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" action="install.html" accept-charset="UTF-8">
      <input type="hidden" name="step" value="4" />
      <input type="hidden" name="prev" value="" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="6">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Configure Liquid Site (Step 4 of 5)</h2>

            <p>Please enter the default configuration information for
            Liquid Site.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
<#if updateVersion?has_content>
        <tr>
          <td colspan="2">
            No configuration updates are needed. All the configuration
            parameters from the previous version ${updateVersion} will
            be transferred without changes.
          </td>
        </tr>
<#else>
        <tr>
          <th>
            Data&nbsp;Directory:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="40"
                   name="dir" value="${dir}" />
            <p>This is the directory on the web server machine
            containing binary data files.</p>
          </td>
        </tr>
        <tr>
          <th>
            Admin&nbsp;User:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="20"
                   name="user" value="${user}" />
            <p>This is the default Liquid Site administrator user.
            This user cannot be removed unless a new Liquid Site
            administrator is created.</p>
          </td>
        </tr>
        <tr>
          <th>
            Admin&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" tabindex="3" size="20"
                   name="password1" value="${password}" />
            <p>The password for the admin user above.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" tabindex="4" size="12"
                   name="password2" value="${password}" />
            <p>Password verification for the admin user above.</p>
          </td>
        </tr>
</#if>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="6" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="5">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
