import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";

export default function Login() {
  const { user, login, loading, hasRole } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!loading && user) {
      navigate(hasRole("ROLE_ADMIN") ? "/admin/packages" : "/");
    }
  }, [user, loading]);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-lg p-10 w-full max-w-sm text-center">
        <div className="text-4xl mb-4">✈</div>
        <h1 className="text-2xl font-bold text-gray-800 mb-2">TravelAgency</h1>
        <p className="text-gray-400 text-sm mb-8">
          Inicia sesión para continuar
        </p>
        <button
          onClick={login}
          className="w-full bg-teal-700 text-white py-3 rounded-xl font-medium hover:bg-teal-800 transition"
        >
          Iniciar sesión con Keycloak
        </button>
      </div>
    </div>
  );
}
