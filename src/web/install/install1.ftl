<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("host").item(0).focus();
    }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="1" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="5">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Select Database Server (Step 1 of 5)</h2>

            <p>Welcome to Liquid Site! By following the steps in this 
            installation guide, you will create a Liquid Site data
            repository and perform the basic setup.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>  
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;Host:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="20"
                   name="host" value="${host}" />
            <p>This is the machine name or IP address of the database
            to use for the Liquid Site repository.</p>
          </td>
        </tr>
        <tr>
          <th>
            User&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="12" 
                   name="user" value="${user}" />
            <p>This database user is ONLY used during the 
            installation. If you wish to create a new database, use 
            a user with full administration privileges here. The 
            database user for normal usage by Liquid Site is entered
            in a later step.</p>
          </td>
        </tr>
        <tr>
          <th>
            Password:
          </th>
          <td class="field">
            <input type="password" tabindex="3" size="12" 
                   name="password" value="${password}" />
            <p>The password for the database user above.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" disabled="disabled">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="4">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
