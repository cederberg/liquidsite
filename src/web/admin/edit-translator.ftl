<#assign isadd = liquidsite.request.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>

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
        utilGetElement("action").value = "save";
        return true;
    }

    function doPublish() {
        utilGetElement("action").value = "publish";
        return true;
    }
    </script>

    <form method="post" accept-charset="UTF-8">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="action" value="save" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="translator" />
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
            <h2>Enter Translator Details (Step 2 of 2)</h2>

            <p>Enter the details of the translator you wish to add.</p>
<#else>
            <h2>Enter Translator Details (Step 1 of 1)</h2>

            <p>Edit the details of the translator.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name?html}" />
            <p>The translator name is only used to identify the
            translator within the site. The translator name should use
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
            <p>The parent folder controls the location of the
            translator. Note that changing folder will also modify the
            translated URLs.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Section:
          </th>
          <td class="field">
            <select tabindex="4" name="section">
  <#list sections as item>
    <#if section == item.id>
              <option value="${item.id}" selected="selected">${item.name?html}</option>
    <#else>
              <option value="${item.id}">${item.name?html}</option>
    </#if>
  </#list>
            </select>
            <p>The section field links this translator to the contents
            of the specified section. This will translate URL:s
            containing subsection and document names by adding them to
            the request environment. The <code>index.html</code> page
            in the translator can then present the content through the
            <code>liquidsite.section</code> and
            <code>liquidsite.doc</code> variables.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="100" size="40"
                   name="comment" value="${comment?html}" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
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
