package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class Starter implements ServletContextAware, InitializingBean {
	@Setter
	private AuthenticationProvider userProvider;


	@Override
	public void setServletContext(ServletContext servletContext) {
		Map<String, User> users = userProvider.getUsers();
		int threadCount = users.size() * 3;
		final ExecutorService vaultageServerExecutor = ObjectFactory.createDaemonExecutorService(threadCount, threadCount, 10,
				"vaultage-server controlling thread %d");
		doConfigureServersAndRedirects(servletContext, users, vaultageServerExecutor);
	}


	private void doConfigureServersAndRedirects(ServletContext servletContext, Map<String, User> users,
			final ExecutorService vaultageServerExecutor) {
		ServletRegistration servletRegistration = servletContext.getServletRegistration("proxyServlet");
		users.values().forEach((user) -> {
			servletRegistration.addMapping(String.format("/%s/*", user.getVaultageUsername()));
		});
		vaultageServerExecutor.execute(() -> {
			while (SessionController.getApplicationContext() == null) {
				try {
					ThreadControl.sleep(500);
				} catch (InterruptedException e) {
					log.warn(e.getMessage(), e);
				}
			}
			doValidateUsers(users);
			users.values().forEach((user) -> {
				vaultageServerExecutor.execute(() -> {
					while(true) {
						try {
							ProcessSpawner.executeProcessAndWait(vaultageServerExecutor,
									"vaultage-server",
									"-p",
									user.getPort().toString(),
									"-d",
									user.getDataDir());
							ThreadControl.sleep(5000);
						} catch (Exception e) {
							log.info("vaultage-server process has terminated and will be restarted.");
						}
					}
				});
			});
		});
	}


	private void doValidateUsers(Map<String, User> users) {
		Set<String> dataDirs = new HashSet<>();
		Set<Integer> ports = new HashSet<Integer>();
		for(Entry<String, User> userEntry : users.entrySet()) {
			User user = userEntry.getValue();
			String userId = userEntry.getKey();
			if(!dataDirs.add(user.getDataDir())) {
				throw new IllegalArgumentException(String.format("duplicate paths (detected on user %s)", userId));
			}
			if(!ports.add(user.getPort())) {
				throw new IllegalArgumentException(String.format("duplicate ports (detected on user %s)", userId));
			}
		}
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

}
