<#assign isadd = liquidsite.request.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/template.js"></script>

    <script type="text/javascript">
    function initialize() {
        templateInitialize("elementedit");
<#list locals.keySet() as elem>
        templateAddLocal('${elem}', ${locals[elem]});
</#list>
        templateDisplay();
        doOpenTemplate(${template});
        utilGetElement("name").focus();
        utilSessionKeepAlive();
    }

    function doOpenTemplate(id) {
        var script = document.createElement('script');

        script.type = "text/javascript";
        script.src = "opentemplate.js?type=template&id=" + id;
        document.getElementsByTagName("head")[0].appendChild(script);
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }

    function doSave() {
        utilGetElement("action").value = "save";
        return true;
    }

    function doPublish() {
        utilGetElement("action").value = "publish";
        return true;
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
      <input type="hidden" name="action" value="save" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="page" />
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
            <h2>Enter Page Details (Step 2 of 2)</h2>

            <p>Enter the details of the page you wish to add.</p>
<#else>
            <h2>Enter Page Details (Step 1 of 1)</h2>

            <p>Edit the details of the page.</p>
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
            <p>The page name is part of the URL by which the user
            will access the page contents. The page name should use
            only English alphabet characters or numbers without any
            spaces.</p>
          </td>
        </tr>
<#if !isadd>
        <tr>
          <th>
            Folder:
          </th>
          <td class="field">
            <select tabindex="2" name="parent">
  <#list folders as item>
    <#if parent == item.id>
              <option value="${item.id}" selected="selected">${item.name?html}</option>
    <#else>
              <option value="${item.id}">${item.name?html}</option>
    </#if>
  </#list>
            </select>
            <p>The parent folder controls the location of the page.
            Note that changing folder will also modify the page
            URL.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Template:
          </th>
          <td class="field">
            <select tabindex="3" onchange="doOpenTemplate(this.value)"
                    name="template">
              <option value="0">&lt; None &gt;</option>
<#list templates as item>
  <#if template == item.id>
              <option value="${item.id}" selected="selected">${item.name?html}</option>
  <#else>
              <option value="${item.id}">${item.name?html}</option>
  </#if>
</#list>
            </select>
            <p>The template provides a set of inherited page
            elements. Note that changing base template will modify
            the inherited page elements below.</p>
          </td>
        </tr>
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
            <button type="button" tabindex="103" onclick="doPrevious()">
<#if isadd>
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
<#else>
              <img src="images/icons/24x24/cancel.png" />
              Cancel
</#if>
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
