<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("category").item(0).checked = "checked";
        document.getElementsByName("category").item(0).focus();
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <input type="hidden" name="step" value="1" />
      <input type="hidden" name="prev" value="" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/edit.png" alt="Add" />
          </td>
          <td colspan="2">
            <h2>Select Object Category (Step 1 of 2)</h2>

            <p>Select the category of the object you wish to add.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
<#if enableDomain?exists>
        <tr>
          <th style="width: 7em;">
            <input type="radio" tabindex="1" 
                   name="category" value="domain" />
            Domain
          </th>
          <td>
            A domain consists of a set of related resources,
            normally belonging to a single organization or entity.
            Users, groups and content objects can only be shared 
            within a domain. Note that a domain does not have to 
            correlate to an Intenet domain name, although that 
            often is the case.
          </td>
        </tr>
</#if>
<#if enableSite?exists>
        <tr>
          <th style="width: 7em;">
            <input type="radio" tabindex="2"
                   name="category" value="site" />
            Site
          </th>
          <td>
            A web site consists of a collection of documents and
            files available from a single location on the world wide
            web. A web site is identified by protocol, host name, 
            port number, and base directory.
          </td>
        </tr>
</#if>
<#if enableFolder?exists>
        <tr>
          <th style="width: 7em;">
            <input type="radio" tabindex="3"
                   name="category" value="folder" />
            Folder
          </th>
          <td>
            A folder contains other objects. All object in the folder
            must have unique names. The special names 'index.html' 
            and 'index.htm' are used to identify default objects.
          </td>
        </tr>
</#if>
<#if enableFile?exists>
        <tr>
          <th style="width: 7em;">
            <input type="radio" tabindex="4" 
                   name="category" value="file" />
            File
          </th>
          <td>
            A file contains static data. The file contents may be any
            kind of data, including HTML, text, images, or binary 
            data.
          </td>
        </tr>
</#if>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="6" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button tabindex="5" type="submit">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
