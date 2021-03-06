<#assign isadd = liquidsite.request.path?ends_with("add-users.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
<#if isadd>
        utilGetElement("name").focus();
<#else>
        utilGetElement("description").focus();
</#if>
        utilSessionKeepAlive();
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="group" />
      <input type="hidden" name="domain" value="${domain}" />
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
            <h2>Enter Group Details (Step 1 of 1)</h2>

<#if isadd>
            <p>Enter the details of the group you wish to add.</p>
<#else>
            <p>Edit the details of the group.</p>
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
<#if isadd>
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name?html}" />
<#else>
            <input type="hidden" name="name" value="${name?html}" />
            <strong>${name?html}</strong>
</#if>
            <p>The group name uniquely identifies the group in the
            domain. It cannot be modified once the group has been
            created.</p>
          </td>
        </tr>
        <tr>
          <th>
            Description:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="30"
                   name="description" value="${description?html}" />
            <p>The group description to be presented on the site.</p>
          </td>
        </tr>
        <tr>
          <th>
            Public:
          </th>
          <td class="field">
<#if public = "true">
            <input type="checkbox" tabindex="3" checked="checked"
<#else>
            <input type="checkbox" tabindex="3"
</#if>
                   name="public" value="true" />
            <p>The the public group flag. A public group allows users
            to add or remove themselves to or from the group. The
            public groups should not be security related, but only
            used for other things such as mailing list membership and
            similar.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="4" size="30"
                   name="comment" value="${comment?html}" />
            <p>The group comment. This annotation area is only visible
            for the domain administrators and isn't shown for users.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="9" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="8">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
