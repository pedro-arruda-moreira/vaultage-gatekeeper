<%@page import="com.github.pedroarrudamoreira.vaultage.pwa.security.TokenService"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1"/>
<title>Login completed.</title>
<script type="text/javascript">
setTimeout(function() {
	document.getElementById('frm1').submit();
}, 2000);
</script>
</head>
<body>
Taking you to the PWA now.
<form method="get" action="/pwa" id="frm1">
	<input type="hidden" name="use_basic" value="true" />
	<input type="hidden" name="self_contained" value="true" />
	<input type="hidden" name="crypto_type"
		value="<%=request.getAttribute(TokenService.CRYPTO_TYPE) %>" />
</form>
</body>
</html>