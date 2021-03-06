<#assign title>Publish ${type?cap_first}</#assign>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("date").focus();
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="5">
            <img src="images/icons/48x48/onoff.png" alt="Publish" />
          </td>
          <td colspan="2">
            <h2>Publish  ${type?cap_first}</h2>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Date:
          </th>
          <td>
            <input type="text" tabindex="1"
                   name="date" value="${date}" />
          </td>
        </tr>
        <tr>
          <th>
            Recursive:
          </th>
          <td>
            <input type="checkbox" tabindex="2"
                   name="recursive" value="true" />
            Publish all child objects as well.
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td>
            <textarea tabindex="3" rows="2" cols="30"
                      name="comment">${comment?html}</textarea>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="5" onclick="doPrevious();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="4">
              <img src="images/icons/24x24/online.png" />
              Publish
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
