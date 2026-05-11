import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import keycloak from "./keycloak.js";
import "./index.css";
import App from "./App.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";

keycloak
  .init({
    onLoad: "check-sso",
    checkLoginIframe: false,
    pkceMethod: "S256",
  })
  .then(() => {
    createRoot(document.getElementById("root")).render(
      <StrictMode>
        <AuthProvider>
          <App />
        </AuthProvider>
      </StrictMode>,
    );
  })
  .catch(() => console.error("Error al inicializar Keycloak"));
