<#assign title = "Closing Dialog...">
<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript">
    function initialize() {
        if (window.opener != null) {
            if (window.opener.doReload) {
                window.opener.doReload();
            } else {
                window.opener.location.reload(1);
            }
        }
        window.close();
    }
    </script>

    <a href="javascript:window.close()">Close this window</a>

<#include "footer.ftl">
