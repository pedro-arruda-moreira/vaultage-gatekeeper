package com.github.pedroarrudamoreira.vaultage.root.security._2fa.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.ServletContextAware;

import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenManager;
import com.github.pedroarrudamoreira.vaultage.accesscontrol.TokenType;
import com.github.pedroarrudamoreira.vaultage.filter.SwitchingFilter;
import com.github.pedroarrudamoreira.vaultage.root.email.service.EmailService;
import com.github.pedroarrudamoreira.vaultage.root.security.AuthenticationProvider;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

@Setter
@CommonsLog
public class TwoFactorAuthFilter extends SwitchingFilter implements ServletContextAware {
    static final String CHANNEL_SELECTOR_LOCATION = "/select-channel.jsp";
    static final String EMAIL_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
    static final String EMAIL_PASSWORD_REQUEST_KEY = "email_password";
    static final String ALREADY_VALIDATED_KEY = TwoFactorAuthFilter.class.getName() + ".ALL_OK";
    static final String CHECK_EMAIL_HTML_LOCATION = "/2fa/email/check_email.jsp";
    static final String PASSWORD_HTML_LOCATION = "/2fa/email/password.jsp";
    static final String SUBJECT = "Login Attempt from Vaultage";

    public static final String EMAIL_TOKEN_KEY = "email_token";
    static final String EMAIL_SENT_KEY = "__EMAIL_SENT_%%$$";

    private EmailService emailService;

    private AuthenticationProvider authProvider;

    private String thisServerHost;

    private ServletContext servletContext;

    @Autowired
    private TokenManager tokenManager;

    @Autowired
    private EventLoop eventLoop;


    @Override
    protected void doFilterImpl(HttpServletRequest request, HttpServletResponse response,
                                FilterChain chain) throws IOException, ServletException {
        SecurityContext context = ObjectFactory.invokeStatic(SecurityContextHolder.class, "getContext");
        Authentication authentication = context.getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            chain.doFilter(request, response);
            return;
        }
        if (!emailService.isEnabled()) {
            throw new IllegalStateException("cannot send emails because e-mail service"
                    + " is not enabled. Please enable it and try again.");
        }
        HttpSession httpSession = request.getSession();
        if (httpSession.getAttribute(ALREADY_VALIDATED_KEY) != null) {
            chain.doFilter(request, response);
            return;
        }
        String receivedToken = request.getParameter(EMAIL_TOKEN_KEY);
        if (tokenManager.isTokenValid(receivedToken, TokenType.SESSION) &&
                tokenManager.removeToken(receivedToken)) {
            httpSession.setAttribute(ALREADY_VALIDATED_KEY, ObjectFactory.PRESENT);
            this.servletContext.getRequestDispatcher(
                    CHANNEL_SELECTOR_LOCATION).forward(request, response);
            return;
        }
        if (!validateEmailPassword(response, request)) {
            return;
        }


        doSendMail(request, response, httpSession);

    }

    private void doSendMail(HttpServletRequest request, ServletResponse response,
                            HttpSession httpSession)
            throws IOException, ServletException {

        boolean mustSendEmail = false;
        String token = null;

        synchronized (httpSession) {
            if (httpSession.getAttribute(EMAIL_SENT_KEY) == null) {
                mustSendEmail = true;
                token = tokenManager.generateNewToken(TokenType.SESSION);
                httpSession.setAttribute(EMAIL_SENT_KEY, ObjectFactory.PRESENT);
            }
        }
        if(mustSendEmail) {
            final String finalToken = token;
            String formAction = formatFormAction(request);
            String emailAddr = authProvider.getCurrentUser().getEmail();
            eventLoop.execute(() -> {
                try {
                    String emailContent = extractEmailContent(finalToken, request, formAction);

                    emailService.sendEmail(emailAddr, SUBJECT, emailContent, null);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }

        servletContext.getRequestDispatcher(CHECK_EMAIL_HTML_LOCATION).forward(request, response);

    }

    private String extractEmailContent(String token, HttpServletRequest request, String formAction)
            throws ServletException, IOException {
        @Cleanup InputStream htmlStream = TwoFactorAuthFilter.class.getResourceAsStream("email_template.html");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(htmlStream));
        String line = null;
        StringBuilder bld = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            bld.append(line).append('\n');
        }
        return String.format(bld.toString(), formAction, token);
    }

    private String formatFormAction(HttpServletRequest request) {
        return String.format("http%s://%s",
                (request.isSecure() ? "s" : StringUtils.EMPTY),
                thisServerHost);
    }

    private boolean validateEmailPassword(ServletResponse response, HttpServletRequest request)
            throws ServletException, IOException {
        if (!emailService.isAuthenticationConfigured()) {
            String emailPass = request.getParameter(EMAIL_PASSWORD_REQUEST_KEY);
            if (emailPass == null) {
                servletContext.getRequestDispatcher(PASSWORD_HTML_LOCATION).forward(request, response);
                return false;
            } else {
                emailService.setPassword(emailPass);
            }
        }
        return true;
    }
}
