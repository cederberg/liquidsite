<#assign title = "Liquid Site Administration">
<#include "../header.ftl">

<#macro entries>
  <#if liquidsite.user.login?has_content>
    <@menuentry "Home", "home.html", true />
    <@menuentry "Site", "site.html" />
    <@menuentry "Content", "content.html" />
    <#if liquidsite.user.domainadmin>
      <@menuentry "Users", "users.html" />
    </#if>
    <#if liquidsite.user.superuser>
      <@menuentry "System", "system.html" />
    </#if>
  <#else>
    <@menuentry "Login" />
  </#if>
</#macro>

<#macro info>
          Version&nbsp;${liquidsite.version}<br />
          ${liquidsite.date}<br />
          <br />
  <#if liquidsite.user.login?has_content>
          <a href="logout.html">Logout</a>
  <#else>
          &nbsp;
  </#if>
</#macro>

<@menu entries, info />
