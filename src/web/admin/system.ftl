<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function doRestart() {
        utilGetElement("action").value = "restart";
        return true;
    }

    function doBackup() {
        utilGetElement("action").value = "backup";
        return true;
    }

    function doRestore() {
        utilGetElement("action").value = "restore";
        return true;
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="action" value="none" />
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
        <tr>
          <th>
            <button type="submit" onclick="return doBackup();">
              <img src="images/icons/24x24/save.png" />
              Backup
            </button>
          </th>
          <td>
            <strong>Backing up</strong> a domain creates an archive file
            with the full contents of the domain. This operation is useful
            for copying a domain to another server or duplicating it under
            another name on the same server. The result of the backup will
            be stored in the backup directory on the server, so machine
            access is required to download the resulting data.
          </td>
        </tr>
        <tr>
          <th>
            <button type="submit" onclick="return doRestore();">
              <img src="images/icons/24x24/revert.png" />
              Restore
            </button>
          </th>
          <td>
            <strong>Restoring</strong> creates a new domain with the contents
            of a backup archive file. This operation is useful for copying a
            domain from another server or for duplicating a domain under
            another name. The restore operation can be either complete or just
            with the latest revisions. The domain to create cannot previously
            exist.
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
