package com.github.pedroarrudamoreira.vaultage.root.security;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.test.util.TestUtils;

@RunWith(PowerMockRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest({SecurityContextHolder.class})
@PowerMockRunnerDelegate(JUnit4.class)
@PowerMockIgnore({"javax.security.*"})
public class AuthenticationProviderTest {
	
	private static final String USER_ID_FOR_USER_1 = "test1";

	private static final String USER_ID_FOR_USER_2 = "test2";

	private static final AuthenticationProvider impl = new AuthenticationProvider();
	
	@Mock
	private Resource resourceMock;
	
	@Mock
	private SecurityContext securityContextMock;
	
	@Mock
	private Authentication authMock;
	
	@BeforeClass
	public static void setupStatic() {
		TestUtils.prepareMockStatic();
	}
	
	@Before
	public void setup() {
		setupStatic();
		PowerMockito.when(SecurityContextHolder.getContext()).thenReturn(securityContextMock);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void test001_afterProperties() throws Exception {
		Mockito.when(resourceMock.getFile()).thenReturn(
				new File(AuthenticationProviderTest.class.getResource("test-users.json").getFile()));
		impl.setUserConfigFile(resourceMock);
		impl.afterPropertiesSet();
		Map<String, User> users = impl.getUsers();
		Assert.assertEquals(2, users.size());
		User obtainedUser2 = users.get(USER_ID_FOR_USER_2);
		Assert.assertEquals(3100, (int) obtainedUser2.getPort());
		Assert.assertEquals(USER_ID_FOR_USER_2, obtainedUser2.getUserId());
		Assert.assertEquals("example@example.com", users.get(USER_ID_FOR_USER_1).getEmail());
		Assert.assertEquals("test@test.com", ((List<String>)obtainedUser2.getBackupConfig().get("email")).get(0));
	}
	
	@Test
	public void test002_loadUserByUsername_ExistingOne() throws Exception {
		UserDetails springUser = impl.loadUserByUsername(USER_ID_FOR_USER_2);
		Assert.assertEquals("{noop}password", springUser.getPassword());
	}
	
	@Test(expected=UsernameNotFoundException.class)
	public void test003_loadUserByUsername_NonExistingOne() throws Exception {
		impl.loadUserByUsername("non-existing-user");
	}
	
	@Test
	public void test004_getCurrentUser() throws Exception {
		Mockito.when(securityContextMock.getAuthentication()).thenReturn(authMock);
		Mockito.when(authMock.getName()).thenReturn(USER_ID_FOR_USER_2);
		User obtainedUser = impl.getCurrentUser();
		User expectedUser = impl.getUsers().get(USER_ID_FOR_USER_2);
		Assert.assertEquals(expectedUser, obtainedUser);
	}

}
