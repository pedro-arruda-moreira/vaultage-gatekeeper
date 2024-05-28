<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@page import="com.github.pedroarrudamoreira.vaultage.root.redirector.servlet.ChannelDecidingServlet"%>
<%@page import="com.github.pedroarrudamoreira.vaultage.root.security.model.User"%>
<%@page import="java.util.Map"%>
<%@page import="com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider"%>
<%@ page import="org.springframework.context.ApplicationContext"%>
<%@ page import="org.springframework.web.context.ContextLoaderListener" %>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<title>Select User</title>
<%
	ApplicationContext context = ContextLoaderListener.getCurrentWebApplicationContext();
	ChannelDecidingServlet decidingServlet = context.getBean(ChannelDecidingServlet.class);
	if(decidingServlet.isMobileDevice(request)) { %>
		<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"/>
		<style type="text/css">
			input,span {
				width: 100%;
				height: 200px;
				font-size: x-large;
			}
		</style>
<%  } else { %>
		<style type="text/css">
			input {
				width: 300px;
				height: 70px;
			}
		</style>
<%  } %>
</head>
<body>
<span>Please select your user:</span><br/>
<%
	AuthenticationProvider provider = context.getBean(AuthenticationProvider.class);
	Map<String, User> users = provider.getUsers();
	for(User u : users.values()) { %>
		<form method="post" action="/select-channel.jsp">
			<input type="submit" name="user" value="<%=u.getUserId() %>"/>
			<input type="hidden" name="step" value="login" />
		</form>
		<br/>
<%  } %>

</body>
</html>