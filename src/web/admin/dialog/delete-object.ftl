<#assign title>Delete ${type?cap_first}</#assign>
<#include "header.ftl">

    <script type="text/javascript">
    function previous() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
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
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/delete.png" alt="Delete" />
          </td>
          <td>
            <h2>Delete ${type?cap_first} '${name}'</h2>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Are you sure you wish to delete this
            ${type}?</strong>

<#if type = "domain">
            <p>Deleting the domain will
            <span class="important">remove all sites, pages and
            content in the domain</span> from the system, without
            possibility to recover at a later date. Use "Unpublish"
            to only take sites or content offline.</p>
<#elseif type = "site" || type = "folder">
            <p>Deleting the ${type} will remove it and all child
            pages from the database, without possibility to recover
            at a later date. Use "Unpublish" to only take the ${type}
            offline.</p>
<#else>
            <p>Deleting the ${type} will remove it from the database,
            without possibility to recover at a later date. Use
            "Unpublish" to only take the ${type} offline.</p>
</#if>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button type="button" tabindex="2" onclick="previous();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="1">
              <img src="images/icons/24x24/delete.png" />
              Delete
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
