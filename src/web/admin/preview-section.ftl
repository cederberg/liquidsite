<#assign title = "Liquid Site Section Preview">
<#assign hideadmin = true>
<#include "header.ftl">

    <h2>Document Properties</h2>
    
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
          Single-line Text
  <#elseif prop.type == 2>
          Multi-line Text
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

<#include "footer.ftl">
