<#assign isadd = liquidsite.request.path?ends_with("add-content.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/property.js"></script>
    <script type="text/javascript">
    function initialize() {
        propertyInitialize("propertyedit");
<#list properties as prop>
        propertyAdd('${prop.id}', 
                    '${prop.name?replace("'","\\'")}', 
                    ${prop.type}, 
                    '${prop.description?replace("'","\\'")}');
</#list>
        propertyDisplay();
        utilFocusElement("name");
        utilSessionKeepAlive();
    }

    function previous() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8"
          onkeypress="utilDisableEnterSubmitForIE()">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="section" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
<#if isadd>
            <img src="images/icons/48x48/edit.png" alt="Add" />
<#else>
            <img src="images/icons/48x48/edit.png" alt="Edit" />
</#if>
          </td>
          <td colspan="2">
<#if isadd>
            <h2>Enter Section Details (Step 2 of 2)</h2>

            <p>Enter the details of the section you wish to add.</p>
<#else>
            <h2>Enter Section Details (Step 1 of 1)</h2>

            <p>Edit the details of the section.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
            <p>The section name is used to identify the section
            when listing or searching for documents. The name may 
            only contain English alphabet characters or numbers 
            without any spaces.</p>
          </td>
        </tr>
<#if !isadd>
        <tr>
          <th>
            Parent&nbsp;Section:
          </th>
          <td class="field">
            <select tabindex="2" name="parent">
              <option value="0">&lt; None &gt;</option>
  <#list parents as item>
    <#if parent == item.id>
              <option value="${item.id}" selected="selected">${item.name?xml}</option>
    <#else>
              <option value="${item.id}">${item.name?xml}</option>
    </#if>
  </#list>
            </select>
            <p>The parent section controls the location of this 
            section in the content tree.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Properties:
          </th>
          <td class="field">
            The document properties define the fields available in
            the documents in this section. If no properties are 
            specified here, the parent section properties will be
            inherited.
          </td>
        </tr>
        <tr>
          <td class="field" colspan="2">
            <table id="propertyedit" class="border"></table>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="100" size="40"
                   name="comment" value="${comment}" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="submit" style="display: none;" 
                    onclick="return false;">
              Mozilla Disable Enter Submit
            </button>
            <button type="button" tabindex="102" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="101">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
