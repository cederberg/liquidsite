<#assign title = "Liquid Site Section Preview">
<#assign hidefooter = true>
<#include "../header.ftl">
<@menu />

    <h2>Section Description</h2>

    <p>${description?html}</p>


    <h2>Section Properties</h2>

    <p>The properties for documents in this section.</p>

    <table class="border">
      <tr>
        <th>Identifier</th>
        <th>Name</th>
        <th>Type</th>
        <th>Description</th>
      </tr>
<#list properties as prop>
      <tr>
        <td>
          ${prop.id}
        </td>
        <td>
          ${prop.name}
        </td>
        <td>
  <#if prop.type == 1>
          Plain Text
  <#elseif prop.type == 2>
          Formatted Text
  <#else>
          Formatted Text (HTML)
  </#if>
        </td>
        <td>
          ${prop.description}
        </td>
      </tr>
</#list>
    </table>

<#include "../footer.ftl">
