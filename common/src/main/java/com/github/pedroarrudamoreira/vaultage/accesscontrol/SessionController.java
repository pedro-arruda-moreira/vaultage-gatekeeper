package com.github.pedroarrudamoreira.vaultage.accesscontrol;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.springframework.cglib.proxy.UndeclaredThrowableException;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

public class SessionController implements HttpSessionListener, ServletContextAware {
	
	private static final int ONE_HOUR = 60 * 60 * 1000;

	private static int maxSessionsPerHour = -1;
	
	private static int maxSessionsPerDay = -1;
	
	private static final AtomicInteger REMAINING_HOUR_SESSIONS = ObjectFactory.buildAtomicInteger(500);
	
	private static final AtomicInteger REMAINING_DAY_SESSIONS = ObjectFactory.buildAtomicInteger(900);
	
	static {
		Thread cleanThread = ObjectFactory.buildThread(() -> {
			int count = 0;
			while (true) {
				try {
					ThreadControl.sleep(ONE_HOUR);
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
	
	public void setMaxSessionsPerDay(int maxSessionsPerDay) {
		if(SessionController.maxSessionsPerDay != -1) {
			return;
		}
		REMAINING_DAY_SESSIONS.set(maxSessionsPerDay);
		SessionController.maxSessionsPerDay = maxSessionsPerDay;
	}
	
	public void setMaxSessionsPerHour(int maxSessionsPerHour) {
		if(SessionController.maxSessionsPerHour != -1) {
			return;
		}
		REMAINING_HOUR_SESSIONS.set(maxSessionsPerHour);
		SessionController.maxSessionsPerHour = maxSessionsPerHour;
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		int daySessions = REMAINING_DAY_SESSIONS.addAndGet(-1);
		int hourSessions = REMAINING_HOUR_SESSIONS.addAndGet(-1);
		
		if(daySessions < 0 || hourSessions < 0) {
			se.getSession().invalidate();
			try {
				throw new IllegalAccessException();
			} catch (IllegalAccessException e) {
				throw new UndeclaredThrowableException(e);
			}
		}
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		servletContext.addListener(SessionController.class);
	}

}
