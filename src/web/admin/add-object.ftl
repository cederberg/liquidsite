<#assign onload = "initialize()">
<#include "header.ftl">

    <script type="text/javascript" src="script/util.js"></script>
    <script type="text/javascript">
    function initialize() {
        utilGetElement("category").checked = "checked";
        utilGetElement("category").focus();
    }

    function doPrevious() {
        utilGetElement("liquidsite.prev").value = "true";
        document.forms.item(0).submit();
    }
    </script>

    <form method="post" accept-charset="UTF-8">
<#if startpage?has_content>
      <input type="hidden" name="liquidsite.startpage" value="${startpage}" />
</#if>
      <input type="hidden" name="liquidsite.step" value="1" />
      <input type="hidden" name="liquidsite.prev" value="" />
      <input type="hidden" name="type" value="${type}" />
      <input type="hidden" name="id" value="${id}" />
      <table class="form">
        <tr>
          <td class="decoration" rowspan="10">
            <img src="images/icons/48x48/edit.png" alt="Add" />
          </td>
          <td colspan="2">
            <h2>Select Object Category (Step 1 of 2)</h2>

            <p>Select the category of the object you wish to add.</p>
<#if error?has_content>
            <p class="incorrect">Error: ${error?html}</p>
</#if>
          </td>
        </tr>
<#if enableDomain?exists>
        <tr>
          <th style="width: 7em;">
            <input type="radio" tabindex="1"
                   name="category" value="domain" />
            Domain
          </th>
          <td>
            A domain consists of a set of related resources,
            normally belonging to a single organization or entity.
            Users, groups and content objects can only be shared
            within a domain. Note that a domain does not have to
            correlate to an Intenet domain name, although that
            often is the case.
          </td>
        </tr>
</#if>
<#if enableSite?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="2"
                   name="category" value="site" />
            Site
          </th>
          <td>
            A web site consists of a collection of documents and
            files available from a single location on the world wide
            web. A web site is identified by protocol, host name,
            port number, and base directory.
          </td>
        </tr>
</#if>
<#if enableFolder?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="3"
                   name="category" value="folder" />
            Folder
          </th>
          <td>
            A folder contains other objects. All object in the folder
            must have unique names. The special names 'index.html'
            and 'index.htm' are used to identify default objects.
          </td>
        </tr>
</#if>
<#if enablePage?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="4"
                   name="category" value="page" />
            Page
          </th>
          <td>
            A web page contains static or dynamic content. The page
            consists of page elements with HTML code. Each page
            element can be defined in the page or inherited from a
            template.
          </td>
        </tr>
</#if>
<#if enableFile?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="5"
                   name="category" value="file" />
            File
          </th>
          <td>
            A file contains static data. The file contents may be any
            kind of data, including HTML, text, images, or binary
            data.
          </td>
        </tr>
</#if>
<#if enableTranslator?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="6"
                   name="category" value="translator" />
            Translator
          </th>
          <td>
            A translator maps URLs to other URLs, content sections or
            error pages. Only URLs not matching anything else are
            passed to the translator.
          </td>
        </tr>
</#if>
<#if enableTemplate?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="7"
                   name="category" value="template" />
            Template
          </th>
          <td>
            A web page template is used as a basis for creating web
            pages. The template consists of page elements with HTML
            code. Each page element can be defined in the template
            or inherited from a parent template.
          </td>
        </tr>
</#if>
<#if enableSection?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="8"
                   name="category" value="section" />
            Section
          </th>
          <td>
            A document section is used for grouping similar documents.
            The section also defines the document properties
            available for each document.
          </td>
        </tr>
</#if>
<#if enableDocument?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="9"
                   name="category" value="document" />
            Document
          </th>
          <td>
            A document can store any type of text content, such as
            articles, news and similar. The containing section
            defines the document properties available.
          </td>
        </tr>
</#if>
<#if enableForum?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="10"
                   name="category" value="forum" />
            Forum
          </th>
          <td>
            A forum is used to allow site users exchange ideas and
            discuss. The user messages are grouped into topics inside
            the forum.
          </td>
        </tr>
</#if>
<#if enableTopic?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="11"
                   name="category" value="topic" />
            Topic
          </th>
          <td>
            A topic is a set of forum messages that follow each other
            in a single discussion. The users are free to post
            messages to an existing topic or to create a new topic.
          </td>
        </tr>
</#if>
<#if enablePost?exists>
        <tr>
          <th style="width: 8em;">
            <input type="radio" tabindex="12"
                   name="category" value="post" />
            Post
          </th>
          <td>
            A post is a forum message. It contains the message text
            that the user posted to the thread. Normally only the user
            himself and the forum moderators are allowed to edit or
            remove the post.
          </td>
        </tr>
</#if>
        <tr>
          <td class="buttons" colspan="2">
            <button type="button" tabindex="101" onclick="doPrevious()">
              <img src="images/icons/24x24/cancel.png" />
              Cancel
            </button>
            <button tabindex="100" type="submit">
              Next
              <img src="images/icons/24x24/right_arrow.png" />
            </button>
          </td>
        </tr>
      </table>
    </form>

<#include "footer.ftl">
