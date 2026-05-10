import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: "http://localhost:8080",
  realm: "travel-agency",
  clientId: "travel-frontend",
});

export default keycloak;
