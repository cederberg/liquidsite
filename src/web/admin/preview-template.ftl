<#assign title = "Liquid Site Template Preview">
<#assign hideadmin = true>
<#include "header.ftl">

    <table class="border">
<#list locals.keySet() as elem>
      <tr>
        <th>
          ${elem}:
        </th>
        <td>
          <pre>${locals[elem]?html}</pre>
        </td>
      </tr>
</#list>
<#list inherited.keySet() as elem>
      <tr class="inherited">
        <th>
          ${elem}:
        </th>
        <td>
          <pre>${inherited[elem]?html}</pre>
        </td>
      </tr>
</#list>
    </table>

<#include "footer.ftl">
