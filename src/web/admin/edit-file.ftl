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

    <form method="post" enctype="multipart/form-data">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="step" value="2" />
      <input type="hidden" name="prev" value="" />
      <input type="hidden" name="category" value="file" />
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
            <h2>Enter File Details (Step 2 of 2)</h2>

            <p>Enter the details of the file you wish to add.</p>
<#else>
            <h2>Enter File Details (Step 1 of 1)</h2>

            <p>Edit the details of the file.</p>
</#if>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            File&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" name="name" value="${name}" size="30" />
            <p>The file name is part of the URL by which the user 
            will access the file contents. The file name should use
            only English alphabet characters or numbers without any 
            spaces.</p>
          </td>
        </tr>
        <tr>
          <th>
            File&nbsp;Content:
          </th>
          <td class="field">
            <input type="file" name="content" />
            <p>The file content. This is the local file that contains
            the data. The file will be uploaded and inserted into the 
            system. Note that the file extension on the uploaded file
            determines the file type seen by the user.
<#if !isadd>
            If you leave this field blank, the previous file data will
            be reused.
</#if>
            </p>
          </td>
        </tr>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" name="comment" value="${comment}" size="40" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" onclick="previous()">
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