package lti;

// import java.io.ObjectInputFilter.Config;

// import org.keycloak.Config;
// import org.keycloak.models.KeycloakSession;
// import org.keycloak.models.KeycloakSessionFactory;
// import org.keycloak.theme.ThemeSelectorProvider;
// import org.keycloak.theme.ThemeSelectorProviderFactory;

// import org.keycloak.Config;
// import org.keycloak.authentication.Authenticator;
// import org.keycloak.authentication.AuthenticatorFactory;
// import org.keycloak.models.AuthenticationExecutionModel;
// import org.keycloak.models.KeycloakSession;
// import org.keycloak.models.KeycloakSessionFactory;
// import org.keycloak.provider.ProviderConfigProperty;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.theme.ThemeSelectorProvider;
import org.keycloak.theme.ThemeSelectorProviderFactory;

public class MyThemeSelectorProviderFactory implements ThemeSelectorProviderFactory {

    @Override
    public ThemeSelectorProvider create(KeycloakSession session) {
        return new MyThemeSelectorProvider(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "myThemeSelector";
    }
}