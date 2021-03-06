<%@page import="com.github.pedroarrudamoreira.vaultage.root.filter._2fa.TwoFactorAuthFilter"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<h2>A login attempt has been made on your Vaultage server.</h2><br/>
If you did not attempt to login, <b>change your password ASAP!</b><br/>
Otherwise,
<a href="<%=request.getAttribute(TwoFactorAuthFilter.LINK_REQUEST_KEY) %>"><b>click here</b></a>
(using the same browser) to allow.