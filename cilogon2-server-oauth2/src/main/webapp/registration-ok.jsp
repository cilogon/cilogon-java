<%--
  User: Jeff Gaynor
  Date: 9/25/11
  Time: 4:26 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<head>
    <title>Registration Successful</title>
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

    <h2>Registration Successful!</h2>

    <p>Here is your client identifier:
        <br><br><b>${client.identifier}</b>
    </p>

    <p>Here is your client secret:
        <br><br><b>${client.secret}</b>
    </p>

    <br><br>IMPORTANT NOTE: It is the client's responsibility to store the identifier and secret.
    Your client will need to use it
    as needed to identify itself. Please keep these in a safe location. If you lose the secret, you will have to
    re-regisiter. Be sure you copy the secret without line breaks (which some browsers will insert) or you will
    get an invalid secret.
    <p>
        An administrator will contact you once your registration request is approved. You cannot use this
        identifier code until you have been approved by the administrator.
    </p>
</div>
</body>

</html>