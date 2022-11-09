package lti;

// import org.keycloak.theme.Theme.Type;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeSelectorProvider;

// import org.keycloak.Config;
// import org.keycloak.authentication.Authenticator;
// import org.keycloak.authentication.AuthenticatorFactory;
// import org.keycloak.models.AuthenticationExecutionModel;
// import org.keycloak.models.KeycloakSession;
// import org.keycloak.models.KeycloakSessionFactory;
// import org.keycloak.provider.ProviderConfigProperty;

public class MyThemeSelectorProvider implements ThemeSelectorProvider {

    public MyThemeSelectorProvider(KeycloakSession session) {
    }


    @Override
    public String getThemeName(Theme.Type type) {
        return "my-theme";
    }

    @Override
        public void close() {
    }


    // @Override
    // public String getThemeName(Type arg0) {
    //     // TO-DO Auto-generated method stub
    //     return null;
    // }
}