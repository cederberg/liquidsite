<#assign title>Publish ${type?cap_first}</#assign>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("date").item(0).focus();
    }
    </script>

    <form method="post">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/onoff.png" alt="Publish" />
          </td>
          <td colspan="2">
            <h2>Publish  ${type?cap_first}</h2>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Date:
          </th>
          <td>
            <input type="text" name="date" value="${date}" />
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td>
            <textarea name="comment" rows="2" cols="30">${comment}</textarea>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
<#assign cancel>publish-site.html?type=${type}&id=${id}&cancel=true</#assign>
            <button type="button" onclick="window.location='${cancel}'">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit">
              <img src="images/icons/24x24/online.png" />
              Publish
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
