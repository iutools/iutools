<html lang="en">


<% String pageTitle = "Inuktitut Web Search Engine"; %>
<% String pageName = "search"; %>

<%@ include file="pages/common/_head.jsp" %>

<body>

<%@ include file="pages/common/_top.jsp" %>

<main>
<% pageContext.include("pages/" + pageName + "/_main.jsp"); %>
</main>

<%@ include file="pages/common/_bottom.jsp" %>

<%@ include file="pages/common/_js_includes.jsp" %>

<!--
    For some reason, we can't use the same pageContext.include() trick we
    used above for including the _common_js_includes.jsp file.
-->
<%@ include file="pages/search/_controller_setup.jsp" %>

</body>

</html>