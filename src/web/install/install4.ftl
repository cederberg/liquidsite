<#include "header.ftl">

    <script type="text/javascript">
        function initialize() {
            document.getElementsByName("dir").item(0).focus();
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="4" />
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
        <tr>
          <th>
            Data&nbsp;Directory:
          </th>
          <td class="field">
            <input type="text" name="dir" value="${dir}" size="40" />
            <p>This is the directory on the web server machine 
            containing binary data files.</p>
          </td>
        </tr>
        <tr>
          <th>
            Admin&nbsp;User:
          </th>
          <td class="field">
            <input type="text" name="user" value="${user}" size="20" />
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
            <input type="password" name="password1" 
                   value="${password}" size="20" />
            <p>The password for the admin user above.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" name="password2" 
                   value="${password}" size="12" />
            <p>Password verification for the admin user above.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
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

<#include "footer.ftl">
