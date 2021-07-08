<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<html lang="en">

<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">  
  <% out.println("<title>iutools: "+pageTitle+"</title>\n"); %>
  <link rel="stylesheet" href="css/styles.css?v2">
  <link rel="stylesheet" href="css/design-styles.css">
  <link rel="preconnect" href="https://fonts.gstatic.com">
  <link href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400;1,500;1,600&display=swap" rel="stylesheet">
  <script src="js/vendors/jquery/jquery-3.3.1.min.js"></script>
</head>

<body>

<div id="header" class="header">
  <div id="header_inner">
    <a href="index.html">Computer Resources for Inuktut</a> 
  </div>
</div>

<nav id="main_nav">
  <button class="menu-toggle"><span></span><span></span><span></span></button>
  <ul id="main_nav_menu">
    <li id="home_link"><a href="index.html">Home</a></li>
    <li id="feedback_link"><a href="mailto:alain.desilets@nrc-cnrc.gc.ca;contact@inuktitutcomputing.ca?subject=Inuktitut Tools Feedback">Send Feedback</a></li>
    <li id="other_tools">
      <button class="drop-menu-toggle">Other Inuktut Tools</button>
      <ul class="drop-menu">
        <li><a href="morpheme_examples.jsp">Morpheme Examples</a></li>
        <li><a href="gisttext.jsp">Gister</a></li>
        <li><a href="spell.jsp">Spell Checker</a></li>
        <li><a href="concordancer.jsp">Inuktut Multilingual Concordancer</a></li>
        <li><a href="search.jsp">Web Search Engine</a></li>
        <li><a href="http://inuktitutcomputing.ca/Transcoder/index.php">Inuktitut Computing Transcoder</a></li>
      </ul>
    </li>
  </ul>
</nav>

<div id="page_title">
<h1><%= pageTitle %></h1>
<p><em><%= pageUsage %></em></p>
</div>
<!--
   Setup the "main" part for this type of page
-->
<main>
<% pageContext.include("pages/" + pageName + "/_view.jsp"); %>
</main>

<div id="footer" class="footer">
  Copyright, National Research Council of Canada, 2017
</div>
<div id="sponsors">
  <a href="https://nrc.canada.ca/en" target="_blank"><img src="imgs/NRC-ID_138x138.jpg" alt="logo of National Research Council Canada - Conseil national de recherches Canada"></a>
  <a href="https://www.pirurvik.ca/" target="_blank"><img src="imgs/Pirurvik_logo_2.jpg" alt="logo of pirurvik"></a>
</div>

<!--
   Load scripts that are common to all page types
-->
<script src="js/navscript.js"></script>
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