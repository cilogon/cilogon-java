<%--
  User: Jeff Gaynor
  Date: 9/27/11
  Time: 4:58 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>CILogon 2 success page.</title>
    <link rel="stylesheet"
          type="text/css"
          media="all"
          href="static/cilogon.css"/>
</head>

<body>

<div id="topimgfill">
    <div id="topimg"/>
</div>

<br clear="all"/>

<div class="main">
    <p><b>Success!</b><br><br> The subject of the cert is<br><br> ${certSubject}
        <br><br>and the user id found for this request was <b>${username}</b>

    <form name="input" action="${action}" method="get"/>
    <input type="submit" value="Return to client"/>
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
</html>