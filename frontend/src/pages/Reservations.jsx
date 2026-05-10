import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getPackageById } from "../api/packages";
import { createReservation } from "../api/reservations";
import { useAuth } from "../context/useAuth";

export default function Reservation() {
  const { packageId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [pkg, setPkg] = useState(null);
  const [passengerCount, setPassengerCount] = useState(1);
  const [reservation, setReservation] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    getPackageById(packageId)
      .then(({ data }) => setPkg(data))
      .catch(() => navigate("/"));
  }, [navigate, packageId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const { data } = await createReservation({
        userId: user.id,
        packageId: Number(packageId),
        passengerCount: Number(passengerCount),
      });
      setReservation(data);
    } catch (err) {
      setError(err.response?.data?.message || "Error al crear la reserva.");
    } finally {
      setLoading(false);
    }
  };

  if (!pkg)
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="text-center py-20 text-gray-400">Cargando...</div>
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-2xl mx-auto px-4 py-10">
        <button
          onClick={() => navigate(`/packages/${packageId}`)}
          className="text-teal-700 text-sm mb-6 hover:underline"
        >
          ← Volver al paquete
        </button>

        <div className="bg-white rounded-xl shadow p-8">
          <h1 className="text-2xl font-bold text-gray-800 mb-1">
            Nueva Reserva
          </h1>
          <p className="text-gray-400 text-sm mb-6">
            Paquete:{" "}
            <span className="font-medium text-teal-700">{pkg.name}</span> —{" "}
            {pkg.destination}
          </p>

          {!reservation ? (
            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              {error && (
                <p className="bg-red-100 text-red-600 px-4 py-2 rounded text-sm">
                  {error}
                </p>
              )}

              <div className="bg-gray-50 rounded-lg p-4 text-sm text-gray-600 space-y-1">
                <p>
                  📅 {pkg.startDate} → {pkg.endDate}
                </p>
                <p>
                  💰 Precio por persona:{" "}
                  <strong>${pkg.price?.toLocaleString()}</strong>
                </p>
                <p>
                  🪑 Cupos disponibles: <strong>{pkg.availableSlots}</strong>
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Cantidad de pasajeros
                </label>
                <input
                  type="number"
                  min="1"
                  max={pkg.availableSlots}
                  value={passengerCount}
                  onChange={(e) => setPassengerCount(e.target.value)}
                  className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-teal-500"
                />
              </div>

              <div className="bg-teal-50 rounded-lg p-4">
                <p className="text-sm text-gray-500">Monto estimado</p>
                <p className="text-2xl font-bold text-teal-700">
                  ${(pkg.price * passengerCount).toLocaleString()}
                </p>
                <p className="text-xs text-gray-400 mt-1">
                  Los descuentos se calculan al confirmar
                </p>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="bg-teal-700 text-white py-3 rounded-lg font-semibold hover:bg-teal-800 transition disabled:opacity-50"
              >
                {loading ? "Creando reserva..." : "Confirmar Reserva"}
              </button>
            </form>
          ) : (
            /* Resumen post-creación */
            <div className="flex flex-col gap-4">
              <div className="bg-green-50 border border-green-200 rounded-lg p-4 text-center">
                <p className="text-green-700 font-bold text-lg">
                  ✅ Reserva creada
                </p>
                <p className="text-gray-500 text-sm mt-1">
                  Código: <strong>{reservation.reservationCode}</strong>
                </p>
              </div>

              <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Monto base</span>
                  <span className="font-medium">
                    ${reservation.baseAmount?.toLocaleString()}
                  </span>
                </div>
                <div className="flex justify-between text-green-600">
                  <span>Descuentos aplicados</span>
                  <span>-${reservation.discountAmount?.toLocaleString()}</span>
                </div>
                {reservation.discountDetail && (
                  <p className="text-xs text-gray-400">
                    {reservation.discountDetail}
                  </p>
                )}
                <div className="flex justify-between font-bold text-base border-t pt-2">
                  <span>Total a pagar</span>
                  <span className="text-teal-700">
                    ${reservation.finalAmount?.toLocaleString()}
                  </span>
                </div>
              </div>

              <p className="text-xs text-orange-500 text-center">
                ⚠️ Tienes 24 horas para completar el pago antes que expire la
                reserva.
              </p>

              <button
                onClick={() => navigate(`/payments/${reservation.id}`)}
                className="bg-teal-700 text-white py-3 rounded-lg font-semibold hover:bg-teal-800 transition"
              >
                Proceder al Pago
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
