package com.github.pedroarrudamoreira.vaultage.root.security._2fa.filter.util;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class EmailCollector extends HttpServletResponseWrapper {
	private final PrintWriter writer;

	public EmailCollector(ServletResponse response, PrintWriter writer) {
		super((HttpServletResponse) response);
		this.writer = writer;
	}


	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public void addDateHeader(String name, long date) {
		return;
	}
	@Override
	public void addCookie(Cookie cookie) {
		return;
	}
	@Override
	public void addHeader(String name, String value) {
		return;
	}
	@Override
	public void addIntHeader(String name, int value) {
		return;
	}

}
