package com.github.pedroarrudamoreira.vaultage.pwa.security.crypto.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectReader;
import com.github.pedroarrudamoreira.vaultage.pwa.util.PWAObjectFactory;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
	PWAObjectFactory.class,
	LogFactory.class
})
public class OnlineCryptoServletTest {
	
	private static final String GENERATED_KEY = "KEY123";

	private static final String WRONG_PIN = "4321";

	private static final String CORRECT_PIN = "1234";

	private static final int ATTEMPTS = 3;
	
	@Mock
	private ObjectReader readerMock;
	
	@Mock
	private HttpServletRequest reqMock;
	
	@Mock
	private HttpServletResponse resMock;
	
	@Mock
	private HttpSession sessMock;
	
	@Mock
	private BufferedReader mockBuffReader;
	
	@Mock
	private Log logMock;
	
	@Mock
	private PrintWriter writerMock;
	
	private OnlineCryptoServlet impl;
	
	private CryptoData cryptoData;
	
	private String providedPin;

	@BeforeClass
	public static void setupStatic() {
		TestUtils.prepareMockStatic();
	}
	
	@Before
	public void setup() throws IOException {
		setupStatic();
		cryptoData = null;
		providedPin = null;
		PowerMockito.when(PWAObjectFactory.readerFor(CryptoData.class)).thenReturn(readerMock);
		PowerMockito.when(LogFactory.getLog(OnlineCryptoServlet.class)).thenReturn(logMock);
		Mockito.when(readerMock.readValue(Mockito.any(BufferedReader.class))).thenAnswer((i) -> cryptoData);
		Mockito.when(sessMock.getAttribute(OnlineCryptoServlet.CRYPTO_KEY)).thenAnswer((i) -> cryptoData);
		Mockito.when(reqMock.getSession()).thenReturn(sessMock);
		Mockito.when(reqMock.getReader()).thenReturn(mockBuffReader);
		Mockito.when(reqMock.getParameter(OnlineCryptoServlet.PIN_PARAMETER)).thenAnswer((i) -> providedPin);
		Mockito.when(resMock.getWriter()).thenReturn(writerMock);
		impl = new OnlineCryptoServlet();
		impl.setAttempts(ATTEMPTS);
	}
	
	@Test
	public void testSaveCryptoKey() throws Exception {
		cryptoData = new CryptoData();
		cryptoData.setGenKey("aaaa");
		cryptoData.setPin(CORRECT_PIN);
		impl.doPost(reqMock, resMock);
		Assert.assertEquals(ATTEMPTS, cryptoData.getAttemptsLeft());
		Mockito.verify(sessMock).setAttribute(OnlineCryptoServlet.CRYPTO_KEY, cryptoData);
	}
	
	@Test
	public void testSaveCryptoKey_Empty() throws Exception {
		cryptoData = new CryptoData();
		impl.doPost(reqMock, resMock);
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong message.", i.getArgument(0, String.class).contains(
					"No crypto key for"));
			return null;
		}).when(logMock).info(Mockito.any());
		Mockito.verify(sessMock, Mockito.never()).setAttribute(OnlineCryptoServlet.CRYPTO_KEY, cryptoData);
	}
	
	@Test
	public void testNoCryptoKey() throws Exception {
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong message.", i.getArgument(0, String.class).contains(
					"No crypto key for"));
			return null;
		}).when(logMock).info(Mockito.any());
		impl.doGet(reqMock, resMock);
		Mockito.verify(resMock).sendError(HttpStatus.NOT_FOUND.value());
	}
	
	@Test
	public void testWrongPin_attemptsLeft() throws Exception {
		cryptoData = new CryptoData();
		cryptoData.setPin(CORRECT_PIN);
		cryptoData.setAttemptsLeft(ATTEMPTS);
		providedPin = WRONG_PIN;
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong message.", i.getArgument(0, String.class).contains(
					"updating crypto key for session"));
			return null;
		}).when(logMock).info(Mockito.any());
		impl.doGet(reqMock, resMock);
		Mockito.verify(resMock).setStatus(HttpStatus.FORBIDDEN.value());
		Mockito.verify(writerMock).write("2");
		Assert.assertEquals(ATTEMPTS - 1, cryptoData.getAttemptsLeft());
	}
	
	@Test
	public void testWrongPin_noAttemptsLeft() throws Exception {
		cryptoData = new CryptoData();
		cryptoData.setPin(CORRECT_PIN);
		cryptoData.setAttemptsLeft(0);
		providedPin = WRONG_PIN;
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong message.", i.getArgument(0, String.class).contains(
					"deleting crypto key for session"));
			return null;
		}).when(logMock).info(Mockito.any());
		impl.doGet(reqMock, resMock);
		Mockito.verify(resMock).setStatus(HttpStatus.FORBIDDEN.value());
		Mockito.verify(writerMock).write("-1");
		Mockito.verify(sessMock).removeAttribute(OnlineCryptoServlet.CRYPTO_KEY);
		Assert.assertEquals(0, cryptoData.getAttemptsLeft());
	}
	
	@Test
	public void testPinOk() throws Exception {
		cryptoData = new CryptoData();
		cryptoData.setGenKey(GENERATED_KEY);
		cryptoData.setPin(CORRECT_PIN);
		cryptoData.setAttemptsLeft(2);
		providedPin = CORRECT_PIN;
		Mockito.doAnswer((i) -> {
			Assert.assertTrue("wrong message.", i.getArgument(0, String.class).contains(
					"reseting attempts"));
			return null;
		}).when(logMock).info(Mockito.any());
		impl.doGet(reqMock, resMock);
		Mockito.verify(writerMock).write(GENERATED_KEY);
		Assert.assertEquals(ATTEMPTS, cryptoData.getAttemptsLeft());
	}
	
}
