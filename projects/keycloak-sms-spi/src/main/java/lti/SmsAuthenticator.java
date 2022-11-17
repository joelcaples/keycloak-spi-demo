package lti;

import lti.gateway.SmsServiceFactory;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.SecretGenerator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.Theme;

import javax.ws.rs.core.Response;
import java.util.Locale;
import org.jboss.logging.Logger;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticator implements Authenticator {

	private static final String TPL_CODE = "login-sms.ftl";
	private static final Logger LOG = Logger.getLogger(SmsAuthenticatorFactory.class);

	@Override
	public void authenticate(AuthenticationFlowContext context) {
		AuthenticatorConfigModel config = context.getAuthenticatorConfig();
		KeycloakSession session = context.getSession();
		UserModel user = context.getUser();

		String mobileNumber = user.getFirstAttribute("mobile_number");
		// mobileNumber of course has to be further validated on proper format, country code, ...

		int length = Integer.parseInt(config.getConfig().get("length"));
		int ttl = Integer.parseInt(config.getConfig().get("ttl"));

		String code = SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		authSession.setAuthNote("code", code);
		authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

		try {
			LOG.warn(String.format("***** Getting Theme... *****"));
			Theme theme = session.theme().getTheme("login-sms", Theme.Type.LOGIN);
			LOG.warn(String.format("*****     Theme Name: %s", theme.getName()));


			// LOG.warn(String.format("***** Getting Theme Alt... *****"));
			// Theme themeAlt = session.theme().getTheme(Theme.Type.LOGIN);
			// LOG.warn(String.format("*****     Theme Alt Name: %s", themeAlt.getName()));


			LOG.warn(String.format("***** Getting Locale... *****"));
			Locale locale = session.getContext().resolveLocale(user);
			LOG.warn(String.format("***** Getting Sms Auth Text... *****"));
			// String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
			String smsAuthText = "AUTH TEXT";
			LOG.warn(String.format("***** Getting Sms Text... *****"));
			LOG.warn(String.format("***** DETAILS:... *****"));
			LOG.warn(String.format("*****     smsAuthText: %s", smsAuthText));
			LOG.warn(String.format("*****     code: %s", code));
			LOG.warn(String.format("*****     ttl: %s", ttl));
			LOG.warn(String.format("*****     locale: %s", locale));
			LOG.warn(String.format("*****     locale display name: %s", locale.getDisplayName()));
			LOG.warn(String.format("*****     locale language: %s", locale.getLanguage()));
			LOG.warn(String.format("*****     theme: %s", theme));
			LOG.warn(String.format("*****     theme name: %s", theme.getName()));
			String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

			LOG.warn(String.format("***** Getting Factory... *****"));
			SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);

			LOG.warn(String.format("***** Creating Form... *****"));
			Response challenge = context.form().createForm("login-sms.ftl");
			// context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TPL_CODE));

			LOG.warn(String.format("***** Challenge... *****"));
			context.challenge(challenge);

		} catch (Exception e) {
			LOG.warn(String.format("***** Exception... *****"));
			LOG.warn(String.format(e.getMessage()));
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().setError("smsAuthSmsNotSent", e.getMessage())
					.createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
		}
	}

	@Override
	public void action(AuthenticationFlowContext context) {
		String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

		AuthenticationSessionModel authSession = context.getAuthenticationSession();
		String code = authSession.getAuthNote("code");
		String ttl = authSession.getAuthNote("ttl");

		if (code == null || ttl == null) {
			context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
				context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
			return;
		}

		boolean isValid = enteredCode.equals(code);
		if (isValid) {
			if (Long.parseLong(ttl) < System.currentTimeMillis()) {
				// expired
				context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
					context.form().setError("smsAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
			} else {
				// valid
				context.success();
			}
		} else {
			// invalid
			AuthenticationExecutionModel execution = context.getExecution();
			if (execution.isRequired()) {
				context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
					context.form().setAttribute("realm", context.getRealm())
						.setError("smsAuthCodeInvalid").createForm(TPL_CODE));
			} else if (execution.isConditional() || execution.isAlternative()) {
				context.attempted();
			}
		}
	}

	@Override
	public boolean requiresUser() {
		return true;
	}

	@Override
	public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
		return user.getFirstAttribute("mobile_number") != null;
	}

	@Override
	public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
		// this will only work if you have the required action from here configured:
		// https://github.com/dasniko/keycloak-extensions-demo/tree/main/requiredaction
		user.addRequiredAction("mobile-number-ra");
	}

	@Override
	public void close() {
	}

}
