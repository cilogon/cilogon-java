<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head>
    <title>CILogon OAuth 2 Delegation Service Client Registration Page</title>
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
        <h2>Welcome to the CILogon OAuth 2 Delegation Service Client Registration Page</h2>

        <p>This page allows you to register your client with the
            CILogon delegation service that supports the OIDC/OAuth 2. To get your client approved,
            please fill out the form below. Your request will be evaluated for approval. For more information,
            please make sure you read the
            <a href="http://grid.ncsa.illinois.edu/myproxy/oauth/client/manuals/registering-with-an-oauth2-server.xhtml"
               target="_blank">Registering a Client with an OAuth 2 server</a> document.
        </p><br>
        <table>
            <tr>
                <td>Client Name:</td>
                <td><input type="text" size="25" name="${clientName}" value="${clientNameValue}"/></td>
            </tr>
            <tr>
                <td>Contact email:</td>
                <td><input type="text" size="25" name="${clientEmail}" value="${clientEmailValue}"/></td>
            </tr>
            <tr>
                <td>Home URL:</td>
                <td><input type="text" size="25" name="${clientHomeUrl}" value="${clientHomeUrlValue}"/></td>
            </tr>
            <tr>
                <td>Callback URLs:</td>
                <td>
                       <textarea id="${callbackURI}" rows="10" cols="80"
                                 name="${callbackURI}">${callbackURIValue}</textarea>
                </td>
            </tr>
            <tr>
                <td>Scopes:</td>
                <td>
                    <c:forEach items="${scopes}" var="scope">
                        <input type="checkbox"
                               name="chkScopes"
                               value="${scope}"
                            <c:set var="CILTestStoreProviderImpl" scope="session" value="${scope}"/>
                               <c:if test="${CILTestStoreProviderImpl == 'openid'}">checked="checked"</c:if>
                                >${scope}&nbsp;
                    </c:forEach>
                </td>
            </tr>
            <tr>
                <td ${rtFieldVisible}>Refresh Token lifetime:</td>
                <td ${rtFieldVisible}><input type="text" size="25" name="${rtLifetime}" value="${rtLifetimeValue}"/>(in
                    seconds - leave blank for no refresh tokens.)
                </td>
            </tr>
            <tr>
                <td>Issuer (optional):</td>
                <td><input type="text" size="25" name="${issuer}" value="${issuerValue}"/></td>
            </tr>
            <tr>
                <td><input type="submit" value="submit"/></td>
            </tr>
            <tr>
                <td colspan="2"><b><font color="red">${retryMessage}</font></b></td>
            </tr>
        </table>
        <input type="hidden" id="status" name="${action}"
               value="${request}"/>
    </form>
</div>
</body>
</html>