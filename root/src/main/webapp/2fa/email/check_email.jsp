<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1"/>
<title>Check your configured e-mail.</title>
</head>
<body>
	Please check your configured e-mail for instructions.<br/>
	<br/>
	If you copied the token, paste it here:
	<form method="get" action="/select-channel/" autocomplete="off">
		<input type="text" name="email_token"
			value="" placeholder="put token here" />
		<input type="submit" value="check token"/>
	</form>
</body>
</html>