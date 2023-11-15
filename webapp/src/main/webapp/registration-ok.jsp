<!doctype html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

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

    <title>Registration Successful</title>
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

    <h2>Registration Successful!</h2>

    <p>
      An administrator will contact you once your registration request
      has been approved. <b>You cannot use your client until it has
      been approved.</b> While waiting for approval, please
      <a target="_blank" href="https://www.cilogon.org/service/outages">join
      the CILogon mailing list</a> to be notified of service updates and
      outages.
    </p>

    <p>
      IMPORTANT: It is your responsibility to store the client
      identifier and secret. Your OIDC/OAuth 2.0 client will need to use
      these values to identify itself. Please keep them in a safe location.
      If you lose the secret, you will need to re-regisiter your client.
      Be sure you copy/paste the values without line breaks
      (which some browsers may insert) or your client may not work.
    </p>

    <hr />

    <form>

      <div class="form-group row">
        <label for="inputID" class="col-sm-2
	  col-form-label">Client Identifier</label>
	<div class="col-sm-9">
	  <textarea class="form-control" id="inputID" name="clientID"
	    rows="2" readonly>${client.identifier}</textarea>
	</div>
	<div class="col-sm-1">
	  <button type="button" class="btn btn-primary mb-2" data-clipboard-target="#inputID">Copy</button>
	</div>
      </div>

      <div class="form-group row">
        <label for="inputSecret" class="col-sm-2
	  col-form-label">Client Secret</label>
	<div class="col-sm-9">
	  <textarea class="form-control" id="inputSecret"
	    name="clientSecret" rows="2"
	    readonly>${client.secret}</textarea>
	</div>
	<div class="col-sm-1">
	  <button type="button" class="btn btn-primary mb-2" data-clipboard-target="#inputSecret">Copy</button>
	</div>
      </div>

      <div class="row">
        <div class="col-sm-12">
          <a href="/oauth2/register">Register another client</a>
        </div>
      </div>

    </form>

</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.10/clipboard.min.js"></script>
<script>
    var clipboard = new ClipboardJS('.btn');
    clipboard.on('success', function(e) {
        console.log(e);
    });
    clipboard.on('error', function(e) {
        console.log(e);
    });
</script>

</body>
</html>
