<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!doctype html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Login form</title>
<style type="text/css">
	table,tbody,tr,td {
		border: none !important;
	}
	input:not([type="submit"]) {
		width: 300px !important;
	}
	span {
		color: rgb(164, 0, 24);
	}
</style>
</head>
<body>

<form method="post" autocapitalize="none" autocomplete="off" action="/select-channel.jsp">
	<table>
		<tr>
			<td>
				<label for="user">Username:</label>
			</td>
			<td>
				<input type="text" name="user" id="user" placeholder="username"/>
			</td>
		</tr>
		<tr>
			<td>
				<label for="pass">Password:</label>
			</td>
			<td>
				<input type="password" name="pass" id="pass"/>
			</td>
		</tr>
		<tr>
			<td>
				&nbsp;
			</td>
			<td>
				<input type="submit" value="Login"/>
			</td>
		</tr>
		<%
			Enumeration<String> e = request.getParameterNames();
			boolean hasError = false;
			while(e.hasMoreElements()) {
				if("error".equals(e.nextElement())) {
					hasError = true;
					break;
				}
			}
			if(hasError) {
		%>
		<tr>
			<td>
				&nbsp;
			</td>
			<td>
				<span>
					Username and/or password incorrect.<br/>
					Please try again.
				</span>
			</td>
		</tr>
		<%
			}
		%>
	</table>
</form>
</body>
</html>