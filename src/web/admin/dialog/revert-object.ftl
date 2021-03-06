<#assign title>Delete ${type?cap_first} Revision</#assign>
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
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
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/delete.png" alt="Delete Revision" />
          </td>
          <td colspan="2">
            <h2>Delete ${type?cap_first} Revision '${revision}'</h2>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Are you sure you wish to delete this
            revision?</strong>

            <p>Deleting the ${type} revision will remove it from the
            system, without possibility to recover at a later date.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button type="button" tabindex="2" onclick="doPrevious();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="1">
              <img src="images/icons/24x24/revert.png" />
              Revert
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
