<#assign isadd = liquidsite.request.path?ends_with("add-content.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/htmledit.js"></script>
    <script type="text/javascript" src="script/tagedit.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("name").focus();
        utilSessionKeepAlive();
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }

    function doSave() {
        utilGetElement("publish").value = "false";
        return false;
    }

    function doPublish() {
        utilGetElement("publish").value = "true";
        return false;
    }
    </script>

    <form method="post" accept-charset="UTF-8" onsubmit="htmlEditSubmit()">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="document" />
      <input type="hidden" name="publish" value="false" />
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
            <h2>Enter Document Details (Step 2 of 2)</h2>

            <p>Enter the details of the document you wish to add.</p>
<#else>
            <h2>Enter Document Details (Step 1 of 1)</h2>

            <p>Edit the details of the document.</p>
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
            <p>The document name is used to identify the document.
            As the name may form part of a URL it may only contain
            English alphabet characters or numbers without any
            spaces.</p>
          </td>
        </tr>
<#if !isadd>
        <tr>
          <th>
            Section:
          </th>
          <td class="field">
            <select tabindex="2" name="section">
  <#list sections as item>
    <#if section == item.id>
              <option value="${item.id}" selected="selected">${item.name?xml}</option>
    <#else>
              <option value="${item.id}">${item.name?xml}</option>
    </#if>
  </#list>
            </select>
            <p>The section controls the location and availability of
            of this document.</p>
          </td>
        </tr>
</#if>
<#list properties as prop>
        <tr>
          <th>
            ${prop.name}:
          </th>
          <td class="field">
          <input type="hidden"
                 name="propertytype.${prop.id}" value="${prop.type}" />
  <#if prop.type == 1>
            ${prop.description}<br/><br/>
            <textarea tabindex="10" rows="6" cols="60"
                      name="property.${prop.id}">${data[prop.id]?xml}</textarea>
  <#elseif prop.type == 2>
            ${prop.description}<br/><br/>
            <div id="property.${prop.id}.toolbar"></div>
            <textarea id="property.${prop.id}.editor"
                      tabindex="10" rows="25" cols="60"
                      name="property.${prop.id}">${data[prop.id]?xml}</textarea>
            <script type="text/javascript">
            tagEditInitialize('property.${prop.id}');
            </script>
  <#elseif prop.type == 3>
            ${prop.description}<br/><br/>
            <div id="property.${prop.id}.toolbar"></div>
            <div id="property.${prop.id}.editor"></div>
            <script type="text/javascript">
            htmlEditInitialize('property.${prop.id}', ${data[prop.id]}, 10);
            </script>
  </#if>
          </td>
        </tr>
</#list>
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
            <button type="button" tabindex="103" onclick="doPrevious()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="102" onclick="doSave()">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
<#if publish = "true">
            <button type="submit" tabindex="101" onclick="doPublish()">
              <img src="images/icons/24x24/online.png" />
              Publish
            </button>
</#if>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
