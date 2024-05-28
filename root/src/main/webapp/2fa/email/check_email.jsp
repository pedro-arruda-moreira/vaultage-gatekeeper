<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="com.github.pedroarrudamoreira.vaultage.root.redirector.servlet.ChannelDecidingServlet"%>
<%@page import="org.springframework.web.context.ContextLoaderListener"%>
<%@page import="org.springframework.context.ApplicationContext"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="ISO-8859-1"/>
	<%
		ApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
		ChannelDecidingServlet decidingServlet = context.getBean(ChannelDecidingServlet.class);
		boolean isMobile = decidingServlet.isMobileDevice(request);
		if(isMobile) { %>
			<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
			<style type="text/css">
				input[type="submit"] {
					width: 100px !important;
					height: 45px !important;
				}
			</style>
	<%  } %>
	<style type="text/css">
		input:not([type="submit"]) {
			width: 300px !important;
		}
	</style>
	<title>Check your configured e-mail.</title>
</head>
<body>
	<span>Please check your configured e-mail for instructions.</span><br/>
	<br/>
	<span>If you copied the token, paste it here:</span>
	<form method="get" action="/select-channel/" autocomplete="off">
		<input type="text" name="email_token"
			value="" placeholder="put token here" />
		<% if(isMobile) { %>
			<br/>
		<% } %>
		<input type="submit" value="check token"/>
	</form>
</body>
</html>