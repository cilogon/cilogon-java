<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isErrorPage="true" %>
<%@ page import="java.io.*" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--
    By Terry Fleury. If set as an error page in the web.xml file,
    it will catch any error and process it.
--%>

<html>
<head>
<title>
<%= application.getServerInfo() %> - Error
</title>
</head>

<body>
<%
    ErrorData ed = null;
    String erout = "";
    if (pageContext != null) {
        try {
            ed = pageContext.getErrorData();
        } catch (NullPointerException e) {
            // If the error page was accessed directly, a NullPointerException
            // is thrown at (PageContext.java:514). So catch it and ignore it.
            // It effectively means we can't use the ErrorData.
        }
    }

    if (ed != null) {
        erout += "Error : " + ed.getStatusCode() + "\n";
        request.setAttribute("erout", erout);
    }
%>

<pre><c:out value="${erout}" default="No information about this error was available."/></pre>

</body>
</html>
