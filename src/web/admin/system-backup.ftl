<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("domain").focus();
    }

    function doPrevious() {
        window.location='system.html'
    }

    function doBackup() {
        return true;
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="action" value="backup" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/system.png" alt="System" />
          </td>
          <td colspan="2">
            <h2>Select Backup Options (Step 1 of 1)</h2>

            <p>Select the options for the backup operation.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Domain:
          </th>
          <td class="field">
            <select tabindex="1" name="domain">
              <option value="">&lt; None &gt;</option>
<#list domains as item>
              <option value="${item}">${item}</option>
</#list>
            </select>
            <p>Only the single domain specified will be backed up.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="103" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="102" onclick="return doBackup();">
              <img src="images/icons/24x24/save.png" />
              Backup
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
