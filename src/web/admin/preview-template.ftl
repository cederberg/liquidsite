<#assign title = "Liquid Site Template Preview">
<#assign hidefooter = true>
<#include "../header.ftl">
<@menu />

    <table class="border">
<#list locals.keySet() as elem>
      <tr>
        <th>
          ${elem?html}:
        </th>
        <td>
          <pre>${locals[elem]?html}</pre>
        </td>
      </tr>
</#list>
<#list inherited.keySet() as elem>
      <tr class="inherited">
        <th>
          ${elem?html}:
        </th>
        <td>
          <pre>${inherited[elem]?html}</pre>
        </td>
      </tr>
</#list>
    </table>

<#include "../footer.ftl">
