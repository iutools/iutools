<% String pageName="spike-somepage"; %>
<% String VERSION="1.0"; %>

<h1>pageContext.include(pageName+".jsp") with setAttribute</h1>
<%
  pageContext.setAttribute("VERSION", VERSION);
  pageContext.include(pageName+".jsp");
%>

<h1>&lt;jsp:include page="spike-somepage.jsp"&gt;, hardcoded VERSION</h1>
<jsp:include page="spike-somepage.jsp">
    <jsp:param name="VERSION" value="2.0"/>
</jsp:include>

<h1>pageContext.include(pageName+".jsp")</h1>
<% pageContext.include(pageName+".jsp"); %>
