<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("name").item(0).focus();
    }

    function doPrevious() {
        document.getElementsByName("liquidsite.prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="liquidsite.step" value="2" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="category" value="domain" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/edit.png" alt="Add" />
          </td>
          <td colspan="2">
            <h2>Enter Domain Details (Step 2 of 2)</h2>

            <p>Enter the details of the domain you wish to add.</p>
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
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
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
                   name="description" value="${description}" />
            <p>The description of the domain. This description is
            only visible in the administration application.</p>
          </td>
        </tr>
        <tr>
          <th>
            Host&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" tabindex="3" size="30"
                   name="host" value="${host}" />
            <p>The initial web host name connected to the domain. All
            domains but the ROOT domain must have at least one web
            host name.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="5" onclick="doPrevious()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="4">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
