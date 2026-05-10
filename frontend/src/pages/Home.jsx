import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getAvailablePackages, searchPackages } from "../api/packages";

export default function Home() {
  const navigate = useNavigate();
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    destination: "",
    minPrice: "",
    maxPrice: "",
    startDate: "",
    endDate: "",
    tripType: "",
  });

  const fetchPackages = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await getAvailablePackages();
      setPackages(data);
    } catch {
      setPackages([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPackages();
  }, [fetchPackages]);

  const handleSearch = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const params = Object.fromEntries(
        Object.entries(filters).filter(([, v]) => v !== ""),
      );
      const { data } = await searchPackages(params);
      setPackages(data);
    } catch {
      setPackages([]);
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setFilters({
      destination: "",
      minPrice: "",
      maxPrice: "",
      startDate: "",
      endDate: "",
      tripType: "",
    });
    fetchPackages();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">
          Paquetes Disponibles
        </h2>

        {/* Filtros */}
        <form
          onSubmit={handleSearch}
          className="bg-white rounded-xl shadow p-4 mb-8 grid grid-cols-2 md:grid-cols-3 gap-4"
        >
          <input
            placeholder="Destino"
            value={filters.destination}
            onChange={(e) =>
              setFilters({ ...filters, destination: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            placeholder="Precio mínimo"
            type="number"
            value={filters.minPrice}
            onChange={(e) =>
              setFilters({ ...filters, minPrice: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            placeholder="Precio máximo"
            type="number"
            value={filters.maxPrice}
            onChange={(e) =>
              setFilters({ ...filters, maxPrice: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            placeholder="Fecha inicio"
            type="date"
            value={filters.startDate}
            onChange={(e) =>
              setFilters({ ...filters, startDate: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            placeholder="Fecha fin"
            type="date"
            value={filters.endDate}
            onChange={(e) =>
              setFilters({ ...filters, endDate: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <input
            placeholder="Tipo de viaje"
            value={filters.tripType}
            onChange={(e) =>
              setFilters({ ...filters, tripType: e.target.value })
            }
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
          />
          <div className="col-span-2 md:col-span-3 flex gap-3">
            <button
              type="submit"
              className="bg-teal-700 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-teal-800 transition"
            >
              Buscar
            </button>
            <button
              type="button"
              onClick={handleClear}
              className="border border-gray-300 text-gray-600 px-6 py-2 rounded-lg text-sm hover:bg-gray-100 transition"
            >
              Limpiar
            </button>
          </div>
        </form>

        {/* Listado */}
        {loading ? (
          <div className="text-center py-16 text-gray-400">
            Cargando paquetes...
          </div>
        ) : packages.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            No se encontraron paquetes disponibles.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {packages.map((pkg) => (
              <div
                key={pkg.id}
                className="bg-white rounded-xl shadow hover:shadow-md transition overflow-hidden"
              >
                <div className="bg-teal-700 h-2" />
                <div className="p-5">
                  <div className="flex justify-between items-start mb-2">
                    <h3 className="font-bold text-gray-800 text-lg">
                      {pkg.name}
                    </h3>
                    <span className="text-xs bg-teal-100 text-teal-700 px-2 py-1 rounded-full">
                      {pkg.tripType || "General"}
                    </span>
                  </div>
                  <p className="text-teal-600 font-medium text-sm mb-1">
                    📍 {pkg.destination}
                  </p>
                  <p className="text-gray-500 text-sm mb-3 line-clamp-2">
                    {pkg.description}
                  </p>
                  <div className="text-xs text-gray-400 mb-3">
                    📅 {pkg.startDate} → {pkg.endDate} · {pkg.duration} días
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-xl font-bold text-teal-700">
                      ${pkg.price?.toLocaleString()}
                    </span>
                    <span className="text-xs text-gray-400">
                      {pkg.availableSlots} cupos
                    </span>
                  </div>
                  <button
                    onClick={() => navigate(`/packages/${pkg.id}`)}
                    className="mt-4 w-full bg-teal-700 text-white py-2 rounded-lg text-sm font-medium hover:bg-teal-800 transition"
                  >
                    Ver detalle
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
