<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Please wait...</title>
<script type="text/javascript">
	setTimeout(function() {
		document.getElementById('frm1').submit();
	}, 300);
</script>
</head>
<body>
	Please wait, redirecting...
	<form method="get" action="/select-channel" id="frm1">
		<input type="hidden" name="select" value="true" />
	</form>
</body>
</html>