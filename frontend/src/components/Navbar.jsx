import { Link } from "react-router-dom";
import { useAuth } from "../context/useAuth";

export default function Navbar() {
  const { user, login, logout, hasRole } = useAuth();

  return (
    <nav className="bg-teal-700 text-white px-6 py-4 flex justify-between items-center shadow">
      <Link to="/" className="font-bold text-xl tracking-tight">
        ✈ TravelAgency
      </Link>

      <div className="flex items-center gap-6 text-sm">
        {user ? (
          <>
            {hasRole("ROLE_ADMIN") ? (
              <>
                <Link to="/admin/packages" className="hover:underline">
                  Paquetes
                </Link>
                <Link to="/admin/reservations" className="hover:underline">
                  Reservas
                </Link>
                <Link to="/admin/promotions" className="hover:underline">
                  Promociones
                </Link>
                <Link to="/admin/reports" className="hover:underline">
                  Reportes
                </Link>
              </>
            ) : (
              <>
                <Link to="/" className="hover:underline">
                  Inicio
                </Link>
                <Link to="/my-reservations" className="hover:underline">
                  Mis Reservas
                </Link>
              </>
            )}
            <span className="text-teal-200 font-medium">{user.name}</span>
            <button
              onClick={logout}
              className="bg-white text-teal-700 px-3 py-1 rounded-lg text-sm font-medium hover:bg-teal-50 transition"
            >
              Cerrar sesión
            </button>
          </>
        ) : (
          // Usuario no autenticado
          <button
            onClick={login}
            className="bg-white text-teal-700 px-3 py-1 rounded-lg text-sm font-medium hover:bg-teal-50 transition"
          >
            Iniciar sesión
          </button>
        )}
      </div>
    </nav>
  );
}
