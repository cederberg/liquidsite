<#assign isadd = liquidsite.request.path?ends_with("add-users.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
<#if isadd>
        utilGetElement("name").focus();
<#else>
        utilGetElement("enabled").focus();
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
      <input type="hidden" name="type" value="user" />
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
            <h2>Enter User Details (Step 1 of 1)</h2>

<#if isadd>
            <p>Enter the details of the user you wish to add.</p>
<#else>
            <p>Edit the details of the user.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Login:
          </th>
          <td class="field">
<#if isadd>
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name?html}" />
<#else>
            <input type="hidden" name="name" value="${name?html}" />
            <strong>${name?html}</strong>
</#if>
            <p>The login name uniquely identifies the user in the
            domain. It cannot be modified once the user has been
            created.</p>
          </td>
        </tr>
        <tr>
          <th>
            Enabled:
          </th>
          <td class="field">
<#if enabled = "true">
            <input type="checkbox" tabindex="2" checked="checked"
<#else>
            <input type="checkbox" tabindex="2"
</#if>
                   name="enabled" value="true" />
            <p>The enabled flag. If the user is not enabled, no logins
            will be permitted by this user.</p>
          </td>
        </tr>
        <tr>
          <th>
            Password:
          </th>
          <td class="field">
            <input type="password" tabindex="3" size="30"
                   name="password" value="${password?html}" />
            &nbsp;&nbsp;&nbsp;&nbsp;
            Suggestion: <strong>${passwordSuggestion?html}</strong>
<#if isadd>
            <p>The user password. This can later be modified by the
            user.</p>
<#else>
            <p>The new user password. Leave this field blank to keep
            the current user password.</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td class="field">
            <input type="text" tabindex="4" size="30"
                   name="realname" value="${realname?html}" />
            <p>The user name to be presented on the site. This field
            can be modified by the user.</p>
          </td>
        </tr>
        <tr>
          <th>
            Email:
          </th>
          <td class="field">
            <input type="text" tabindex="5" size="30"
                   name="email" value="${email?html}" />
            <p>The user email address. This field can be modified by
            the user.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="6" size="30"
                   name="comment" value="${comment?html}" />
            <p>The user comment. This annotation area is only visible
            for the domain administrators and isn't shown for the
            user.</p>
          </td>
        </tr>
        <tr>
          <th>
            Groups:
          </th>
          <td class="field">
<#list groups as group>
  <#if memberships.containsKey(group.name)>
            <input type="checkbox" tabindex="7" checked="checked"
                   name="member${group_index}" value="${group.name?html}" />
  <#else>
            <input type="checkbox" tabindex="7"
                   name="member${group_index}" value="${group.name?html}" />
  </#if>
            <strong>${group.name?html}</strong>
  <#if group.description?has_content>
            &ndash; ${group.description?html}
  </#if>
            <br />
</#list>
            <p>The user group memberships.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="11" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="10">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
