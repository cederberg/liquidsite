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
          <h2>${heading}</h2>

          <p>${text?cap_first}</p>

          <p>Try going to the
          <a href="${liquidsite.linkTo("/index.html")}">site start
          page</a>.</p>
        </td>
      </tr>
    </table>

<#include "footer.ftl">
