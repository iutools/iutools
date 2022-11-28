<%@ page contentType="text/html;charset=UTF-8" %>
<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<!--
For some reason, including a DOCTYPE html tag messes up the floating winbox
windows
-->
<%--<!DOCTYPE html>--%>

<html lang="en">

<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <% out.println("<title>InuktiTools: "+pageTitle+"</title>\n"); %>
    <link rel="stylesheet" href="./css/styles.css?v2">
    <link rel="stylesheet" href="./css/design-styles.css">
    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400;1,500;1,600&display=swap" rel="stylesheet">
    <script src="./js/vendors/jquery/jquery-3.3.1.min.js"></script>
    <script src="./js/vendors/jquery/jquery-ui.min.js" type="text/javascript"></script>
    <script src="./js/vendors/jquery/jquery.cookie.js" type="text/javascript"></script>
    <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

    <!-- winbox stuff -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/10.7.2/styles/railscasts.min.css">
    <script src="./js/vendors/winbox/winbox.bundle.js"></script>

    <!-- cookieconsent stuff -->
    <link rel="stylesheet" type="text/css"
          href="./js/vendors/cookieconsent/cookieconsent.min.css"/>
    <script src="./js/vendors/cookieconsent/cookieconsent.min.js"
            data-cfasync="false"></script>
</head>

<body>


<!-- Cookie consent stuff -->
<script src="./js/CookieManager.js"></script>
<script>
    new CookieManager().displayCookieConsent();
</script>

<div id="header" class="header">
  <div id="header_inner">
		<a id="header_title" href="index.html">
			<span>INUKTITOOLS:</span> <span>APPS</span> <span>FOR</span> <span>THE</span> <span>INUKTITUT</span> <span>LANGUAGE</span>
		</a> 
	</div>
</div>

<nav id="main_nav">
  <button class="menu-toggle"><span></span><span></span><span></span></button>
  <ul id="main_nav_menu">
    <li id="home_link"><a href="index.html">Home</a></li>
    <li id="feedback_link"><a target="#iutools_feeback" href="mailto:alaindesilets0@gmail.com;contact@inuktitutcomputing.ca?subject=Inuktitut Tools Feedback">Send Feedback</a></li>
    <li id="other_tools">
      <button id="mnu-other-tools" class="drop-menu-toggle">Other Inuktut Tools</button>
      <ul class="drop-menu">
        <li><a href="worddict.jsp">Inuktitut-English Dictionary</a></li>
        <li><a href="spell.jsp">Spell Checker</a></li>
        <li><a href="search.jsp">Web Search Engine</a></li>
        <li><a href="morpheme_dictionary.jsp">Morpheme Dictionary</a></li>
        <li><a href="gisttext.jsp">Reading Assistant</a></li>
        <li><a href="http://inuktitutcomputing.ca/Transcoder/index.php">Inuktitut Computing Transcoder</a></li>
      </ul>
    </li>
    <li id="alphabet">
        <button id="mnu-alphabet" class="drop-menu-toggle">SYLLABIC</button>
        <ul class="drop-menu">
            <li><a onclick="mainNavController.selectAlphabet('ROMAN')">Roman</a></li>
            <li><a onclick="mainNavController.selectAlphabet('SYLLABIC')">Syllabic</a></li>
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
  In collaboration with:
</div>
<div id="sponsors">
  <a href="https://nrc.canada.ca/en" target="_blank"><img src="imgs/NRC-ID_138x138.jpg" alt="logo of National Research Council Canada - Conseil national de recherches Canada"></a>
  <a href="https://www.pirurvik.ca/" target="_blank"><img src="imgs/Pirurvik_logo_2.jpg" alt="logo of pirurvik"></a>
</div>

<!-- To ensure that the browser remembers the old breakpoints after page
reloading -->
<script>debugger;</script>

<!--
   Load scripts that are common to all page types
-->
<script src="./js/navscript.js"></script>
<script src="./js/vendors/log4javascript.js"></script>
<script src="./js/utils/jsonStringifySafe.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/debug/Debug.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/debug/DebugConfig.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/utils/PlatformDetector.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/utils/CSSUtils.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/RunWhen.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/WidgetController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/FloatingWindowController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>


<!-- This one must be before any other controller -->
<script src="./js/controllers/IUToolsController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script src="./js/controllers/MobileWarningController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/settings/SettingsController.js"></script>
<script src="./js/controllers/navigation/MainNavController.js"></script>
<script src="./js/controllers/worddict/WordEntryData.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="./js/controllers/worddict/WordEntryController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
    var mainNavController = new MainNavController();
    new MobileWarningController().possiblyWarnAgainstMobile();
</script>

<!-- Include the code that creates and configures the controller for this page -->

<!-- DISABLE THIS BIT UNTIL THE PageTemplateControler IS FULLY IMPLEMENTED
<script src="../../js/controllers/common/MainContainerController.js?version=<%= IUTOOLS_JS_VERSION %>></script>
<script>
    var pageTemplateController =
        new PageTemplateControlLer({"linkFeedback", "feedback_link"});
</script>
-->

<% pageContext.include("pages/" + pageName + "/_controller.jsp"); %>
