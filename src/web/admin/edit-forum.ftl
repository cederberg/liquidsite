<#assign isadd = liquidsite.request.path?ends_with("add-content.html")>
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
<#if startpage?has_content>
      <input type="hidden" name="liquidsite.startpage" value="${startpage}" />
</#if>
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="action" value="save" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="forum" />
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
            <h2>Enter Forum Details (Step 2 of 2)</h2>

            <p>Enter the details of the forum you wish to add.</p>
<#else>
            <h2>Enter Forum Details (Step 1 of 1)</h2>

            <p>Edit the details of the forum.</p>
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
            <p>The forum name is used to identify the forum as part of
            a URL. The name may only contain English alphabet
            characters or numbers without any spaces.</p>
          </td>
        </tr>
<#if !isadd>
        <tr>
          <th>
            Parent&nbsp;Section:
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
            <p>The parent section controls the location of this
            forum in the content tree.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Title:
          </th>
          <td class="field">
            <input type="text" tabindex="3" size="30"
                   name="title" value="${title}" />
            <p>The forum title contains the full name of the forum,
            complete with spacing and similar. It can be used to
            present the forum in lists of several forums.</p>
          </td>
        </tr>
        <tr>
          <th>
            Description:
          </th>
          <td class="field">
            <textarea tabindex="4" rows="6" cols="60"
                      name="description">${description?xml}</textarea>
            <p>The forum description contains a longer text describing
            the topics discussed in the forum.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="5" size="40"
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
