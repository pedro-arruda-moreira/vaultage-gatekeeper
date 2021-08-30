package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

import lombok.Setter;

public class SessionController implements HttpSessionListener, ServletContextAware,
	ServletRequestListener, Filter {
	
	static final String ORIGINAL_URL = ServletRequestListener.class.getCanonicalName()
			+ "_originalRequest";
	
	static final String LOGGED_ON_KEY = "__logged_on__$$";
	
	private static final ThreadLocal<ServletRequest> REQUESTS = new ThreadLocal<>();
	
	private static final String LOGIN_ATTEMPTS_REMAINING = "login_attempts_remaining";

	private static final int ONE_HOUR_MINUTES = 60;
	
	private static final int ONE_HOUR_SECONDS = ONE_HOUR_MINUTES * 60;

	private static final int ONE_HOUR_MILLIS = ONE_HOUR_SECONDS * 1000;
	
	private static final AtomicInteger REMAINING_HOUR_SESSIONS = ObjectFactory.buildAtomicInteger(500);
	
	private static final AtomicInteger REMAINING_DAY_SESSIONS = ObjectFactory.buildAtomicInteger(900);
	
	private static int maxSessionsPerHour = -1;
	
	private static int maxSessionsPerDay = -1;

	private static int maxLoginAttemptsPerSession = -1;
	static {
		Thread cleanThread = ObjectFactory.buildThread(() -> {
			int count = 0;
			while (true) {
				try {
					ThreadControl.sleep(ONE_HOUR_MILLIS);
					REMAINING_HOUR_SESSIONS.set(maxSessionsPerHour);
					count++;
					if(count >= 24) {
						count = 0;
						REMAINING_DAY_SESSIONS.set(maxSessionsPerDay);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}, "vaultage session limiter thread");
		cleanThread.setDaemon(true);
		cleanThread.start();
	}
	
	public static synchronized void setMaxSessionsPerDay(int maxSessionsPerDay) {
		if(SessionController.maxSessionsPerDay != -1) {
			return;
		}
		REMAINING_DAY_SESSIONS.set(maxSessionsPerDay);
		SessionController.maxSessionsPerDay = maxSessionsPerDay;
	}
	
	public static synchronized void setMaxSessionsPerHour(int maxSessionsPerHour) {
		if(SessionController.maxSessionsPerHour != -1) {
			return;
		}
		REMAINING_HOUR_SESSIONS.set(maxSessionsPerHour);
		SessionController.maxSessionsPerHour = maxSessionsPerHour;
	}
	
	public static synchronized void setMaxLoginAttemptsPerSession(int maxLoginRetryPerSession) {
		if(SessionController.maxLoginAttemptsPerSession != -1) {
			return;
		}
		SessionController.maxLoginAttemptsPerSession = maxLoginRetryPerSession;
	}

	
	public static HttpServletRequest getCurrentRequest() {
		return (HttpServletRequest) REQUESTS.get();
	}

	public static String getOriginalUrl() {
		return REQUESTS.get().getAttribute(ORIGINAL_URL).toString();
	}
	
	public static ApplicationContext getApplicationContext() {
		return ContextLoaderListener.getCurrentWebApplicationContext();
	}
	
	@Setter
	private int sessionDurationInHours;
	
	@Setter
	private boolean secure = true;
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		session.setAttribute(LOGIN_ATTEMPTS_REMAINING, new AtomicInteger(maxLoginAttemptsPerSession));
		int daySessions = REMAINING_DAY_SESSIONS.addAndGet(-1);
		int hourSessions = REMAINING_HOUR_SESSIONS.addAndGet(-1);
		
		if(daySessions < 0 || hourSessions < 0) {
			session.invalidate();
			throw new UndeclaredThrowableException(new SecurityException());
		}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		servletContext.addListener(this);
		servletContext.setSessionTimeout(sessionDurationInHours * ONE_HOUR_MINUTES);
		SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
		sessionCookieConfig.setMaxAge(sessionDurationInHours * ONE_HOUR_SECONDS);
		sessionCookieConfig.setSecure(secure);
	}
	
	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		HttpServletRequest request = (HttpServletRequest) sre.getServletRequest();
		request.setAttribute(ORIGINAL_URL, request.getRequestURI());
		REQUESTS.set(request);
		HttpSession session = request.getSession();
		if(secure && !request.isSecure()) {
			session.invalidate();
			throw new UndeclaredThrowableException(new SecurityException());
		}
		if(session.getAttribute(LOGGED_ON_KEY) != null) {
			return;
		}
		AtomicInteger attempts = (AtomicInteger) session.getAttribute(LOGIN_ATTEMPTS_REMAINING);
		if(attempts.addAndGet(-1) < 0) {
			session.invalidate();
		}
	}
	
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		REQUESTS.remove();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		((HttpServletRequest)request).getSession().setAttribute(LOGGED_ON_KEY, ObjectFactory.PRESENT);
		chain.doFilter(request, response);
	}
	
}
