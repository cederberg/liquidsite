<#assign isadd = liquidsite.page.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("name").item(0).focus();
    }

    function previous() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="site" />
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
            <h2>Enter Site Details (Step 2 of 2)</h2>

            <p>Enter the details of the site you wish to add.</p>
<#else>
            <h2>Edit Site Details (Step 1 of 1)</h2>

            <p>Edit the details of the site.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Site&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
            <p>The site name is used to present the site in the admin
            application.</p>
          </td>
        </tr>
        <tr>
          <th>
            Protocol:
          </th>
          <td class="field">
            <select name="protocol" tabindex="2">
              <option value="http">HTTP</option>
<#if protocol?has_content && protocol = "https">
              <option value="https" selected="selected">Secure HTTP</option>
<#else>
              <option value="https">Secure HTTP</option>
</#if>
            </select>
            <p>The protocol to accept for the site. Note that using
            secure HTTP, requires certificates to be installed in 
            order to work.</p>
          </td>
        </tr>
        <tr>
          <th>
            Host&nbsp;Name:
          </th>
          <td class="field">
            <select name="host" tabindex="3">
              <option value="*">&lt;Any Host&gt;</option>
<#list hostnames as hostname>
  <#if host?has_content && hostname = host>
              <option value="${hostname}" selected="selected">${hostname}</option>
  <#else>
              <option value="${hostname}">${hostname}</option>
  </#if>
</#list>
            </select>
            <p>The site host name. The host name must be registered
            with the domain and in DNS.</p>
          </td>
        </tr>
        <tr>
          <th>
            Port&nbsp;Number:
          </th>
          <td class="field">
            <input type="text" tabindex="4" size="10"
                   name="port" value="${port}" />
            <p>The site port number. The web server must be 
            configured for listening on this port. Normal HTTP 
            traffic uses port 80, and secure HTTP uses port 443.
            Use 0 to specify any port number.</p>
          </td>
        </tr>
        <tr>
          <th>
            Directory:
          </th>
          <td class="field">
            <input type="text" tabindex="5" size="40"
                   name="dir" value="${dir}" />
            <p>The site base directory. If several sites are run on 
            the same protocol, host and port, the base directory must
            be used to distinguish between them.</p>
          </td>
        </tr>
<#if isadd>
        <tr>
          <th>
            Special&nbsp;Sites:
          </th>
          <td class="field">
            <input type="checkbox" tabindex="6"
                   name="admin" value="true" /> 
            <strong>Administration Site</strong>
            <p>The special site flags. Note that these flags cannot
            be changed after creation, as they determine the site
            type.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="7" size="40"
                   name="comment" value="${comment}" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="9" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
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
