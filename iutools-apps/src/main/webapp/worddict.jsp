<%
    String pageTitle = "Inuktitut-English Dictionary";
    String pageName = "worddict";
    String pageUsage = "This <b>machine-generated</b> dictionary provides information about Inuktitut words. It can also be used to find possible translations for English words";
%>

<%--<%@ include file="pages/common/_pageTemplate.jsp" %>--%>
<jsp:include page="pages/common/_pageTemplate.jsp" >
    <jsp:param name="pageTitle" value="Test JSP page" />
    <jsp:param name="pageName" value="worddict" />
    <jsp:param name="pageUsage" value="This <b>machine-generated</b> dictionary provides information about Inuktitut words. It can also be used to find possible translations for English words" />
</jsp:include>