# FROM jboss/keycloak
FROM quay.io/keycloak/keycloak:16.1.1

COPY /providers.dasniko /opt/jboss/keycloak/providers
