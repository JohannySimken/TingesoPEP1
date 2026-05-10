import { useState } from "react";
import Navbar from "../../components/Navbar";
import { getSalesReport, getRankingReport } from "../../api/reports";

export default function AdminReports() {
  const [tab, setTab] = useState("sales");
  const [startDate, setStart] = useState("");
  const [endDate, setEnd] = useState("");
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [searched, setSearched] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    setSearched(true);
    try {
      const fn = tab === "sales" ? getSalesReport : getRankingReport;
      const { data } = await fn(startDate, endDate);
      setResults(data);
    } catch {
      setError("Error al obtener el reporte.");
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const totalRevenue =
    tab === "sales"
      ? results.reduce((acc, r) => acc + (r.finalAmount || 0), 0)
      : results.reduce((acc, r) => acc + (r.totalRevenue || 0), 0);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-5xl mx-auto px-4 py-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Reportes</h2>

        {/* Tabs */}
        <div className="flex gap-2 mb-6">
          {[
            { key: "sales", label: "📊 Ventas por período" },
            { key: "ranking", label: "🏆 Ranking de paquetes" },
          ].map((t) => (
            <button
              key={t.key}
              onClick={() => {
                setTab(t.key);
                setResults([]);
                setSearched(false);
              }}
              className={`px-5 py-2 rounded-lg text-sm font-medium transition ${
                tab === t.key
                  ? "bg-teal-700 text-white"
                  : "bg-white border border-gray-300 text-gray-600 hover:bg-gray-50"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Filtro de fechas */}
        <form
          onSubmit={handleSearch}
          className="bg-white rounded-xl shadow p-5 mb-6 flex flex-wrap gap-4 items-end"
        >
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Fecha inicio
            </label>
            <input
              required
              type="date"
              value={startDate}
              onChange={(e) => setStart(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">
              Fecha fin
            </label>
            <input
              required
              type="date"
              value={endDate}
              onChange={(e) => setEnd(e.target.value)}
              className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="bg-teal-700 text-white px-6 py-2 rounded-lg text-sm font-medium hover:bg-teal-800 transition disabled:opacity-50"
          >
            {loading ? "Buscando..." : "Generar reporte"}
          </button>
        </form>

        {error && (
          <p className="bg-red-100 text-red-600 px-4 py-2 rounded mb-4 text-sm">
            {error}
          </p>
        )}

        {/* Resultados */}
        {searched && !loading && (
          <>
            {/* KPI total */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="bg-white rounded-xl shadow p-5">
                <p className="text-xs text-gray-400 mb-1">Total registros</p>
                <p className="text-3xl font-bold text-teal-700">
                  {results.length}
                </p>
              </div>
              <div className="bg-white rounded-xl shadow p-5">
                <p className="text-xs text-gray-400 mb-1">Ingresos totales</p>
                <p className="text-3xl font-bold text-teal-700">
                  ${totalRevenue.toLocaleString()}
                </p>
              </div>
            </div>

            {results.length === 0 ? (
              <p className="text-center py-8 text-gray-400">
                No hay datos para el período seleccionado.
              </p>
            ) : tab === "sales" ? (
              <div className="bg-white rounded-xl shadow overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
                    <tr>
                      {[
                        "Código",
                        "Usuario",
                        "Paquete",
                        "Pasajeros",
                        "Base",
                        "Descuento",
                        "Total",
                        "Fecha",
                      ].map((h) => (
                        <th key={h} className="px-4 py-3 text-left">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {results.map((r) => (
                      <tr key={r.id} className="hover:bg-gray-50">
                        <td className="px-4 py-3 font-mono text-xs">
                          {r.reservationCode}
                        </td>
                        <td className="px-4 py-3 text-gray-600">{r.userId}</td>
                        <td className="px-4 py-3 text-gray-600">
                          {r.tourPackageId}
                        </td>
                        <td className="px-4 py-3 text-center">
                          {r.passengerCount}
                        </td>
                        <td className="px-4 py-3">
                          ${r.baseAmount?.toLocaleString()}
                        </td>
                        <td className="px-4 py-3 text-green-600">
                          -${r.discountAmount?.toLocaleString()}
                        </td>
                        <td className="px-4 py-3 font-bold text-teal-700">
                          ${r.finalAmount?.toLocaleString()}
                        </td>
                        <td className="px-4 py-3 text-gray-400 text-xs">
                          {r.createdAt?.substring(0, 10)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="bg-white rounded-xl shadow overflow-x-auto">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
                    <tr>
                      {[
                        "#",
                        "Paquete",
                        "Destino",
                        "Reservas",
                        "Pasajeros",
                        "Ingresos",
                      ].map((h) => (
                        <th key={h} className="px-4 py-3 text-left">
                          {h}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {results.map((r, i) => (
                      <tr key={r.packageId} className="hover:bg-gray-50">
                        <td className="px-4 py-3">
                          <span
                            className={`font-bold text-lg ${
                              i === 0
                                ? "text-yellow-500"
                                : i === 1
                                  ? "text-gray-400"
                                  : i === 2
                                    ? "text-orange-400"
                                    : "text-gray-300"
                            }`}
                          >
                            {i === 0
                              ? "🥇"
                              : i === 1
                                ? "🥈"
                                : i === 2
                                  ? "🥉"
                                  : i + 1}
                          </span>
                        </td>
                        <td className="px-4 py-3 font-medium text-gray-800">
                          {r.packageName}
                        </td>
                        <td className="px-4 py-3 text-gray-500">
                          {r.destination}
                        </td>
                        <td className="px-4 py-3 text-center font-medium">
                          {r.totalReservations}
                        </td>
                        <td className="px-4 py-3 text-center">
                          {r.totalPassengers}
                        </td>
                        <td className="px-4 py-3 font-bold text-teal-700">
                          ${Number(r.totalRevenue)?.toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
