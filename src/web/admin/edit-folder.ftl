<#assign isadd = liquidsite.page.path?ends_with("add-site.html")>
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("name").item(0).focus();
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="step" value="2" />
      <input type="hidden" name="prev" value="" />
      <input type="hidden" name="category" value="folder" />
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
            <h2>Enter Folder Details (Step 2 of 2)</h2>

            <p>Enter the details of the folder you wish to add.</p>
<#else>
            <h2>Enter Folder Details (Step 1 of 1)</h2>

            <p>Edit the details of the folder.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Folder&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
            <p>The folder name is part of the URL by which the user 
            will access the folder contents. The folder name should 
            use only English alphabet characters or numbers without 
            any spaces.</p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="40"
                   name="comment" value="${comment}" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="4" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="3">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
