package lti;

import lti.gateway.SmsServiceFactory;
// import lti.models.TriviaQuestion;
import lti.models.TriviaQuestionResults;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
// import javax.ws.rs.core.GenericType;

// import com.openshift.restclient.ClientBuilder;
// import com.openshift.restclient.IClient;

import javax.ws.rs.core.Response;

// import java.util.List;
import java.util.Locale;
import org.jboss.logging.Logger;
// import javax.ws.rs.core.Response.Status;

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
			LOG.warn(String.format("***** CALLING EXTERNAL API... *****"));
            // String externalApplicationUrl = "https://catfact.ninja/fact";
            String externalApplicationUrl = "https://opentdb.com/api.php?amount=1&category=27&type=multiple";
            // String externalApplicationUrl = "https://api.api-ninjas.com/v1/trivia?category=music";

			// Response response = Response
            //   .status(Status.FOUND)
            //   .header("Location", externalApplicationUrl)
            //   .build();

			// IClient client = new ClientBuilder(externalApplicationUrl).build();
			// Response response = client.get("fact");

			Client client = ClientBuilder.newClient();
			String response = client.target(externalApplicationUrl).request().get(String.class);
			// List<TriviaQuestion> response = client.target(externalApplicationUrl).request()
            //                     // .accept("application/xml")
            //                     .get(new GenericType<List<TriviaQuestion>>() {});
			
			LOG.warn(response.toString());

            ObjectMapper om = new ObjectMapper();
            TriviaQuestionResults data = om.readValue(response, TriviaQuestionResults.class);
            System.out.println(data.results.get(0).question);


			// LOG.warn(response.get(0).category);
			// LOG.warn(response.get(0).question);
			// LOG.warn(response.get(0).correct_answer);
			// LOG.warn(response.get(0).incorrect_answers);

			// LOG.warn(response.readEntity(String.class));
			// LOG.warn(response.getLength());
			// LOG.warn(response.getEntity());
		} catch(Exception e) {
			LOG.warn(String.format("***** Exception... *****"));
			LOG.warn(String.format(e.getMessage()));
		}

		try {
			LOG.warn(String.format("***** Getting Theme... *****"));
			Theme theme = session.theme().getTheme("login-sms", Theme.Type.LOGIN);

			LOG.warn(String.format("*****     Theme Name: %s", theme.getName()));

			LOG.warn(String.format("***** Getting Locale... *****"));
			Locale locale = session.getContext().resolveLocale(user);

			LOG.warn(String.format("***** Getting Sms Auth Text... *****"));
			// String smsAuthText = theme.getMessages(locale).getProperty("smsAuthText");
			String smsAuthText = "AUTH TEXT";

			LOG.warn(String.format("***** DETAILS:... *****"));
			LOG.warn(String.format("*****     smsAuthText: %s", smsAuthText));
			LOG.warn(String.format("*****     code: %s", code));
			LOG.warn(String.format("*****     ttl: %s", ttl));
			LOG.warn(String.format("*****     locale: %s", locale));
			LOG.warn(String.format("*****     locale display name: %s", locale.getDisplayName()));
			LOG.warn(String.format("*****     locale language: %s", locale.getLanguage()));
			LOG.warn(String.format("*****     theme: %s", theme));
			LOG.warn(String.format("*****     theme name: %s", theme.getName()));

			LOG.warn(String.format("***** Getting Sms Text... *****"));
			String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

			LOG.warn(String.format("***** Getting Factory... *****"));
			SmsServiceFactory.get(config.getConfig()).send(mobileNumber, smsText);

			LOG.warn(String.format("***** Creating Form... *****"));
			Response challenge = context.form().createForm(TPL_CODE);
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