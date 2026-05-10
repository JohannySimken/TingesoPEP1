import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getPackageById } from "../api/packages";

export default function PackageDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pkg, setPkg] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPackageById(id)
      .then(({ data }) => setPkg(data))
      .catch(() => navigate("/"))
      .finally(() => setLoading(false));
  }, [navigate, id]);

  if (loading)
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="text-center py-20 text-gray-400">Cargando...</div>
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto px-4 py-10">
        <button
          onClick={() => navigate("/")}
          className="text-teal-700 text-sm mb-6 hover:underline flex items-center gap-1"
        >
          ← Volver al catálogo
        </button>

        <div className="bg-white rounded-xl shadow p-8">
          <div className="flex justify-between items-start mb-4">
            <h1 className="text-2xl font-bold text-gray-800">{pkg.name}</h1>
            <span
              className={`text-sm px-3 py-1 rounded-full font-medium ${
                pkg.status === "AVAILABLE"
                  ? "bg-green-100 text-green-700"
                  : pkg.status === "SOLD_OUT"
                    ? "bg-red-100 text-red-600"
                    : "bg-gray-100 text-gray-500"
              }`}
            >
              {pkg.status}
            </span>
          </div>

          <p className="text-teal-600 font-medium mb-1">📍 {pkg.destination}</p>
          <p className="text-gray-400 text-sm mb-4">
            📅 {pkg.startDate} → {pkg.endDate} · {pkg.duration} días
          </p>

          <p className="text-gray-600 mb-6">{pkg.description}</p>

          <div className="grid grid-cols-2 gap-4 mb-6">
            {pkg.tripType && (
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400 mb-1">Tipo de viaje</p>
                <p className="font-medium text-gray-700">{pkg.tripType}</p>
              </div>
            )}
            {pkg.season && (
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400 mb-1">Temporada</p>
                <p className="font-medium text-gray-700">{pkg.season}</p>
              </div>
            )}
            {pkg.category && (
              <div className="bg-gray-50 rounded-lg p-3">
                <p className="text-xs text-gray-400 mb-1">Categoría</p>
                <p className="font-medium text-gray-700">{pkg.category}</p>
              </div>
            )}
            <div className="bg-gray-50 rounded-lg p-3">
              <p className="text-xs text-gray-400 mb-1">Cupos disponibles</p>
              <p className="font-medium text-gray-700">
                {pkg.availableSlots} / {pkg.totalSlots}
              </p>
            </div>
          </div>

          {pkg.includedServices && (
            <div className="mb-4">
              <h3 className="font-semibold text-gray-700 mb-1">
                ✅ Servicios incluidos
              </h3>
              <p className="text-gray-500 text-sm">{pkg.includedServices}</p>
            </div>
          )}
          {pkg.conditions && (
            <div className="mb-4">
              <h3 className="font-semibold text-gray-700 mb-1">
                📋 Condiciones
              </h3>
              <p className="text-gray-500 text-sm">{pkg.conditions}</p>
            </div>
          )}
          {pkg.restrictions && (
            <div className="mb-6">
              <h3 className="font-semibold text-gray-700 mb-1">
                ⚠️ Restricciones
              </h3>
              <p className="text-gray-500 text-sm">{pkg.restrictions}</p>
            </div>
          )}

          <div className="flex justify-between items-center border-t pt-6">
            <span className="text-3xl font-bold text-teal-700">
              ${pkg.price?.toLocaleString()}
            </span>
            <button
              disabled={pkg.status !== "AVAILABLE"}
              onClick={() => navigate(`/reservations/new/${pkg.id}`)}
              className="bg-teal-700 text-white px-8 py-3 rounded-lg font-semibold hover:bg-teal-800 transition disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Reservar ahora
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
