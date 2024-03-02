package com.github.pedroarrudamoreira.vaultage.pwa.security.crypto.servlet;

import com.fasterxml.jackson.databind.ObjectReader;
import com.github.pedroarrudamoreira.vaultage.pwa.util.ReaderForSupplier;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@CommonsLog
public class OnlineCryptoServlet extends HttpServlet {
	
	static final String PIN_PARAMETER = "pin";
	static final String CRYPTO_KEY = OnlineCryptoServlet.class.getCanonicalName() + "__CRYPTO%%&&";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final ObjectReader reader = ObjectFactory.fromSupplier(ReaderForSupplier.class, CryptoData.class);
	@Setter
	private int attempts;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		final HttpSession session = req.getSession();
		CryptoData cryptoData = (CryptoData) session.getAttribute(CRYPTO_KEY);
		if(cryptoData == null) {
			log.info(String.format("No crypto key for session %s.", session.getId()));
			resp.sendError(HttpStatus.NOT_FOUND.value());
			return;
		}
		if(!req.getParameter(PIN_PARAMETER).equals(cryptoData.getPin())) {
			final int newAttemptsLeft = cryptoData.getAttemptsLeft() - 1;
			if(newAttemptsLeft < 0) {
				log.info(String.format("deleting crypto key for session %s: too many attempts!",
						session.getId()));
				session.removeAttribute(CRYPTO_KEY);
			} else {
				log.info(String.format("updating crypto key for session %s -> attempts: %d", session.getId(),
						newAttemptsLeft));
				cryptoData.setAttemptsLeft(newAttemptsLeft);
			}
			resp.setStatus(HttpStatus.FORBIDDEN.value());
			resp.getWriter().write(String.valueOf(newAttemptsLeft));
			return;
		}
		resetAttempts(cryptoData, session.getId());
		resp.getWriter().write(cryptoData.getGenKey());
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		HttpSession session = req.getSession();
		synchronized (session) {
			CryptoData cryptoData = reader.readValue(req.getReader());
			if(cryptoData == null || StringUtils.isEmpty(cryptoData.getGenKey())) {
				log.warn(String.format("Attempted to set a empty crypto key for session %s", session.getId()));
				return;
			}
			resetAttempts(cryptoData, session.getId());
			session.setAttribute(CRYPTO_KEY, cryptoData);
		}
	}

	private void resetAttempts(CryptoData cryptoData, String sessId) {
		log.info(String.format("reseting attempts for session %s.", sessId));
		cryptoData.setAttemptsLeft(attempts);
	}

}
