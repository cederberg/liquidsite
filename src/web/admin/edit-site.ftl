<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("name").item(0).focus();
    }
    </script>

    <form method="post">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="step" value="1" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/edit.png" alt="Edit" />
          </td>
          <td colspan="2">
            <h2>Edit Site Details (Step 1 of 1)</h2>

            <p>Edit the details of the site.</p>
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
            <input type="text" name="name" value="${name}" size="30" />
            <p>The site name is used to present the site in the admin
            application.</p>
          </td>
        </tr>
        <tr>
          <th>
            Protocol:
          </th>
          <td class="field">
            <select name="protocol">
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
            <select name="host">
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
            <input type="text" name="port" value="${port}" size="10" />
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
            <input type="text" name="dir" value="${dir}" size="40" />
            <p>The site base directory. If several sites are run on 
            the same protocol, host and port, the base directory must
            be used to distinguish between them.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
