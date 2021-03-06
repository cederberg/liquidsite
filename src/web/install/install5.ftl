<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" action="install.html" accept-charset="UTF-8">
      <input type="hidden" name="step" value="5" />
      <input type="hidden" name="prev" value="" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="7">
            <img src="liquidsite/system/images/icons/48x48/install.png"
                 alt="Install" />
          </td>
          <td colspan="2">
            <h2>Verify Information (Step 5 of 5)</h2>

            <p>Please verify that all the information presented below
            is correct.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;Host:
          </th>
          <td>
            ${host?html}
          </td>
        </tr>
        <tr>
          <th>
            Database:
          </th>
          <td>
            ${database?html}
<#if createDatabase>
            (create database)
<#elseif updateVersion?has_content>
            (update from version ${updateVersion?html})
<#else>
            (existing database)
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Database&nbsp;User:
          </th>
          <td>
            ${databaseUser?html}
<#if createDatabaseUser>
            (create user)
<#else>
            (existing user)
</#if>
          </td>
        </tr>
<#if dataDir?has_content>
        <tr>
          <th>
            Data&nbsp;Directory:
          </th>
          <td>
            ${dataDir?html}
          </td>
        </tr>
</#if>
<#if adminUser?has_content>
        <tr>
          <th>
            Admin&nbsp;User:
          </th>
          <td>
            ${adminUser?html}
          </td>
        </tr>
</#if>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="2" onclick="previous()">
              <img src="liquidsite/system/images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="1">
              <img src="liquidsite/system/images/icons/24x24/save.png" />
              Install
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "../footer.ftl">
