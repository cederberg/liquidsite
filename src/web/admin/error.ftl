<#include "header.ftl">

    <form method="post" accept-charset="UTF-8">
      <table class="form">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/error.png" alt="Error" />
          </td>
          <td>
            <h2>Error</h2>
          </td>
        </tr>
        <tr>
          <td>
            <p>${error}</p>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button type="button" onclick="window.location='${page}'">
              <img src="images/icons/24x24/ok.png" />
              OK
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
