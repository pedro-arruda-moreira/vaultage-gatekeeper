<%@page import="com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Redirecting to mobile app...</title>
<script type="text/javascript">
	setTimeout(function() {
		document.getElementById('frm1').submit();
	}, 2000);
</script>
</head>
<body>
	Please wait, redirecting...
	<form method="post" action="/pwa/auth" id="frm1">
		<input type="hidden" name="type" value="token" />
<%
	String token = TokenManager.generateNewToken();
%>
		<input type="hidden" name="value" value="<%=token%>" />
		<input type="hidden" name="use_basic" value="<%=request.getAttribute("use_basic")%>" />
	</form>
</body>
</html>