<#assign title = "Liquid Site Document Preview">
<#assign hideadmin = true>
<#include "header.ftl">

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

<#include "footer.ftl">
