<#assign title = "Delete Domain">
<#include "header.ftl">

    <form method="post">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="confirmed" value="true" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/delete.png" alt="Delete" />
          </td>
          <td>
            <h2>Delete Domain '${name}'</h2>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Are you sure you wish to delete this
            domain?</strong>

            <p>Deleting the domain will 
            <span class="important">remove all content in the
            domain</span> from the system, without possibility to 
            recover at a later date. Use "Unpublish" to only take 
            sites or content offline.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button onclick="window.close();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit">
              <img src="images/icons/24x24/delete.png" />
              Delete
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
