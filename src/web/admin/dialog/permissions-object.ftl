<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/permission.js"></script>

    <script type="text/javascript">
    function initialize() {
        permissionInitialize('permtable');
<#list groups as group>
        permissionAddGroup('${group.name}');
</#list>
<#list inherited as perm>
  <#if perm.user?has_content>
    <#assign user = "'" + perm.user + "'">
  <#else>
    <#assign user = "null">
  </#if>
  <#if perm.group?has_content>
    <#assign group = "'" + perm.group + "'">
  <#else>
    <#assign group = "null">
  </#if>
        permissionAddInherited(${user}, ${group}, ${perm.read}, ${perm.write}, ${perm.publish}, ${perm.admin});
</#list>
<#list local as perm>
  <#if perm.user?has_content>
    <#assign user = "'" + perm.user + "'">
  <#else>
    <#assign user = "null">
  </#if>
  <#if perm.group?has_content>
    <#assign group = "'" + perm.group + "'">
  <#else>
    <#assign group = "null">
  </#if>
        permissionAddLocal(${user}, ${group}, ${perm.read}, ${perm.write}, ${perm.publish}, ${perm.admin});
</#list>
        permissionDisplay();
    }

    function doPrevious() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <table class="dialog">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/permissions.png" alt="Permissions" />
          </td>
          <td colspan="2">
            <h2>${type?cap_first} Permissions</h2>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Inherit:
          </th>
          <td>
<#if (local?size > 0)>
            <input type="checkbox" tabindex="1"
<#else>
            <input type="checkbox" tabindex="1" checked="checked"
</#if>
                   name="inherit" value="true"
                   onclick="permissionToggleEdit();" />
            Inherit parent object permissions.
          </td>
        </tr>
        <tr>
          <td colspan="2">
            <table id="permtable" class="border"></table>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="101" onclick="doPrevious();">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="100">
              <img src="images/icons/24x24/save.png" />
              Set
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
