<#include "header.ftl">

    <script type="text/javascript">
    function refresh() {
        document.getElementsByName("page").item(0).value = "1";
        document.forms.item(0).submit();
    }
    
    function changePage(page) {
        document.getElementsByName("page").item(0).value = page;
        document.getElementsByName("filter").item(0).value = "${filter}";
        document.forms.item(0).submit();
    }
    </script>


    <form method="post" accept-charset="UTF-8" 
          onsubmit="refresh(); return false;">
      <table class="form">
        <tr>
          <th>
            Type:<br/>
            <select name="type" onchange="refresh()">
              <option value="user">Users</option>
<#if groups?exists>
              <option value="group" selected="selected">Groups</option>
<#else>
              <option value="group">Groups</option>
</#if>
            </select>
          </th>
<#if enableDomains>
          <th>
            Domain:<br/>
            <select name="domain" onchange="refresh()">
              <option>&lt;None&gt;</option>
  <#list domains as item>
    <#if domain = item>
              <option selected="selected">${item}</option>
    <#else>
              <option>${item}</option>
    </#if>
  </#list>
            </select>
          </th>
</#if>
          <th>
            Filter:<br/>
            <input name="filter" value="${filter?xml}" />
          </th>
          <th>
            <button type="submit">
              <img src="images/icons/24x24/refresh.png" />
              Refresh
            </button>
          </th>
        </tr>
      </table>
      <input type="hidden" name="page" value="${page}" />
    </form>

    <div style="margin-left: 10%">
      <p><strong>Page:</strong>
<#if (page != 1) && (pages > 1)>
      <a href="#" onclick="changePage(${page-1}); return false;">&lt;&lt;</a>
<#else>
      &lt;&lt;
</#if>
<#list 1..pages as item>
  <#if item = page>
      <strong>${item}</strong>
  <#else>
      <a href="#" onclick="changePage(${item}); return false;">${item}</a>
  </#if>
</#list>
<#if (page != pages) && (pages > 1)>
      <a href="#" onclick="changePage(${page+1}); return false;">&gt;&gt;</a>
<#else>
      &gt;&gt;
</#if>
      </p>


      <table class="border">
<#if users?exists>
        <tr>
          <th>User</th>
          <th>Name</th>
          <th>Email</th>
          <th>Comment</th>
          <th>&nbsp;</th>
        </tr>
  <#if users?size = 0>
        <tr>
          <td colspan="5">No users found</td>
        </tr>
  </#if>
  <#list users as user>
        <tr>
          <td>${user.name?xml}</td>
          <td>${user.realName?xml}</td>
          <td>${user.email?xml}</td>
          <td>${user.comment?xml}</td>
          <td>
            <img src="images/icons/24x24/delete.png"  alt="Delete" />
          </td>
        </tr>
  </#list>
<#else>
        <tr>
          <th>Group</th>
          <th>Description</th>
          <th>Comment</th>
          <th>&nbsp;</th>
        </tr>
  <#if groups?size = 0>
        <tr>
          <td colspan="4">No groups found</td>
        </tr>
  </#if>
  <#list groups as group>
        <tr>
          <td>${group.name?xml}</td>
          <td>${group.description?xml}</td>
          <td>${group.comment?xml}</td>
          <td>
            <img src="images/icons/24x24/delete.png"  alt="Delete" />
          </td>
        </tr>
  </#list>
</#if>
      </table>


      <p>
        <button type="button">
          <img src="images/icons/24x24/add.png" />
          Add New
        </button>
      </p>

    </div>

<#include "footer.ftl">
