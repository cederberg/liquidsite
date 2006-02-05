<#assign title = "Liquid Site Error">
<#include "header.ftl">

<#macro entries>
  <@menuentry "Error" />
</#macro>

<@menu entries />

    <table class="form">
      <tr>
        <td class="decoration">
          <img src="${liquidsite.linkTo("/liquidsite/system/images/icons/48x48/error.png")}"
               alt="Error" />
        </td>
        <td>
          <h2>${heading?html}</h2>

          <p>${text?cap_first?html}</p>

<#if detail?has_content>
          <div class="example">
            <p><code>${detail?html}</code></p>
          </div>
</#if>

          <p>Try going to the
          <a href="${liquidsite.linkTo("/index.html")}">site start
          page</a>.</p>
        </td>
      </tr>
    </table>

<#include "footer.ftl">
