<#assign isadd = liquidsite.request.path?ends_with("add-content.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("title").focus();
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
      <input type="hidden" name="category" value="topic" />
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
            <h2>Enter Topic Details (Step 2 of 2)</h2>

            <p>Enter the details of the topic you wish to add.</p>
<#else>
            <h2>Enter Topic Details (Step 1 of 1)</h2>

            <p>Edit the details of the topic.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Subject:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="60"
                   name="subject" value="${subject}" />
            <p>The topic subject is normally the same as the subject
            of the first post.</p>
          </td>
        </tr>
<#if isadd>
        <tr>
          <th>
            Post:
          </th>
          <td class="field">
            <textarea tabindex="2" rows="20" cols="60"
                      name="post">${post?xml}</textarea>
            <p>The text of the first post to add to the topic.</p>
          </td>
        </tr>
<#else>
        <tr>
          <th>
            Parent&nbsp;Forum:
          </th>
          <td class="field">
            <select tabindex="3" name="forum">
  <#list forums as item>
    <#if forum == item.id>
              <option value="${item.id}" selected="selected">${item.name?xml}</option>
    <#else>
              <option value="${item.id}">${item.name?xml}</option>
    </#if>
  </#list>
            </select>
            <p>The parent forum controls the location of this topic in
            the content tree.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Locked:
          </th>
          <td class="field">
<#if locked = "true">
            <input type="checkbox" tabindex="4" checked="checked"
                   name="locked" value="true" />
<#else>
            <input type="checkbox" tabindex="4"
                   name="locked" value="true" />
</#if>
            <p>The topic locked flag. If this flag is set it may not
            be possible for ordinary users to post to this topic.</p>
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
