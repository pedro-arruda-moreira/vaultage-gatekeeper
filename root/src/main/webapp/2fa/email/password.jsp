<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1"/>
<title>E-mail password required.</title>
</head>
<body>
	Please, insert the password for your configured e-mail address:<br/>
	<form method="post" action="/select-channel/" autocomplete="off">
		<input type="password" name="email_password"/>
		<input type="submit" value="Ok"/>
	</form>
</body>
</html>