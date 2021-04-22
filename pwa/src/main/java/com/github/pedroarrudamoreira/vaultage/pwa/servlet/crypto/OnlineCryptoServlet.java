package com.github.pedroarrudamoreira.vaultage.pwa.servlet.crypto;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import lombok.Setter;

public class OnlineCryptoServlet extends HttpServlet {
	
	private static final String CRYPTO_KEY = OnlineCryptoServlet.class.getCanonicalName() + "__CRYPTO%%&&";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final ObjectReader READER = new ObjectMapper().readerFor(CryptoData.class);
	@Setter
	private int attempts;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final HttpSession session = req.getSession();
		CryptoData cryptoData = (CryptoData) session.getAttribute(CRYPTO_KEY);
		if(cryptoData == null) {
			resp.sendError(404);
			return;
		}
		if(!req.getParameter("pin").equals(cryptoData.getPin())) {
			final int newAttemptsLeft = cryptoData.getAttemptsLeft() - 1;
			if(newAttemptsLeft < 0) {
				session.removeAttribute(CRYPTO_KEY);
			}
			cryptoData.setAttemptsLeft(newAttemptsLeft);
			resp.setStatus(403);
			resp.getWriter().write(String.valueOf(newAttemptsLeft));
			return;
		}
		resetAttempts(cryptoData);
		resp.getWriter().write(cryptoData.getGenKey());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession httpSession = req.getSession();
		synchronized (httpSession) {
			CryptoData cryptoData = READER.readValue(req.getReader());
			resetAttempts(cryptoData);
			httpSession.setAttribute(CRYPTO_KEY, cryptoData);
		}
	}

	private void resetAttempts(CryptoData cryptoData) {
		cryptoData.setAttemptsLeft(attempts);
	}

}
