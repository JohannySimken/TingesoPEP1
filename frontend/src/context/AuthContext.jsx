import { useState, useEffect } from "react";
import { AuthContext } from "./authContextConfig";
import keycloak from "../keycloak";
import api from "../api/axios";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (keycloak.authenticated) {
      const profile = keycloak.tokenParsed;
      api
        .post("/users/sync", {
          keycloakId: profile.sub,
          email: profile.email,
          name: profile.name || profile.preferred_username,
        })
        .then(({ data }) => {
          console.log("✅ Sync response:", data); // ← agrega esto
          setUser({
            id: data.id,
            keycloakId: profile.sub,
            name: data.name,
            email: data.email,
            roles: profile.realm_access?.roles || [],
            role: data.role,
          });
        })
        .catch((err) => {
          console.error("❌ Sync error:", err.response?.data); // ← y esto
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = () => {
    keycloak.login();
  };

  const logout = () => {
    keycloak.logout({ redirectUri: "http://localhost:5173/" });
  };

  const getToken = () => {
    return keycloak.token;
  };

  const hasRole = (role) => {
    return (
      user?.roles.includes(role) ||
      user?.roles.includes(role.replace("ROLE_", "")) ||
      user?.role === role.replace("ROLE_", "")
    );
  };

  return (
    <AuthContext.Provider
      value={{ user, loading, login, logout, getToken, hasRole }}
    >
      {children}
    </AuthContext.Provider>
  );
}
