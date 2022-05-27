<%@page import="com.github.pedroarrudamoreira.vaultage.root.redirector.servlet.ChannelDecidingServlet"%>
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
	}, 300);
</script>
</head>
<body>
	Please wait, redirecting...
	<form method="post" action="/pwa/auth" id="frm1">
		<input type="hidden" name="type" value="token" />
		<input type="hidden" name="value"
			value="<%=request.getAttribute(ChannelDecidingServlet.TOKEN_KEY) %>" />
		<input type="hidden" name="desktop"
			value="<%=request.getAttribute(ChannelDecidingServlet.DESKTOP_MODE_KEY) %>" />
	</form>
</body>
</html>