<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("backup").focus();
    }

    function doPrevious() {
        window.location='system.html'
    }

    function doRestore() {
        return true;
    }
    </script>

    <form method="post" accept-charset="UTF-8">
      <input type="hidden" name="action" value="restore" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/system.png" alt="System" />
          </td>
          <td colspan="2">
            <h2>Select Restore Options (Step 1 of 1)</h2>

            <p>Select the options for the backup restore operation.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
        <tr>
          <th>
            Backup:
          </th>
          <td class="field">
            <select tabindex="1" name="backup">
              <option value="">&lt; None &gt;</option>
<#list backups as item>
  <#if item = backup>
              <option value="${item?html}" selected="selected">${item?html}</option>
  <#else>
              <option value="${item?html}">${item?html}</option>
  </#if>
</#list>
            </select>
            <p>Select the backup file to restore.</p>
          </td>
        </tr>
        <tr>
          <th>
            Domain&nbsp;Name:
          </th>
          <td class="field">
            <input type="text" tabindex="2" size="30"
                   name="domain" value="${domain?html}" />
            <p>The domain name uniquely identifies the new domain that
            will be created in the database. The domain name cannot be
            changed, and is normally a short UPPERCASE word.</p>
          </td>
        </tr>
        <tr>
          <th>
            Revisions:
          </th>
          <td class="field">
            <select tabindex="3" name="revisions">
              <option value="all">All</option>
<#if revisions == "latest">
  <#assign selected>selected="selected"</#assign>
<#else>
  <#assign selected="">
</#if>
              <option value="latest" ${selected}>Only Latest</option>
<#if revisions == "work">
  <#assign selected>selected="selected"</#assign>
<#else>
  <#assign selected="">
</#if>
              <option value="work" ${selected}>Only Latest, Store As 
              Work</option>
            </select>
            <p>Select the revision policy to use. Either all revision can
            be restored, or only the latest revision of each object. In the 
            latter case the revisions can also be added as unpublished work
            revisions.</p>
          </td>
        </tr>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="103" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button type="submit" tabindex="102" onclick="return doRestore();">
              <img src="images/icons/24x24/revert.png" />
              Restore
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
