package lti;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import org.jboss.logging.Logger;
import java.util.List;

/**
 * @author Niko Köbler, https://www.n-k.de, @dasniko
 */
public class SmsAuthenticatorFactory implements AuthenticatorFactory {

	public static final String PROVIDER_ID = "sms-authenticator";
	private static final Logger LOG = Logger.getLogger(SmsAuthenticatorFactory.class);

	@Override
	public String getId() {
		LOG.warn(String.format("***** PROP PROVIDER_ID *****"));
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		LOG.warn(String.format("***** PROP Display Type *****"));
		return "SMS Authentication";
	}

	@Override
	public String getHelpText() {
		LOG.warn(String.format("***** HelpText *****"));
		return "Validates an OTP sent via SMS to the users mobile phone.";
	}

	@Override
	public String getReferenceCategory() {
		return "otp";
	}

	@Override
	public boolean isConfigurable() {
		return true;
	}

	@Override
	public boolean isUserSetupAllowed() {
		return true;
	}

	@Override
	public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
		return REQUIREMENT_CHOICES;
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		LOG.warn(String.format("***** GetConfigProperties *****"));
		return List.of(
			new ProviderConfigProperty("length", "Code length", "The number of digits of the generated code.", ProviderConfigProperty.STRING_TYPE, 6),
			new ProviderConfigProperty("ttl", "Time-to-live", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "300"),
			new ProviderConfigProperty("senderId", "SenderId", "The sender ID is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, "Keycloak"),
			new ProviderConfigProperty("simulation", "Simulation mode", "In simulation mode, the SMS won't be sent, but printed to the server logs", ProviderConfigProperty.BOOLEAN_TYPE, true)
		);
	}

	@Override
	public Authenticator create(KeycloakSession session) {
		return new SmsAuthenticator();
	}

	@Override
	public void init(Config.Scope config) {
		LOG.warn(String.format("***** Init *****"));
	}

	@Override
	public void postInit(KeycloakSessionFactory factory) {
		LOG.warn(String.format("***** PostInit *****"));
	}

	@Override
	public void close() {
		LOG.warn(String.format("***** Close *****"));
	}

}
