<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("category").item(0).checked = "checked";
        document.getElementsByName("category").item(0).focus();
    }
    </script>

    <form method="post">
      <input type="hidden" name="step" value="1" />
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
            <input type="radio" name="category" value="domain" />
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
          <th>
            <input type="radio" name="category" value="site" />
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
        <tr>
          <td colspan="2">
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">