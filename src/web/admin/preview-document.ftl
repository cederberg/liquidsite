<#assign title = "Liquid Site Document Preview">
<#assign hidefooter = true>
<#include "../header.ftl">
<@menu />

    <table class="border">
<#list properties as prop>
      <tr>
        <th>
          ${prop.name}:
        </th>
        <td>
          ${liquidsite.doc.data[prop.id]}
        </td>
      </tr>
</#list>
    </table>

<#include "../footer.ftl">
