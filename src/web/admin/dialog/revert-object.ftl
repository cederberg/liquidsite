<#assign title>Delete ${type?cap_first} Revision</#assign>
<#include "header.ftl">

    <form method="post">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="confirmed" value="true" />
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
<#assign cancel>revert-site.html?type=${type}&id=${id}&cancel=true</#assign>
            <button type="button" onclick="window.location='${cancel}'">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit">
              <img src="images/icons/24x24/revert.png" />
              Revert
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
