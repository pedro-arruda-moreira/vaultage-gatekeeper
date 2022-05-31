<%@page import="com.github.pedroarrudamoreira.vaultage.pwa.security.service.TokenService"%>
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
	}, 300);
</script>
</head>
<body>
	Taking you to the PWA now.
	<form method="get" action="/pwa" id="frm1">
		<input type="hidden" name="use_basic"
			value="<%=request.getAttribute(TokenService.USE_BASIC) %>" />
		<input type="hidden" name="self_contained" value="true" />
		<input type="hidden" name="<%=TokenService.CRYPTO_TYPE %>"
			value="<%=request.getAttribute(TokenService.CRYPTO_TYPE) %>" />
		<input type="hidden" name="desktop"
			value="<%=request.getParameter("desktop") %>" />
		<input type="hidden" name="<%=TokenService.CONFIG_CACHE %>"
			value="<%=request.getAttribute(TokenService.CONFIG_CACHE) %>" />
		<input type="hidden" name="<%=TokenService.AUTO_CREATE %>"
			value="<%=request.getAttribute(TokenService.AUTO_CREATE) %>" />
	</form>
</body>
</html>