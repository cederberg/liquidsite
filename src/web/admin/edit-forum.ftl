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
              <option value="${item.id}" selected="selected">${item.name?html}</option>
    <#else>
              <option value="${item.id}">${item.name?html}</option>
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
            Real Name:
          </th>
          <td class="field">
            <input type="text" tabindex="3" size="30"
                   name="realname" value="${realname?html}" />
            <p>The real forum nane is the full name of the forum,
            complete with correct casing, spacing and similar. It can
            be used to present the forum in lists of several forums.</p>
          </td>
        </tr>
        <tr>
          <th>
            Description:
          </th>
          <td class="field">
            <textarea tabindex="4" rows="6" cols="60"
                      name="description">${description?html}</textarea>
            <p>The forum description contains a longer text describing
            the topics discussed in the forum.</p>
          </td>
        </tr>
        <tr>
          <th>
            Moderators:
          </th>
          <td class="field">
            <select tabindex="5" name="moderator">
              <option value="">&lt; None &gt;</option>
<#list moderators as item>
  <#if moderator == item.name>
              <option value="${item.name}" selected="selected">${item.name?html}</option>
  <#else>
              <option value="${item.name}">${item.name?html}</option>
  </#if>
</#list>
            </select>
            <p>The moderator group has access to manipulating all
            posts and threads in the forum via the form interface.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="6" size="40"
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
