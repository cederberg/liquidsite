<#include "header.ftl">

    <div style="margin-left: 10%">

      <h1>Group Members</h1>

      <table class="compact">
        <tr>
          <th>Domain:</th>
          <td>${domain?html}</td>
        </tr>
        <tr>
          <th>Group:</th>
          <td>${name?html}</td>
        </tr>
      </table>

      <p><strong>Page:</strong>
<#macro linkPage page>
  <#assign params = "type=group&domain=" + domain + "&name=" + name>
      <a href="view-users.html?${params}&page=${page}"><#nested></a>
</#macro>
<#if (page != 1) && (pages > 1)>
      <@linkPage 1>&lt;&lt;</@linkPage>
<#else>
      &lt;&lt;
</#if>
<#list 1..pages as item>
  <#if item = page>
      <strong>${item}</strong>
  <#else>
      <@linkPage item>${item}</@linkPage>
  </#if>
</#list>
<#if (page != pages) && (pages > 1)>
      <@linkPage page+1>&gt;&gt;</@linkPage>
<#else>
      &gt;&gt;
</#if>
      </p>


      <table class="border">
        <tr>
          <th>User</th>
          <th>Name</th>
          <th>Email</th>
          <th>Comment</th>
        </tr>
<#if users?size = 0>
        <tr>
          <td colspan="4">No users in the group</td>
        </tr>
</#if>
<#list users as user>
  <#assign params = "type=user&domain=" + domain + "&name=" + user.name>
        <tr>
          <td><a href="edit-users.html?${params}">${user.name?html}</a></td>
          <td>${user.realName?html}</td>
          <td>${user.email?html}</td>
          <td>${user.comment?html}</td>
        </tr>
</#list>
      </table>

    </div>

<#include "footer.ftl">
