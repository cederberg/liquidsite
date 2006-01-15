<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript" src="script/tree.js"></script>
    <script type="text/javascript" src="script/object.js"></script>
    <script type="text/javascript">
    function initialize() {
        objectInitialize("object");
        treeInitialize("tree", loadItem, openItem);
        ${initialize}
    }

    function loadItem(type, id, open) {
        utilLoadScript("loadsite.js?type=" + type + "&id=" + id +
                       "&open=" + open,
                       "iframe",
                       "script/iframefix.js");
    }

    function openItem(type, id) {
        utilLoadScript("opensite.js?type=" + type + "&id=" + id,
                       "iframe",
                       "script/iframefix.js");
    }
    </script>


    <table>
      <tr>
        <td id="tree" class="treeview">
        </td>
        <td id="object" style="padding-left: 30px;">
        </td>
      </tr>
    </table>

    <div id="iframe"></div>

<#include "footer.ftl">
