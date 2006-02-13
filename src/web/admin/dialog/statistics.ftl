<#assign title>Statistics</#assign>
<#include "header.ftl">

    <table class="dialog">
      <tr>
        <td class="decoration" rowspan="4">
          <img src="images/icons/48x48/info.png" alt="Statistics" />
        </td>
          <td colspan="2">
            <h2>Statistics for ${domain}</h2>
          </td>
        </tr>
        <tr>
          <td>
            <p>This is a statistical summary for the domain ${domain}.<br/>
            Generated on
            ${liquidsite.util.currentTime?string("yyyy-MM-dd HH:mm")}.</p>
            
            <table class="border">
              <tr>
                <th>Size:</th>
                <td>${domainsize}</td>
              </tr>
              <tr>
                <th>Visitor Stats:</th>
                <td>
<#if accessStatsUrl?has_content>
                  <a href="${accessStatsUrl}">Available</a>
<#else>
                  Unavailable
</#if>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        <tr>
          <td class="buttons">
            <button onclick="window.close();">
              <img src="images/icons/24x24/ok.png" />
              OK
            </button>
          </td>
        </tr>
      </table>

<#include "footer.ftl">
