<#include "header.ftl">

    <script type="text/javascript">
        function initialize() {
        }
    </script>

    <form method="post" action="install.html">
      <input type="hidden" name="step" value="2" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="3">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td>
            <h2>Select Database (Step 2 of 5)</h2>

            <p>Select the database where you wish to store the Liquid
            Site data. Note that no information will be lost, even if
            an existing database is chosen.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>  
          </td>
        </tr>
        <tr>
          <td class="field">
            <table class="border">
              <tr>
                <th>Database</th>
                <th>Statistics</th>
                <th>Information</th>
              </tr>
<#list databaseInfo as info>
  <#if info.name = database>
    <#assign found = true>
  </#if>
  <#if info.status = 0>
    <#assign style = " class=\"unimportant\"">
    <#assign options = "disabled=\"disabled\"">
  <#elseif info.name = database>
    <#assign style = "">
    <#assign options = "checked=\"checked\"">
  <#else>  
    <#assign style = "">
    <#assign options = "">
  </#if>
              <tr ${style}>
                <td>
                  <input type="radio" name="database1" ${options}
                         value="${info.name}" /> 
                  ${info.name}
                </td>
                <td>
                  ${info.tables} Tables
                </td>
                <td>
                  ${info.info}
                </td>
              </tr>
</#list>
<#if !enableCreate>
  <#assign style = " class=\"unimportant\"">
  <#assign options = "disabled=\"disabled\"">
  <#assign fieldvalue = "">
<#elseif !found?exists>
  <#assign style = "">
  <#assign options = "checked=\"checked\"">
  <#assign fieldvalue = database>
<#else>
  <#assign style = "">
  <#assign options = "">
  <#assign fieldvalue = "">
</#if>
              <tr ${style}>
                <td>
                  <input type="radio" name="database1" value="" ${options} />
                  <input type="input" name="database2" value="${fieldvalue}"
                         size="15" ${options} />
                </td>
                <td colspan="2">
                  Creates a new database with the specified name.
                </td>
              </tr>
            </table>
          </td>
        </tr>
<#if !enableNext>
  <#assign options = " disabled=\"disabled\"">
<#else>
  <#assign options = "">
</#if>
        <tr>
          <td>
            <button type="submit" name="prev">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" ${options}>
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
