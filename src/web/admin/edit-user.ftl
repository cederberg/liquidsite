<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        document.getElementsByName("name").item(0).focus();
    }
    </script>

    <form method="post">
      <input type="hidden" name="edituser" value="true" />
      <input type="hidden" name="step" value="1" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/home.png" alt="Edit" />
          </td>
          <td colspan="2">
            <h2>Change Account Details (Step 1 of 1)</h2>

<#if error?has_content>
            <p class="incorrect">Error: ${error}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Name:
          </th>
          <td class="field">
            <input type="text" name="name" value="${name}" size="20" />
          </td>
        </tr>
        <tr>
          <th>
            E-mail:
          </th>
          <td class="field">
            <input type="text" name="email" value="${email}" size="30" />
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
