import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import keycloak from "./keycloak.js";
import "./index.css";
import App from "./App.jsx";

keycloak
  .init({ onLoad: "check-sso", pkceMethod: "S256" })
  .then(() => {
    createRoot(document.getElementById("root")).render(
      <StrictMode>
        <App />
      </StrictMode>,
    );
  })
  .catch(() => {
    console.error("Error al inicializar Keycloak");
  });
