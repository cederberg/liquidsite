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
        utilLoadScript("loadcontent.js?type=" + type + "&id=" + id +
                       "&open=" + open,
                       "iframe",
                       "script/tree-iframe.js");
    }

    function openItem(type, id) {
        utilLoadScript("opencontent.js?type=" + type + "&id=" + id,
                       "iframe",
                       "script/object-iframe.js");
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
