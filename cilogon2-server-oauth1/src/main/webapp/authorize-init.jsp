<%--
  User: Jeff Gaynor
  Date: 9/25/11
  Time: 6:42 PM
  Properties supplied:
  * clientName = the name of the client
  * clientHome = the home uri of the client
  * AuthUserName = field name containing the user name on submission
  * AuthPassword = field name containing the user's password on submission
  * retryMessage = message displayed if the login in fails.
  * tokenKey = name of hidden field to pass along the authorizationGrant
  * actionToTake = what action that submitting the form invokes.
  * authorizationGrant = the identifier for this transaction
  * action = name of field containing the action the servlet should take
  * actionOk = content of action field in this case telling the service to continue processing.

--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>MyProxy Delegation Client Authorization Page</title>
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

    <h2>Welcome to the OAuth for MyProxy Client Authorization Page</h2>
    The Client below is requesting access to your account. If you approve,
    please sign in with your username and password.
    <p>

    <form action="${actionToTake}" method="POST">
        <table width="800" cellspacing="0" border="2">
            <tr>
                <td valign="top">
                    <table border=1>
                        <tr valign="top">
                            <th>Client Information</th>
                        </tr>
                        <tr>
                            <td> The client listed below is requesting access to your
                                account. If you approve, please sign in.
                                <br><br>
                                <i>Name:</i> ${clientName}
                                <br>
                                <i>URL:</i> ${clientHome}
                            </td>
                        </tr>
                    </table>
                </td>
                <td valign="top">
                    <table width="350" cellspacing="0" cellpadding="0" align="right" border="0">
                        <tr>
                            <th colspan="2">Sign in</th>
                        </tr>
                        <tr>
                            <td>Username</td>
                            <td><input type="text" size="25" name="${AuthUserName}"/></td>
                        </tr>
                        <tr>
                            <td>Password</td>
                            <td><input type="password" size="25" name="${AuthPassword}"/></td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td><input type="submit" value="Sign In"/></td>
                            <td>
                                <a href="${clientHome}" STYLE="TEXT-DECORATION: NONE"><input type="button"
                                                                                             name="cancel"
                                                                                             value="Cancel"/></a>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2"><b><font color="red">${retryMessage}</font></b></td>
                        </tr>

                    </table>
                    <!-- Close sign in table -->
                    <input type="hidden" id="status" name="${action}"
                           value="${actionOk}"/>
                    <input type="hidden" id="token" name="${tokenKey}" value="${authorizationGrant}"/>
                </td>
            </tr>
        </table>
    </form>
</div>
</body>
</html>
