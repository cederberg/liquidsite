<#assign title>Unlock ${type?cap_first}</#assign>
<#include "header.ftl">

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/lock.png" alt="Unlock" />
          </td>
          <td>
            <h2>Unlock ${type?cap_first}</h2>
          </td>
        </tr>
        <tr>
          <td>
            <strong>Are you sure you wish to unlock the
            ${type}?</strong>

            <p>Unlocking the ${type} will stop the current owner of the
            lock from saving any changes. Make sure the other user is
            aware of this before unlocking.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button type="button" tabindex="2" onclick="window.close();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="1">
              <img src="images/icons/24x24/lock.png" />
              Unlock
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
