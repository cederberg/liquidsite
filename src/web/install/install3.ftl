<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        var  user1 = document.getElementsByName("user1").item(0);
        var  user2 = document.getElementsByName("user2").item(0);
        var  pwd2 = document.getElementsByName("password2").item(0);

        if (user1.value == "") {
            user2.disabled = "";
            pwd2.disabled = "";
        } else {
            user2.disabled = "disabled";
            pwd2.disabled = "disabled";
        }
    }

    function previous() {
        document.getElementsByName("prev").item(0).value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" action="install.html" accept-charset="UTF-8">
      <input type="hidden" name="step" value="3" />
      <input type="hidden" name="prev" value="" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="5">
            <img src="images/icons/48x48/install.png" alt="Install" />
          </td>
          <td colspan="2">
            <h2>Select Database User (Step 3 of 5)</h2>

            <p>Select the database user to use when running Liquid 
            Site normally. It is highly recommended to create a new
            user with minimal privileges.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>  
          </td>
        </tr>
        <tr>
          <th>
            User&nbsp;Name:
          </th>
          <td class="field">
            <select name="user1" tabindex="1" onchange="initialize()">
<#list userNames as name>
  <#if name = user>
    <#assign options = " selected=\"selected\"">
    <#assign found = true>
  <#else>
    <#assign options = "">
  </#if>
              <option value="${name}" ${options}>${name}</option>
</#list>
<#if found?exists>
  <#assign options = "">
<#else>
  <#assign options = " selected=\"selected\"">
</#if>
<#if enableCreate>
              <option value="" ${options}>Create New --&gt;</option>
</#if>
            </select>
            <input type="text" tabindex="2" size="20"
                   name="user2" value="${user}" />
            <p>This is the database user name to use for accessing 
            the Liquid Site database.</p>
          </td>
        </tr>
        <tr>
          <th>
            Password:
          </th>
          <td class="field">
            <input type="password" tabindex="3" size="12"
                   name="password1" value="${password}" />
            <p>The password for the database user above.</p>
          </td>
        </tr>
        <tr>
          <th>
            Verify&nbsp;Password:
          </th>
          <td class="field">
            <input type="password" tabindex="4" size="12"
                   name="password2" value="${password}" />
            <p>Verify the password for the database user. This field 
            is only used when creating new users.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="6" onclick="previous()">
              <img src="images/icons/24x24/left_arrow.png" />
              Previous
            </button>
            <button type="submit" tabindex="5">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
