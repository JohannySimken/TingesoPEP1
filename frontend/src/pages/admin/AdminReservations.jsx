import { useState, useEffect } from "react";
import Navbar from "../../components/Navbar";
import {
  getAllReservations,
  cancelReservation,
  confirmReservation,
} from "../../api/reservations";

const STATUS_COLOR = {
  PENDING: "bg-yellow-100 text-yellow-700",
  PENDING_PAYMENT: "bg-yellow-100 text-yellow-700",
  CONFIRMED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-600",
  EXPIRED: "bg-gray-100 text-gray-500",
};

const HEADERS = [
  "ID",
  "Código",
  "Usuario",
  "Paquete",
  "Pasajeros",
  "Base",
  "Descuento",
  "Total",
  "Estado",
  "Acciones",
];

export default function AdminReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState(null); // para modal de detalle

  useEffect(() => {
    getAllReservations()
      .then(({ data }) => setReservations(data))
      .finally(() => setLoading(false));
  }, []);

  const handleCancel = async (id) => {
    if (!confirm("¿Cancelar esta reserva?")) return;
    try {
      await cancelReservation(id);
      setReservations((prev) =>
        prev.map((r) => (r.id === id ? { ...r, status: "CANCELLED" } : r)),
      );
    } catch (err) {
      alert(err.response?.data?.message || "No se puede cancelar.");
    }
  };

  const handleConfirm = async (id) => {
    if (!confirm("¿Confirmar esta reserva?")) return;
    try {
      await confirmReservation(id);
      setReservations((prev) =>
        prev.map((r) => (r.id === id ? { ...r, status: "CONFIRMED" } : r)),
      );
    } catch (err) {
      alert(err.response?.data?.message || "No se puede confirmar.");
    }
  };

  const filtered = reservations.filter(
    (r) => filter === "" || r.status === filter,
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">Reservas</h2>
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-600 focus:outline-none focus:ring-2 focus:ring-teal-500"
          >
            <option value="">Todos los estados</option>
            <option value="PENDING">Pendiente</option>
            <option value="PENDING_PAYMENT">Pendiente de pago</option>
            <option value="CONFIRMED">Confirmada</option>
            <option value="CANCELLED">Cancelada</option>
            <option value="EXPIRED">Expirada</option>
          </select>
        </div>

        {loading ? (
          <div className="text-center py-16 text-gray-400">Cargando...</div>
        ) : (
          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-100">
                  {HEADERS.map((h) => (
                    <th
                      key={h}
                      className="text-left px-4 py-3 text-gray-500 font-medium"
                    >
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.length === 0 ? (
                  <tr>
                    <td
                      colSpan={HEADERS.length}
                      className="text-center py-10 text-gray-400"
                    >
                      No hay reservas con este estado.
                    </td>
                  </tr>
                ) : (
                  filtered.map((r) => (
                    <tr
                      key={r.id}
                      className="border-b border-gray-50 hover:bg-gray-50 transition"
                    >
                      <td className="px-4 py-3 text-gray-500">{r.id}</td>
                      <td className="px-4 py-3 font-mono font-medium">
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
                      <td className="px-4 py-3">
                        <span
                          className={`text-xs px-2 py-1 rounded-full font-medium ${STATUS_COLOR[r.status] || "bg-gray-100 text-gray-500"}`}
                        >
                          {r.status}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex gap-2">
                          {/* Ver detalle */}
                          <button
                            onClick={() => setSelected(r)}
                            className="bg-gray-100 text-gray-600 px-3 py-1 rounded-lg text-xs hover:bg-gray-200 transition"
                            title="Ver detalle"
                          >
                            👁 Ver
                          </button>

                          {/* Confirmar — solo si está pendiente */}
                          {(r.status === "PENDING" ||
                            r.status === "PENDING_PAYMENT") && (
                            <button
                              onClick={() => handleConfirm(r.id)}
                              className="bg-green-100 text-green-700 px-3 py-1 rounded-lg text-xs hover:bg-green-200 transition"
                              title="Confirmar reserva"
                            >
                              ✅ Confirmar
                            </button>
                          )}

                          {/* Cancelar — si no está cancelada ni confirmada */}
                          {r.status !== "CANCELLED" &&
                            r.status !== "CONFIRMED" && (
                              <button
                                onClick={() => handleCancel(r.id)}
                                className="bg-red-50 text-red-500 px-3 py-1 rounded-lg text-xs hover:bg-red-100 transition"
                                title="Cancelar reserva"
                              >
                                ❌ Cancelar
                              </button>
                            )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal de detalle */}
      {selected && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-md">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-lg font-bold text-gray-800">
                Detalle — #{selected.reservationCode}
              </h3>
              <button
                onClick={() => setSelected(null)}
                className="text-gray-400 hover:text-gray-600 text-xl font-bold"
              >
                ×
              </button>
            </div>

            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">ID</span>
                <span className="font-medium">{selected.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Usuario ID</span>
                <span className="font-medium">{selected.userId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Paquete ID</span>
                <span className="font-medium">{selected.tourPackageId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Pasajeros</span>
                <span className="font-medium">{selected.passengerCount}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Monto base</span>
                <span className="font-medium">
                  ${selected.baseAmount?.toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Descuento</span>
                <span className="font-medium text-green-600">
                  -${selected.discountAmount?.toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Total</span>
                <span className="font-bold text-teal-700">
                  ${selected.finalAmount?.toLocaleString()}
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Estado</span>
                <span
                  className={`text-xs px-2 py-1 rounded-full font-medium ${STATUS_COLOR[selected.status] || "bg-gray-100 text-gray-500"}`}
                >
                  {selected.status}
                </span>
              </div>
              {selected.discountDetail && (
                <div className="pt-2 border-t border-gray-100">
                  <p className="text-gray-400 text-xs">
                    {selected.discountDetail}
                  </p>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-gray-500">Expira</span>
                <span className="font-medium text-xs">
                  {selected.expiresAt?.replace("T", " ").substring(0, 16)}
                </span>
              </div>
            </div>

            <button
              onClick={() => setSelected(null)}
              className="mt-6 w-full bg-gray-100 text-gray-600 py-2 rounded-lg hover:bg-gray-200 transition text-sm"
            >
              Cerrar
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
