<#include "header.ftl">

    <form method="post" accept-charset="UTF-8">
      <table class="form">
        <tr>
          <td class="decoration" rowspan="4">
            <img src="images/icons/48x48/info.png" alt="Info" />
          </td>
          <td>
            <h2>Information</h2>
          </td>
        </tr>
        <tr>
          <td>
            <p>${message?cap_first}</p>
<#if detail?has_content>
            <div class="example">
              <p><code>${detail?html?replace("\n","<br/>")}</code></p>
            </div>
</#if>
          </td>
        </tr>
        <tr>
          <td class="buttons">
<#if page?has_content>
            <button type="button" onclick="window.location='${page}'">
<#else>
            <button type="button" onclick="window.close()">
</#if>
              <img src="images/icons/24x24/ok.png" />
              OK
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
