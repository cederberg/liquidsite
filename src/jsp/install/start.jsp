<%@ include file="header.jsp" %>

    <h1>Liquid Site Installation Guide</h1>

	<p>Welcome to the Liquid Site installation guide! By following
	the steps in this guide you will install the Liquide Site data
	repository and perform the basic setup. Help will be available
	throughout this guide, providing explainations and 
	recommendations.</p>
	
    <p>Please select the correct installation type below.</p>
    
    <p>&nbsp;</p>

    <table>
      <tr>
        <td>
          <img src="images/arrow.jpeg" alt="&gt;&gt;" />
        </td>
        <td>
          <h2>New Installation</h2>

          <p>Select this alternative if you are installing Liquid
          Site for the first time. This will create a new database
          for storing the application data.</p>

          <form method="post" action="install.html">
            <input type="submit" value="   Next &gt;&gt;   "/>
          </form>
          
          <p>&nbsp;</p>
        </td>
      </tr>
      <tr>
        <td>
          <img src="images/arrow.jpeg" alt="&gt;&gt;" />
        </td>
        <td>
          <h2>Update Installation</h2>

          <p>Select this alternative if you already have Liquid 
          Site installed. This will update your existing installation
          to the latest version while keeping all your data intact.</p>

          <form method="post" action="update.html">
            <input type="submit" value="   Next &gt;&gt;   "/>
          </form>
        </td>
      </tr>
    </table>

<%@ include file="footer.jsp" %>
