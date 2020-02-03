<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
          integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">

    <title>CILogon Administrative Client Registration</title>
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

    <h2>CILogon Administrative Client Registration</h2>

    <p>
        Please fill out the form below to register an administrative
        client for use with CILogon.
    </p>
    <h4>Your request will be manually evaluated for approval.</h4>
    <p>
        For more information, please see the
        <a href="http://grid.ncsa.illinois.edu/myproxy/oauth/server/manuals/administrative-clients.xhtml"
           target="_blank">Administrative Clients</a> documentation.
    </p>

    <hr/>

    <form action="${actionToTake}" method="post">

        <div class="form-group row">
            <label for="inputClientName" class="col-sm-2 col-form-label">Client
                Name</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="inputClientName"
                       name="${clientName}" value="${clientNameValue}" required
                       aria-describedBy="clientNameHelp"
                       placeholder="Name of your admin client"/>
                <small id="clientNameHelp" class="form-text text-muted">The Client
                    Name is a user-friendly name for your administrative client.
                </small>
            </div>
        </div>

        <div class="form-group row">
            <label for="inputContactEmail" class="col-sm-2 col-form-label">Contact
                Email</label>
            <div class="col-sm-10">
                <input type="email" class="form-control" id="inputContactEmail"
                       name="${clientEmail}" value="${clientEmailValue}" required
                       aria-describedBy="contactEmailHelp"
                       placeholder="Your email address"/>
                <small id="contactEmailHelp" class="form-text text-muted">A client
                    approval email will be sent to this address.
                </small>
            </div>
        </div>

        <div class="form-group row">
            <label for="inputIssuer" class="col-sm-2 col-form-label">Issuer</label>
            <div class="col-sm-10">
                <input type="url" class="form-control" id="inputIssuer"
                       name="${issuer}" value="${issuerValue}"
                       aria-describedBy="issuerHelp"
                       placeholder="(Optional) The issuser (iss) for the response"/>
                <small id="issuerHelp" class="form-text text-muted">Leave
                    blank to use the default issuer value.
                </small>
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

</body>
</html>

