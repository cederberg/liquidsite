<#assign title = "Delete Site">
<#include "header.ftl">

    <form method="post">
      <input type="hidden" name="confirmed" value="true" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/delete.png" alt="Delete" />
          </td>
          <td>
            <h2>Delete Site</h2>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Are you sure you wish to delete the
            site?</strong>

            <p>Deleting the site will remove all pages and files
            in the site, without possibility to recover at a later 
            date. Use "Unpublish" to only take the site offline.</p>
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
