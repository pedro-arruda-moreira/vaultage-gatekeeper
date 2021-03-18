<%@page import="com.github.pedroarrudamoreira.vaultage.root.filter._2fa.TwoFactorAuthFilter"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<h2>A login attempt has been made on your Vaultage server.</h2><br/>
If you did not attempt to login, <b>change your password ASAP!</b><br/>
Otherwise,
<form method="get" style="display: inline;"
	action="<%=request.getAttribute(TwoFactorAuthFilter.EMAIL_TEMPLATE_SERVER_HOST_KEY)%>">
<input type="submit" value="click here"/>
(using the same browser) to allow.
<br/>
<br/>
Or copy the following token:
<input type="text" readonly="readonly" name="<%=TwoFactorAuthFilter.EMAIL_TOKEN_KEY%>"
	size="50" value="<%= request.getAttribute(TwoFactorAuthFilter.EMAIL_TOKEN_KEY)%>"/>
</form>