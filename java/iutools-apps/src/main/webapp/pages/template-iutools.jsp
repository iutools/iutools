<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>

	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css" 
		integrity="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7" crossorigin="anonymous">
	
	<link rel="stylesheet" href="resources/styles/styles.css?version=1.0001"/> <!-- update the version number each time the css file is modified - ensures browsers use the new css file instead of a cached version -->
		
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content = "IE=edge"> <!-- stops IE from using compatibility mode -->
	<meta name = "viewport" content = "width=device-width, initial-scale=1">
		
	<!-- Bootstrap uses HTML5 elements and CSS properties that IE doesn't use-->
	<!-- [if lt IE 9] >
		<script src = "https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
		<script src = "https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
	<![endif]-->

<title>${param.title}</title>

</head>
<body>

<jsp:include page="header.jsp"/>

<p/>

<div id="container">

	<div id="left"> 
		<jsp:include page="left-pane.jsp"/>
	</div>
	
    <div id="right">
		<h1 class="col-xs-12 text-center">${param.title}</h1>

		<jsp:include page="/WEB-INF/pages/${param.content}.jsp"/>
		
		<p/>

		<em><jsp:include page="footer.jsp"/></em>
    </div>    
</div>
  
<script src = "https://code.jquery.com/jquery-2.1.4.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js" 
	integrity="sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS" crossorigin="anonymous"></script>		
  

</body>
</html>