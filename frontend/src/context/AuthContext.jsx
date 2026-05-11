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
      console.log("🔑 ROLES EN TOKEN:", profile.realm_access);
      const keycloakRoles = profile.realm_access?.roles || [];
      api
        .post("/users/sync", {
          keycloakId: profile.sub,
          email: profile.email,
          name: profile.name || profile.preferred_username,
          roles: keycloakRoles,
        })
        .then(({ data }) => {
          console.log("✅ Sync response:", data);
          setUser({
            id: data.id,
            keycloakId: profile.sub,
            name: data.name,
            email: data.email,
            roles: data.role,
            role: keycloakRoles,
          });
        })
        .catch((err) => {
          console.error("❌ Sync error:", err.response?.data);
        })
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  const login = () =>
    keycloak.login({
      redirectUri: window.location.origin,
    });

  const logout = () => {
    keycloak.logout({ redirectUri: `${window.location.origin}/` });
  };

  const getToken = () => {
    return keycloak.token;
  };

  const hasRole = (role) => {
    const withPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    const withoutPrefix = role.replace("ROLE_", "");

    return (
      keycloak.hasRealmRole(withPrefix) ||
      keycloak.hasRealmRole(withoutPrefix) ||
      user?.roles?.includes(withPrefix) ||
      user?.role === withoutPrefix
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
