<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("name").focus();
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="edituser" value="true" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/home.png" alt="Edit" />
          </td>
          <td colspan="2">
            <h2>Change Account Details (Step 1 of 1)</h2>

<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="20"
                   name="name" value="${name?html}" />
          </td>
        </tr>
        <tr>
          <th>
            E-mail:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="30"
                   name="email" value="${email?html}" />
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="4" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="3">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
