<#assign isadd = liquidsite.page.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/template.js"></script>

    <script type="text/javascript">
    function initialize() {
        templateInitialize("elementedit");
<#list locals.keySet() as elem>
        templateAddLocal('${elem}', ${locals[elem]});
</#list>
        templateDisplay();
        openTemplate(${parent});
        document.getElementsByName("name").item(0).focus();
    }

    function openTemplate(id) {
        var script = document.createElement('script');

        script.type = "text/javascript";
        script.src = "opentemplate.js?type=template&id=" + id;
        document.getElementsByTagName("head")[0].appendChild(script);
    }

    function previous() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
        document.forms.item(0).submit();
    }

    function ieDisableEnterSubmit() {
        var  src;
        var  name = "";

        if (window.event) {
            src = window.event.srcElement
            if (src != undefined) {
                name = src.tagName.toLowerCase();
            }
            if (window.event.keyCode == 13 && name == "input") {
                window.event.keyCode = 0;
            }
        }
    }
    </script>

    <form method="post" accept-charset="UTF-8"
          onkeypress="ieDisableEnterSubmit()">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="template" />
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
            <h2>Enter Template Details (Step 2 of 2)</h2>

            <p>Enter the details of the template you wish to add.</p>
<#else>
            <h2>Enter Template Details (Step 1 of 1)</h2>

            <p>Edit the details of the template.</p>
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
            <p>The template name is used to identify the template 
            when creating web pages. The template name should use
            only English alphabet characters or numbers without any 
            spaces.</p>
          </td>
        </tr>
<#if !isadd>
        <tr>
          <th>
            Base&nbsp;Template:
          </th>
          <td class="field">
            <select tabindex="2" onchange="openTemplate(this.value)"
                    name="parent">
              <option value="0">&lt; None &gt;</option>
  <#list templateIds as id>
    <#if parent == id>
              <option value="${id}" selected="selected">${templateNames[id]?xml}</option>
    <#else>
              <option value="${id}">${templateNames[id]?xml}</option>
    </#if>
  </#list>
            </select>
            <p>The base template provides a set of inherited page 
            elements. Note that changing base template will modify
            the inherited page elements below.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Elements:
          </th>
          <td class="field">
            The page elements contain the HTML code for the web page.
            The <strong>root</strong> element is the base from which 
            other elements can be included with the 
            <code>&lt;#include&nbsp;"<em>name</em>"&gt;</code> syntax.
          </td>
        </tr>
        <tr>
          <td class="field" colspan="2">
            <table id="elementedit" class="border"></table>
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
