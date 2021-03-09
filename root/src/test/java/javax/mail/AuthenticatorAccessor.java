package javax.mail;

public class AuthenticatorAccessor {
	
	private AuthenticatorAccessor() {
		super();
	}
	
	public static PasswordAuthentication access(Authenticator auth) {
		return auth.getPasswordAuthentication();
	}

}
