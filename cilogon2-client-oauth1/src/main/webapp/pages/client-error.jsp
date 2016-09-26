<%--
  User: Jeff Gaynor
  Date: 9/27/11
  Time: 4:37 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<html>
<link rel="stylesheet" type="text/css" media="all"
      href="static/cilogon.css"/>
<head>
    <title>CILogon2 Client Error Page.</title>
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

    <p>Oh dear...<br><br>
        There was a problem servicing your request.


    <li><a href="javascript:unhide('showStackTrace');">Show/Hide stack trace</a></li>
    <div id="showStackTrace" class="hidden">
        <p>
            Cause = ${cause}<br><br>
            Message = ${message}<br><br>
        <pre> ${stackTrace}</pre>

    </div>
    </ul>
    <br>
    <form name="input" action="${action}" method="get">
        Click to go back to the main page.<br><br><input type="submit" value="Return"/>
    </form>

    <div class="footer">

        <p>
            For questions about this site, please see the
            <a target="_blank" href="http://www.cilogon.org/portal-delegation">Portal
                Delegation FAQ</a> or send email to <a
                href="mailto:help@cilogon.org">help&nbsp;@&nbsp;cilogon.org</a>.
        </p>

        <p>
            This material is based upon work supported by the <a target="_blank"
                                                                 href="http://www.nsf.gov/">National Science
            Foundation</a> under grant
            number <a target="_blank"
                      href="http://www.nsf.gov/awardsearch/showAward.do?AwardNumber=0943633">0943633</a>.
        </p>

        <p>
            Any opinions, findings, and conclusions or recommendations expressed in this
            material are those of the authors and do not necessarily reflect the views
            of the National Science Foundation.
        </p>
    </div>
</div>
</body>


</body>
</html>