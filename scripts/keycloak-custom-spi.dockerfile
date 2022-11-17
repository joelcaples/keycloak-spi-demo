# FROM jboss/keycloak
# FROM quay.io/keycloak/keycloak:16.1.1
# FROM jboss/keycloak:16.1.1
FROM jboss/keycloak:16.1.1

# COPY /themes/ltione /opt/jboss/keycloak/themes/ltione
COPY /themes/login-sms /opt/jboss/keycloak/themes/login-sms
COPY /providers /opt/jboss/keycloak/providers
