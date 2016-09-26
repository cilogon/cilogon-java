<%--
  User: Jeff Gaynor
  Date: May 27, 2011
  Time: 10:36:41 AM
  Properties included:
     field names:
        * clientName
        * clientEmail
        * clientHomeUrl
        * clientErrorUrl
        * clientPublicKey
        * action
        * request
     Control flow:
        * actionToTake = url to invoke on submitting this form
        * action = name of hidden field containing the request property
        * request = contents of field with the state of this
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<head>
    <title>CILogon Client Registration Page</title>
    <link rel="stylesheet"
          type="text/css"
          media="all"
          href="static/cilogon.css"/>
</head>


<style type="text/css">
    .hidden {
        display: none;
    }

    .unhidden {
        display: block;
    }
</style>
<script type="text/javascript">
    function unhide(divID) {
        var item = document.getElementById(divID);
        if (item) {
            item.className = (item.className == 'hidden') ? 'unhidden' : 'hidden';
        }
    }
</script>
<body>
<div id="topimgfill">
    <div id="topimg"/>
</div>

<br clear="all"/>

<div class="main">

    <form action="${actionToTake}" method="post">
        <h2>Welcome to the CILogon Client Registration Page</h2>

        <p>This page allows you to register your client with the
            CILogon service. To get your client approved,
            please fill out the form below. Your request will be evaluated for approval. For more information,
            please make sure you read the
            <a href="https://docs.google.com/a/cilogon.org/document/d/1LxiGc0NJmZz_yYyj1r2xfV6Cw4EtW_5cEDl230xZ5io/edit?hl=en_US"
               target="_blank">Client Registration Document</a>.
        </p><br>
        <table>
            <tr>
                <td>Client Name:</td>
                <td><input type="text" size="25" name="${clientName}"/></td>
            </tr>
            <tr>
                <td>Contact email:</td>
                <td><input type="text" size="25" name="${clientEmail}"/></td>
            </tr>
            <tr>
                <td>Home URL:</td>
                <td><input type="text" size="25" name="${clientHomeUrl}"/></td>
            </tr>
            <tr>
                <td>Error url:</td>
                <td><input type="text" size="25" name="${clientErrorUrl}"/></td>
            </tr>
            <tr>
                <td>Public Key:</td>
                <td>
                <textarea id="${clientPublicKey}" rows="20" cols="80"
                          name="${clientPublicKey}">Paste public key here</textarea>
                </td>
            </tr>
            <tr>
                <td><input type="submit" value="submit"/></td>
            </tr>
        </table>
        <input type="hidden" id="status" name="${action}"
               value="${request}"/>
    </form>
</div>
</body>
</html>