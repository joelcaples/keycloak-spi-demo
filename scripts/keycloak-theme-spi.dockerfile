# FROM jboss/keycloak
FROM quay.io/keycloak/keycloak:16.1.1

# COPY /themes/ltione /opt/jboss/keycloak/themes/ltione
COPY /providers /opt/jboss/keycloak/providers
