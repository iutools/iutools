<%--FAILS--%>
<%--<jsp:include page="/pages/common/_pageTemplate.new.jsp" >--%>

<%--OK--%>
<%--<jsp:include page="_includeLevel1.jsp" >--%>

<%--OK--%>
<%--<jsp:include page="/_includeLevel1.jsp" >--%>

<%--OK--%>
<%--<jsp:include page="/pages/_includeLevel1.jsp" >--%>

<%--OK--%>
<%--<jsp:include page="/pages/common/_includeLevel1.jsp" >--%>

<jsp:include page="/pages/common/_pageTemplateNEW.jsp" >
    <jsp:param name="pageTitle" value="Test JSP page" />
    <jsp:param name="pageName" value="worddict" />
    <jsp:param name="pageUsage" value="This <b>machine-generated</b> dictionary provides information about Inuktitut words. It can also be used to find possible translations for English words" />
</jsp:include>

