<%--
  User: Jeff Gaynor
  Date: 9/27/11
  Time: 4:37 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<title>CILogon2 delegation request</title>
<link rel="stylesheet" type="text/css" media="all"
      href="static/cilogon.css"/>
<head><title>CILogon2 delegation request</title></head>
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

    <p>Success!<br><br>
        The redirect uri is<br><br> <font font="modern, arial, veranda"> <A href="${redirectUrl}">${redirectUrl}</A></font>
        <br><br>
        Click to go there!
     </p>

    <ul>
    <li><a href="javascript:unhide('showCert');">Show/Hide private key</a></li>
    <div id="showCert" class="hidden">
        <p>
        <pre>${privateKey}</pre>
    </div>
</ul>

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