<%
    request.setAttribute("onload", "initialize()");
%>
<%@ include file="header.jsp" %>

    <script type="text/javascript" src="script/tree.js"></script>

    <script type="text/javascript" src="script/object.js"></script>

    <script type="text/javascript">
    function initialize() {
        objectInitialize("object");
        treeInitialize("tree", loadItem, openItem);
        <%=request.getAttribute("initialize")%>
    }

    function loadItem(type, id) {
        var script = document.createElement('script');

        script.type = "text/javascript";
        script.src = "loadsite.js?type=" + type + "&id=" + id;
        document.getElementsByTagName("head")[0].appendChild(script);
    }

    function openItem(type, id) {
        var script = document.createElement('script');

        objectClear();
        script.type = "text/javascript";
        script.src = "opensite.js?type=" + type + "&id=" + id;
        document.getElementsByTagName("head")[0].appendChild(script);
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

<%@ include file="footer.jsp" %>