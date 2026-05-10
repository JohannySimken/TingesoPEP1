import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getReservationsByUser, cancelReservation } from "../api/reservations";
import { useAuth } from "../context/useAuth";

const STATUS_LABEL = {
  PENDING: {
    label: "Pendiente de pago",
    color: "bg-yellow-100 text-yellow-700",
  },
  PENDING_PAYMENT: {
    label: "Pendiente de pago",
    color: "bg-yellow-100 text-yellow-700",
  },
  CONFIRMED: { label: "Confirmada", color: "bg-green-100 text-green-700" },
  CANCELLED: { label: "Cancelada", color: "bg-red-100 text-red-600" },
  EXPIRED: { label: "Expirada", color: "bg-gray-100 text-gray-500" },
};

export default function MyReservations() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getReservationsByUser(user.id)
      .then(({ data }) => setReservations(data))
      .finally(() => setLoading(false));
  }, [user.id]);

  const handleCancel = async (id) => {
    if (!confirm("¿Estás seguro de cancelar esta reserva?")) return;
    try {
      await cancelReservation(id);
      setReservations((prev) =>
        prev.map((r) => (r.id === id ? { ...r, status: "CANCELLED" } : r)),
      );
    } catch (err) {
      alert(
        err.response?.data?.message || "No se puede cancelar esta reserva.",
      );
    }
  };

  const canActOnReservation = (status) =>
    status === "PENDING" ||
    status === "PENDING_PAYMENT" ||
    status === "EXPIRED";

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-4xl mx-auto px-4 py-8">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">Mis Reservas</h2>

        {loading ? (
          <div className="text-center py-16 text-gray-400">Cargando...</div>
        ) : reservations.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <p className="text-lg mb-4">No tienes reservas aún.</p>
            <button
              onClick={() => navigate("/")}
              className="bg-teal-700 text-white px-6 py-2 rounded-lg hover:bg-teal-800 transition"
            >
              Ver paquetes disponibles
            </button>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {reservations.map((r) => {
              const s = STATUS_LABEL[r.status] || STATUS_LABEL.EXPIRED;
              return (
                <div key={r.id} className="bg-white rounded-xl shadow p-6">
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <p className="font-bold text-gray-800 text-lg">
                        Reserva #{r.reservationCode}
                      </p>
                      <p className="text-gray-400 text-sm">
                        Paquete ID: {r.tourPackageId} · {r.passengerCount}{" "}
                        pasajero(s)
                      </p>
                    </div>
                    <span
                      className={`text-xs px-3 py-1 rounded-full font-medium ${s.color}`}
                    >
                      {s.label}
                    </span>
                  </div>

                  <div className="grid grid-cols-3 gap-3 text-sm mb-4">
                    <div className="bg-gray-50 rounded-lg p-3">
                      <p className="text-gray-400 text-xs mb-1">Monto base</p>
                      <p className="font-medium">
                        ${r.baseAmount?.toLocaleString()}
                      </p>
                    </div>
                    <div className="bg-green-50 rounded-lg p-3">
                      <p className="text-gray-400 text-xs mb-1">Descuento</p>
                      <p className="font-medium text-green-600">
                        -${r.discountAmount?.toLocaleString()}
                      </p>
                    </div>
                    <div className="bg-teal-50 rounded-lg p-3">
                      <p className="text-gray-400 text-xs mb-1">Total</p>
                      <p className="font-bold text-teal-700">
                        ${r.finalAmount?.toLocaleString()}
                      </p>
                    </div>
                  </div>

                  {r.discountDetail && (
                    <p className="text-xs text-gray-400 mb-3">
                      {r.discountDetail}
                    </p>
                  )}

                  <div className="flex gap-3">
                    {canActOnReservation(r.status) && (
                      <>
                        <button
                          onClick={() => navigate(`/payments/${r.id}`)}
                          className="bg-teal-700 text-white px-4 py-2 rounded-lg text-sm hover:bg-teal-800 transition"
                        >
                          Pagar ahora
                        </button>
                        <button
                          onClick={() => handleCancel(r.id)}
                          className="border border-red-300 text-red-500 px-4 py-2 rounded-lg text-sm hover:bg-red-50 transition"
                        >
                          Cancelar
                        </button>
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
