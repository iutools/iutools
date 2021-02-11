<% String pageName="spike-somepage"; %>
<% String VERSION="1.0"; %>

<h1>pageContext.include(pageName+".jsp") with setAttribute</h1>
<%
  pageContext.setAttribute("VERSION", VERSION, pageContext.REQUEST_SCOPE);
  pageContext.include(pageName+".jsp");
%>

<%
        if (pageName.equals("spike-somepage")){
            %><%@include file="spike-somepage .jsp"%><%
        } else if ("layout2".equalsIgnoreCase(viewletLayout)){
            %><%@include file="layout2.jsp"%><%
        }
    %>

