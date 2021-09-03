package com.github.pedroarrudamoreira.vaultage.root.boot.listener;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
@CommonsLog
public class Starter implements ServletContextAware, DisposableBean {
	@Setter
	private AuthenticationProvider userProvider;

	private volatile boolean online = true;

	private static enum SystemStatus {
		ONLINE, START_VAULTAGE_SERVER, RESTART_VAULTAGE_SERVER, SHUTTING_DOWN;
	}

	private ExecutorService vaultageServerExecutor;
	
	private final AtomicInteger processCount = new AtomicInteger();

	@Override
	public void setServletContext(ServletContext servletContext) {
		Map<String, User> users = userProvider.getUsers();
		int threadCount = users.size() * 3;
		vaultageServerExecutor = RootObjectFactory.buildExecutorService(threadCount, threadCount, 10,
				"vaultage-server controlling thread %d");
		doConfigureServersAndRedirects(servletContext, users);
	}


	private void doConfigureServersAndRedirects(ServletContext servletContext, Map<String, User> users) {
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
					Process vaultageServer = null;
					int[] port = new int[1];
					String[] token = new String[1];
					while (true) {
						try {
							SystemStatus status = obtainSystemStatus(vaultageServer);
							switch (status) {
							case SHUTTING_DOWN:
								shutDownServer(port[0], token[0]);
								processCount.decrementAndGet();
								return;
							case RESTART_VAULTAGE_SERVER:
								log.warn("vaultage-server process has terminated and will be restarted.");
							case START_VAULTAGE_SERVER:
								vaultageServer = doStartServer(user, port, token);
							case ONLINE:
								ThreadControl.sleep(500 * users.size());
							}

						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}
					}
				});
			});
		});
	}


	private void shutDownServer(int port, String token) {
		// TODO Auto-generated method stub
		
	}


	private Process doStartServer(User user, int[] port, String[] token) throws Exception {
		Process vaultageServer;
		vaultageServer = ProcessSpawner.executeProcess(
				(line) -> {
					String[] spl = line.split("/");
					port[0] = Integer.parseInt(spl[0]);
					token[0] = spl[1];
				},
				vaultageServerExecutor,
				"vaultage-server",
				"-p",
				user.getPort().toString(),
				"-d",
				user.getDataDir(),
				"-l",
				"127.0.0.1");
		return vaultageServer;
	}


	private SystemStatus obtainSystemStatus(Process vaultageServer) {
		if(online) {
			if(vaultageServer == null) {
				processCount.incrementAndGet();
				return SystemStatus.START_VAULTAGE_SERVER;
			} else if(!vaultageServer.isAlive()) {
				return SystemStatus.RESTART_VAULTAGE_SERVER;
			}
			return SystemStatus.ONLINE;
		}
		return SystemStatus.SHUTTING_DOWN;
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
	public void destroy() throws Exception {
		online = false;
		while(processCount.get() > 0) {
			ThreadControl.sleep(200);
		}
		vaultageServerExecutor.shutdown();
	}

}
