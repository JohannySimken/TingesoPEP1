import { useContext } from "react";
import { AuthContext } from "./authContextConfig";

export function useAuth() {
  return useContext(AuthContext);
}
