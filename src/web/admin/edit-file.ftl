<#assign issite = liquidsite.request.path?ends_with("site.html")>
<#assign isadd = liquidsite.request.path?ends_with("add-site.html") ||
                 liquidsite.request.path?ends_with("add-content.html")>
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

    <form method="post" enctype="multipart/form-data" accept-charset="UTF-8">
<#if isadd>
      <input type="hidden" name="liquidsite.step" value="2" />
<#else>
      <input type="hidden" name="liquidsite.step" value="1" />
</#if>
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
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
            <input type="text" tabindex="1" size="30"
                   name="name" value="${name}" />
            <p>The file name is part of the URL by which the user 
            will access the file contents. The file name should use
            only English alphabet characters or numbers without any 
            spaces.</p>
          </td>
        </tr>
<#if issite && !isadd>
        <tr>
          <th>
            Folder:
          </th>
          <td class="field">
            <select tabindex="2" name="parent">
  <#list folders as item>
    <#if parent == item.id>
              <option value="${item.id}" selected="selected">${item.name?xml}</option>
    <#else>
              <option value="${item.id}">${item.name?xml}</option>
    </#if>
  </#list>
            </select>
            <p>The parent folder controls the location of the file.
            Note that changing folder will also modify the file
            URL.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            File&nbsp;Upload:
          </th>
          <td class="field">
            <input type="file" tabindex="3"
                   name="upload" />
            <p>The file contents to upload. This is the local file
            that contains the data. The file will be uploaded and
            inserted into the system. Note that the file extension on
            the uploaded file determines the file type seen by the
            user.
<#if !isadd>
            If you leave this field blank, the previous file data will
            be reused.
</#if>
            </p>
          </td>
        </tr>
<#if content?exists>
        <tr>
          <th>
            File&nbsp;Content:
          </th>
          <td class="field">
            <textarea tabindex="4" cols="70" rows="30"
                      name="content">${content}</textarea>
            <p>The file content. The file data can either be edited
            here or being uploaded.</p>
          </td>
        </tr>
</#if>
        <tr>
          <th>
            Comment:
          </th>
          <td class="field">
            <input type="text" tabindex="5" size="40"
                   name="comment" value="${comment}" />
            <p>The revision comment.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="7" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="6">
              <img src="images/icons/24x24/save.png" />
              Save
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
