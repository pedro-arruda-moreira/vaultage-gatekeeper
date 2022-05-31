package com.github.pedroarrudamoreira.vaultage.root.server;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.DisposableBean;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ThreadControl;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class VaultageServerManager implements DisposableBean {

	@Setter
	private AuthenticationProvider userProvider;

	private static enum SystemStatus {
		ONLINE, START_VAULTAGE_SERVER, RESTART_VAULTAGE_SERVER, SHUTTING_DOWN;
	}
	
	private final AtomicInteger processCount = new AtomicInteger();

	private volatile boolean online = true;


	public void doStartAndMonitorVaultageServers() {
		userProvider.getUsers().values().forEach((user) -> {
			final int[] port = new int[1];
			final String[] token = new String[1];
			final Process[] vaultageServer = new Process[1];
			EventLoop.repeatTask(() -> {
				try {
					SystemStatus status = obtainSystemStatus(vaultageServer[0]);
					switch (status) {
					case SHUTTING_DOWN:
						shutDownServer(port[0], token[0], vaultageServer[0]);
						processCount.decrementAndGet();
						return false;
					case RESTART_VAULTAGE_SERVER:
						log.warn("vaultage-wrapper process has terminated and will be restarted.");
					case START_VAULTAGE_SERVER:
						vaultageServer[0] = doStartServer(user, port, token);
					case ONLINE:
						break;
					}

				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				return true;
			}, 500, TimeUnit.MILLISECONDS);
		});
	}
	



	private void shutDownServer(int port, String token, Process process) throws Exception {

		HttpPost post = new HttpPost();
		StringEntity entity = new StringEntity(String.format("{\"token\":\"%s\"}", token));
		entity.setContentType("application/json");
		post.setEntity(entity);
		CloseableHttpResponse response = HttpClientBuilder.create().build().execute(new HttpHost("127.0.0.1", port), post);
		InputStream content = response.getEntity().getContent();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(content, baos);
		if(!"OK".equals(new String(baos.toByteArray()))) {
			log.warn(String.format("could not send shutdown command for vaultage server on port %d", port));
		}
		response.close();
		int retVal = process.waitFor();
		if(retVal != 0) {
			log.warn(String.format("could not send shutdown vaultage server on port %d", port));
		} else {
			log.info(String.format("vaultage server on port %d stopped.", port));
		}
	}


	private Process doStartServer(User user, int[] port, String[] token) throws Exception {
		Process vaultageServer;
		vaultageServer = ProcessSpawner.executeProcess(
				(line) -> {
					String[] spl = line.split("/");
					port[0] = Integer.parseInt(spl[0]);
					token[0] = spl[1];
				},
				"vaultage-wrapper",
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

	@Override
	public void destroy() throws Exception {
		online = false;
		while(processCount.get() > 0) {
			ThreadControl.sleep(200);
		}
		ThreadControl.sleep(1000);
	}

}