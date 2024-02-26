package com.github.pedroarrudamoreira.vaultage.root.redirector.filter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.SessionController;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SessionController.class})
public class PreAuthFilterTest {
	
	private PreAuthFilter unit = new PreAuthFilter();
	
	@Mock
	private HttpServletResponse responseMock;

	@Mock
	private SessionController sessionController;
	
	@Mock
	private HttpServletRequest requestMock;
	
	@Mock
	private FilterChain filterChainMock;
	
	@Mock
	private ServletContext servletContextMock;
	
	@Mock
	private RequestDispatcher requestDispatcherMock;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.doPrepareForTest();
	}
	
	@Before
	public void setup() {
		setupStatic();
		unit.setSessionController(sessionController);
		Mockito.when(requestMock.getServletContext()).thenReturn(servletContextMock);
		Mockito.when(servletContextMock.getRequestDispatcher("/select-channel.jsp")).thenReturn(requestDispatcherMock);
	}
	
	@Test
	public void testFirstAccess() throws Exception {
		PowerMockito.when(sessionController.getOriginalUrl()).thenReturn("/");
		Mockito.when(requestMock.getParameter("cli")).thenReturn(null);
		unit.doFilter(requestMock, responseMock, filterChainMock);
		Mockito.verify(requestDispatcherMock).forward(requestMock, responseMock);
	}
	
	@Test
	public void testCliTrue() throws Exception {
		PowerMockito.when(sessionController.getOriginalUrl()).thenReturn("/");
		Mockito.when(requestMock.getParameter("cli")).thenReturn("true");
		unit.doFilter(requestMock, responseMock, filterChainMock);
		Mockito.verify(responseMock, Mockito.never()).sendRedirect("/select-channel");
		Mockito.verify(filterChainMock).doFilter(requestMock, responseMock);
	}
	
	@Test
	public void testOtherContext() throws Exception {
		PowerMockito.when(sessionController.getOriginalUrl()).thenReturn("/something");
		unit.doFilter(requestMock, responseMock, filterChainMock);
		Mockito.verify(responseMock, Mockito.never()).sendRedirect("/select-channel");
		Mockito.verify(filterChainMock).doFilter(requestMock, responseMock);
	}

}
