<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function doRestart() {
        utilGetElement("operation").value = "restart";
        return true;
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="operation" value="none" />
      <table class="form">
        <tr>
          <th>
            <button type="submit" onclick="return doRestart();">
              <img src="images/icons/24x24/refresh.png" />
              Restart
            </button>
          </th>
          <td>
            <strong>Restarting</strong> the application will reread the
            configuration and scrap all existing database connections and
            cached data. The restart is "soft" in the sense that no web
            requests are lost in the process. This operation is suitable if
            changes have been made to the database or file system outside
            this application.
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
