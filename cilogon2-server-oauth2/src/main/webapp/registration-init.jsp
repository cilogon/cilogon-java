<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">

    <title>CILogon OIDC/OAuth 2.0 Client Registration</title>
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

    <h2>CILogon OIDC/OAuth 2.0 Client Registration</h2>

    <p>
    Please fill out the form below to register your OIDC/OAuth 2.0
    client with CILogon. 
    <p/>
    <h4>Your request will be manually evaluated for approval.</h4>
    </p>
    <p>
    For more information, please read the
    <a href="http://grid.ncsa.illinois.edu/myproxy/oauth/client/manuals/registering-with-an-oauth2-server.xhtml"
    target="_blank">Registering a Client with an OAuth 2 server</a>
    documentation.
    </p>

    <hr />

    <form action="${actionToTake}" method="post">

      <div class="form-group row">
        <label for="inputClientName" class="col-sm-2 col-form-label">Client
          Name</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="inputClientName"
            name="${clientName}" value="${clientNameValue}" required
            aria-describedBy="clientNameHelp" 
            placeholder="Enter a name for your OAuth 2.0 client"/>
          <small id="clientNameHelp" class="form-text text-muted">The Client
            Name is displayed to end-users on the Identity Provider selection
            page.</small>
        </div>
      </div>

      <div class="form-group row">
        <label for="inputContactEmail" class="col-sm-2 col-form-label">Contact
          Email</label>
        <div class="col-sm-10">
          <input type="email" class="form-control" id="inputContactEmail"
            name="${clientEmail}" value="${clientEmailValue}" required
            aria-describedBy="contactEmailHelp"
            placeholder="Enter your email address"/>
          <small id="contactEmailHelp" class="form-text text-muted">A client
            approval email will be sent to this address.</small>
        </div>
      </div>

      <div class="form-group row">
        <label for="inputHomeURL" class="col-sm-2 col-form-label">Home
          URL</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="inputHomeURL"
            name="${clientHomeUrl}" value="${clientHomeUrlValue}" required 
            aria-describedBy="homeURLHelp"
            placeholder="Enter the URL for your client's home page."/>
          <small id="homeURLHelp" class="form-text text-muted">The Home URL
            is used as the hyperlink for the Client Name.</small>
        </div>
      </div>

      <div class="form-group row">
        <label for="inputCallbackURLs" class="col-sm-2
          col-form-label">Callback URLs</label>
        <div class="col-sm-10">
          <textarea class="form-control" id="${callbackURI}" 
            name="${callbackURI}" rows="5" required
            placeholder="Enter your callback URLs, one per line.
The redirect_uri parameter must exactly match one URL in this list."></textarea>
        </div>
      </div>

      <fieldset class="form-group">
        <div class="row">
          <legend class="col-form-label col-sm-2 pt-0">Scopes</legend>
          <div class="col-sm-10">
            <c:forEach items="${scopes}" var="scope">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" name="chkScopes" value="${scope}" id="${scope}Check"
                    <c:set var="CILTestStoreProviderImpl" scope="session" value="${scope}"/>
                    <c:if test="${CILTestStoreProviderImpl == 'openid'}">checked="checked" disabled</c:if>
                />
                <label class="form-check-label" for="${scope}Check">${scope}</label>
              </div>
            </c:forEach>
            <small id="scopesHelp" class="form-text text-muted"><a
              href="https://www.cilogon.org/oidc"
              target="_blank">Information on scopes</a></small>
          </div>
        </div>
      </fieldset>

      <div class="form-group row">
        <label for="inputRtLifetime" class="col-sm-2 col-form-label">Refresh
          Token Lifetime</label>
        <div class="col-sm-10">
          <input type="number" class="form-control" id="inputRtLifetime"
            name="${rtLifetime}" value="${rtLifetimeValue}"
            aria-describedBy="rtLifetimeHelp"
            placeholder="(Optional) lifetime in seconds"/>
          <small id="rtLifetimeHelp" class="form-text text-muted">Leave
            blank if refresh tokens are not required.</small>
        </div>
      </div>

      <div class="form-group row">
        <label for="inputIssuer" class="col-sm-2
          col-form-label">Issuer</label>
        <div class="col-sm-10">
          <input type="text" class="form-control" id="inputIsser"
            name="${issuer}" value="${issuerValue}"
            aria-describedBy="issuerHelp"
            placeholder="(Optional)"/>
          <small id="issuerHelp" class="form-text text-muted">Defaults to
            the URL of the CILogon Service.</small>
        </div>
      </div>

      <input type="hidden" id="status" name="${action}" value="${request}"/>

      <div class="row">
          <b><font color="red">${retryMessage}</font></b>
      </div>

      <button type="submit" class="btn btn-primary mb-2">Register Client</button>
    </form>
</div>
</body>
</html>
