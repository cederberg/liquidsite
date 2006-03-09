<#assign title>Statistics</#assign>
<#include "header.ftl">

    <table class="dialog">
      <tr>
        <td class="decoration" rowspan="4">
          <img src="images/icons/48x48/statistics.png" alt="Statistics" />
        </td>
          <td colspan="2">
            <h2>Statistics for ${domain}</h2>
          </td>
        </tr>
        <tr>
          <td>
            <p>Statistical summary for the domain ${domain}.<br/>
            Generated on
            ${liquidsite.util.currentTime?string("yyyy-MM-dd HH:mm")}.</p>

            <p><strong>Visitor Statistics:</strong>
<#if accessStatsUrl?has_content>
            <a href="${accessStatsUrl}">Available</a>
<#else>
            Unavailable
</#if>
            </p>

            <table class="border">
              <tr>
                <th>Type</th>
                <th>Objects</th>
                <th>Size</th>
              </tr>
              <tr>
                <td>Site Structure</th>
                <td class="number">${siteCount}</td>
                <td>${siteSize}</td>
              </tr>
              <tr>
                <td>Pages &amp; Templates</th>
                <td class="number">${pageCount}</td>
                <td>${pageSize}</td>
              </tr>
              <tr>
                <td>Files</th>
                <td class="number">${fileCount}</td>
                <td>${fileSize}</td>
              </tr>
              <tr>
                <td>Documents</th>
                <td class="number">${docCount}</td>
                <td>${docSize}</td>
              </tr>
              <tr>
                <td>Forums</th>
                <td class="number">${forumCount}</td>
                <td>${forumSize}</td>
              </tr>
              <tr>
                <th>Total</th>
                <th class="number">${domainCount}</th>
                <th>${domainSize}</th>
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
