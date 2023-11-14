<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <!-- Bootstrap CSS -->
    <link rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css"
          integrity="sha384-B0vP5xmATw1+K9KRQjQERJvTumQW0nPEzvF6L/Z6nronJ3oUOFUFpCjEUQouq2+l"
          crossorigin="anonymous">

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
                       name="${clientName}" value="${fn:escapeXml(clientNameValue)}" required
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
                       name="${clientEmail}" value="${fn:escapeXml(clientEmailValue)}" required
                       aria-describedBy="contactEmailHelp"
                       placeholder="Your official university/organization email address"/>
                <small id="contactEmailHelp" class="form-text text-muted">This
                    email address is used for operational notices regarding your
                    client and for validating your organizational (e.g., university)
                    affiliation. You will receive a message at this address when
                    your client is approved. The use of a mailing list address for
                    your operations team is recommended.
                </small>
            </div>
        </div>

        <!--
        <div class="form-group row">
            <label for="inputIssuer" class="col-sm-2 col-form-label">Issuer</label>
            <div class="col-sm-10">
                <input type="url" class="form-control" id="inputIssuer"
                       name="${issuer}" value="${fn:escapeXml(issuerValue)}"
                       aria-describedBy="issuerHelp"
                       placeholder="(Optional) The issuser (iss) for the response"/>
                <small id="issuerHelp" class="form-text text-muted">Leave
                    blank to use the default issuer value.
                </small>
            </div>
        </div>
        -->

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

