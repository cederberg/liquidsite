<#assign isadd = liquidsite.request.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/host.js"></script>
    <script type="text/javascript">
    function initialize() {
        hostInitialize("hosts.table");
<#list hosts as host>
        hostAdd(${host.name}, ${host.description});
</#list>
        hostDisplay();
<#if isadd>
        utilGetElement("name").focus();
<#else>
        utilGetElement("description").focus();
</#if>
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
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
      <input type="hidden" name="category" value="domain" />
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
            <h2>Enter Domain Details (Step 2 of 2)</h2>

            <p>Enter the details of the domain you wish to add.</p>
<#else>
            <h2>Edit Domain Details (Step 1 of 1)</h2>

            <p>Edit the details of the domain.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Domain&nbsp;Name:
          </th>
          <td class="field">
<#if isadd>
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
<#else>
            <input type="hidden" name="name" value="${name}" />
            <strong>${name}</strong><br/>
</#if>
            <p>The domain name uniquely identifies the domain in the
            database. The domain name cannot be changed, and is
            normally a short UPPERCASE word.</p>
          </td>
        </tr>
        <tr>
          <th>
            Description:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="50"
                   name="description" value="${description?html}" />
            <p>The description of the domain. This description is
            only visible in the administration application.</p>
          </td>
        </tr>
        <tr>
          <th>
            Mail Address:
          </th>
          <td class="field">
            <input type="text" tabindex="3" size="50"
                   name="mailaddress" value="${mailaddress?html}" />
            <p>The mail sender address to use for the domain. This address
            is used on all outgoing emails. To include both a sender name
            and address, use the format
            "<code>Name &lt;email@domain&gt;</code>".</p>
          </td>
        </tr>
        <tr>
          <th>
            Host&nbsp;Names:
          </th>
          <td class="field">
            The web host names connected to the domain. All domains
            but the ROOT domain must have at least one web host
            name.<br/>
            <br/>
            <table class="border" id="hosts.table">
            </table>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="101" onclick="doPrevious()">
<#if isadd>
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
<#else>
              <img src="images/icons/24x24/cancel.png" />
              Cancel
</#if>
            </button>
            <button type="submit" tabindex="100">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
