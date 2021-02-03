<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css"
          integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk"
          crossorigin="anonymous">

    <title>CILogon OIDC/OAuth 2.0 Client Registration</title>
    <link rel="stylesheet"
          type="text/css"
          media="all"
          href="static/cilogon.css"/>
</head>

<body>
<div id="topimgfill">
    <div id="topimg"></div>
</div>

<div class="main">

    <h2>CILogon OpenID Connect (OIDC) Client Registration</h2>

    <p>
        Please fill out the form below to register your OIDC/OAuth 2.0
        client with CILogon.
    </p>
    <h4>Your request will be manually evaluated for approval within 1 business day.</h4>
    <p>
        For more information, please see the
        <a href="https://www.cilogon.org/oidc"
           target="_blank">CILogon OpenID Connect (OIDC)</a>
        documentation.
    </p>

    <hr/>

    <form action="${actionToTake}" method="post">

        <div class="form-group row my-4">
            <label for="inputClientName" class="col-sm-2 col-form-label">Client
                Name</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputClientName"
                       name="${clientName}" value="${clientNameValue}" required
                       aria-describedBy="clientNameHelp"
                       placeholder="Name of your OIDC/OAuth 2.0 client"/>
                <small id="clientNameHelp" class="form-text text-muted">The Client
                    Name is displayed to end-users on the Identity Provider selection
                    page.
                </small>
            </div>
        </div>

        <div class="form-group row my-4">
            <label for="inputContactEmail" class="col-sm-2 col-form-label">Contact
                Email</label>
            <div class="col-sm-10">
                <input type="email" class="form-control" id="inputContactEmail"
                       name="${clientEmail}" value="${clientEmailValue}" required
                       aria-describedBy="contactEmailHelp"
                       placeholder="Your official university/organization email address"/>
                <small id="contactEmailHelp" class="form-text text-muted">This
                    email address is used for operational notices regarding your
                    client and for validating your affiliation. A 
                    mailing list address for your operations team is recommended.
                </small>
            </div>
        </div>

        <div class="form-group row my-4">
            <label for="inputHomeURL" class="col-sm-2 col-form-label">Home
                URL</label>
            <div class="col-sm-10">
                <input type="url" class="form-control" id="inputHomeURL"
                       name="${clientHomeUrl}" value="${clientHomeUrlValue}" required
                       aria-describedBy="homeURLHelp"
                       placeholder="URL of your client's home page"/>
                <small id="homeURLHelp" class="form-text text-muted">The Home URL
                    is used as the hyperlink for the Client Name above.
                </small>
            </div>
        </div>

        <div class="form-group row my-4">
            <label for="${callbackURI}" class="col-sm-2
          col-form-label">Callback URLs</label>
            <div class="col-sm-10">
          <textarea class="form-control" id="${callbackURI}"
                    name="${callbackURI}" rows="5" required
                    placeholder="Enter your callback URLs, one per line. The redirect_uri parameter must exactly match a URL in this list.">${callbackURIValue}</textarea>
            </div>
        </div>

        <fieldset class="form-group">
            <div class="row">
                <legend class="col-form-label col-sm-2 pt-0">Client Type</legend>
                <div class="col-sm-10">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="clientIsPublic"
                        id="confidential" value="no" checked="checked"
                        aria-describedby="clientIsPublicHelp"
                        />
                       <label class="form-check-label" for="confidential">Confidential</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="clientIsPublic"
                        id="public" value="on"
                        />
                        <label class="form-check-label" for="public">Public</label>
                    </div>
                    <small id="clientIsPublicHelp" class="form-text text-muted">A <a target="_blank" href="https://oauth.net/2/client-types/">Public client</a>
                        does not use a client_secret and allows ONLY the "openid" scope.
                    </small>
                </div>
            </div>
        </fieldset>

        <fieldset class="form-group">
            <div class="row">
                <legend class="col-form-label col-sm-2 pt-0">Scopes</legend>
                <div class="col-sm-10">
                    <div id="allscopes" class="collapse show">
                        <c:forEach items="${scopes}" var="scope">
                            <div class="form-check">
                                <input class="form-check-input" type="checkbox" name="chkScopes" value="${scope}"
                                       id="${scope}Check"
                                        <c:set var="CILTestStoreProviderImpl" scope="session" value="${scope}"/>
                                       <c:if test="${CILTestStoreProviderImpl == 'openid'}">checked="checked"
                                       disabled="disabled"</c:if>
                                />
                                <label class="form-check-label" for="${scope}Check">${scope}</label>
                            </div>
                        </c:forEach>
                        <input type="hidden" name="chkScopes" value="openid"/>
                    </div>
                    <div id="openidscope" class="collapse">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="ignore" value="openid"
                                   id="ignoreopenid" checked="checked"
                                   disabled="disabled"/>
                            <label class="form-check-label" for="ignoreopenid">openid</label>
                        </div>
                    </div>
                    <small id="scopesHelp" class="form-text text-muted"><a
                            href="https://www.cilogon.org/oidc#h.p_PEQXL8QUjsQm"
                            target="_blank">Information on scopes and returned claims</a></small>
                </div>
            </div>
        </fieldset>

        <div class="form-group row my-4">
            <label for="inputRtLifetime" class="col-sm-2 col-form-label">Refresh Tokens</label>
            <div class="col-sm-10">
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="refreshTokensYesNo"
                    id="refreshyes" value="yes"
                    />
                   <label class="form-check-label" for="refreshyes">Yes</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="refreshTokensYesNo"
                    id="refreshno" value="no" checked="checked"
                    />
                    <label class="form-check-label" for="public">No</label>
                </div>
                <div id="lifetimeinput" class="collapse form-check-inline">
                    <input type="number" class="form-control" id="inputRtLifetime"
                           name="${rtLifetime}" value="${rtLifetimeValue}"
                           placeholder="Lifetime in seconds"/>
                </div>
            </div>
        </div>

        <input type="hidden" id="status" name="${action}" value="${request}"/>

        <div class="row">
            <div class="col-sm-12">
                <p style="color:red"><b>${retryMessage}</b></p>
            </div>
        </div>

        <button type="submit" class="btn btn-primary mb-2">Register Client</button>
    </form>
</div>

<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"
        integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj"
        crossorigin="anonymous"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.bundle.min.js"
        integrity="sha384-1CmrxMRARb6aLqgBO7yyAxTOQE2AKb9GfXnEo760AUcUmFx3ibVJJAzGytlQcNXd"
        crossorigin="anonymous"></script>
<script>
$(document).ready(function() {
    $('input[type=radio][name=refreshTokensYesNo]').prop('disabled', false);
    $('input[type=radio][name=clientIsPublic]').prop('disabled', false);

    $('input[type=radio][name=clientIsPublic]').change(function() {
        if (this.value == "on") {
            $('#allscopes').collapse('hide');
            $('#openidscope').collapse('show');
        } else {
            $('#allscopes').collapse('show');
            $('#openidscope').collapse('hide');
        }
    });

    $('input[type=radio][name=refreshTokensYesNo]').change(function() {
        if (this.value == "yes") {
            $('#lifetimeinput').collapse('show');
        } else {
            $('#lifetimeinput').collapse('hide');
            $('#inputRtLifetime').val('');
        }
    });

    $('form').submit(function() {
        $('input[type=radio][name=refreshTokensYesNo]').prop('disabled', true);
        if ($('input[type=radio][name=clientIsPublic]:checked').val() == 'no') {
            $('input[type=radio][name=clientIsPublic]').prop('disabled', true);
        }
        return true;
    });
});
</script>
</body>
</html>
