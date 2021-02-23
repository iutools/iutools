<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<html lang="en">

<head>
  <meta charset="utf-8">
  <% out.println("<title>iutools: "+pageTitle+"</title>\n"); %>
  <link rel="stylesheet" href="css/styles.css?v1.0.4">
  <script src="js/vendors/jquery/jquery-3.3.1.min.js"></script>
  <script src="js/scripts.js?v1"></script>
</head>

<body>

<div id="header" class="header">
  <a href="index.html">
    <div nrcLogo></div><img src="imgs/NRC-banner.png"/></div>
  </a>

  <p>

  <div id="feedback_link"><a href="mailto:alain.desilets@nrc-cnrc.gc.ca;contact@inuktitutcomputing.ca?subject=Inuktitut Tools Feedback">Send Feedback</a></div>
  <div id="other_tools"><a href="index.html">Other Inuktut Tools</a>
</div>

<p/>

<h1 style="text-align:left;"><%= pageTitle %></h1>
<em><%= pageUsage %></em>
<p/>
<!--
   Setup the "main" part for this type of page
-->
<main>
<% pageContext.include("pages/" + pageName + "/_view.jsp"); %>
</main>

<div id="footer" class="footer">
  <br/>
  <br/>
  Copyright, National Research Council of Canada, 2017
</div>

<!--
   Load scripts that are common to all page types
-->
<script src="js/vendors/log4javascript.js"></script>
<script src="js/debug/Debug.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/debug/DebugConfig.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/RunWhen.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/WidgetController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/IUToolsController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/gist/WordGistController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<!-- Include the code that creates and configures the controller for this page -->
<% pageContext.include("pages/" + pageName + "/_controller.jsp"); %>

</body>

</html>