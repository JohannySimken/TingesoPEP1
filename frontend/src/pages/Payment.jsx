import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { getReservationById } from "../api/reservations";
import { processPayment } from "../api/payment";

export default function Payment() {
  const { reservationId } = useParams();
  const navigate = useNavigate();

  const [reservation, setReservation] = useState(null);
  const [payment, setPayment] = useState(null);
  const [form, setForm] = useState({ cardNumber: "", cardExpiry: "", cvv: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    getReservationById(reservationId)
      .then(({ data }) => setReservation(data))
      .catch(() => navigate("/my-reservations"));
  }, [navigate, reservationId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const { data } = await processPayment({
        reservationId: Number(reservationId),
        cardNumber: form.cardNumber,
        cardExpiry: form.cardExpiry,
        cvv: form.cvv,
      });
      setPayment(data);
    } catch (err) {
      setError(err.response?.data?.message || "Error al procesar el pago.");
    } finally {
      setLoading(false);
    }
  };

  if (!reservation)
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="text-center py-20 text-gray-400">Cargando...</div>
      </div>
    );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-xl mx-auto px-4 py-10">
        <div className="bg-white rounded-xl shadow p-8">
          <h1 className="text-2xl font-bold text-gray-800 mb-6">
            Pago de Reserva
          </h1>

          <div className="bg-gray-50 rounded-lg p-4 mb-6 text-sm space-y-1">
            <p className="text-gray-500">
              Código: <strong>{reservation.reservationCode}</strong>
            </p>
            <p className="text-gray-500">
              Pasajeros: <strong>{reservation.passengerCount}</strong>
            </p>
            <p className="text-2xl font-bold text-teal-700 mt-2">
              ${reservation.finalAmount?.toLocaleString()}
            </p>
          </div>

          {!payment ? (
            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
              {error && (
                <p className="bg-red-100 text-red-600 px-4 py-2 rounded text-sm">
                  {error}
                </p>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Número de tarjeta
                </label>
                <input
                  type="text"
                  required
                  maxLength={16}
                  placeholder="1234 5678 9012 3456"
                  value={form.cardNumber}
                  onChange={(e) =>
                    setForm({ ...form, cardNumber: e.target.value })
                  }
                  className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-teal-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Vencimiento
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="MM/AA"
                    value={form.cardExpiry}
                    onChange={(e) =>
                      setForm({ ...form, cardExpiry: e.target.value })
                    }
                    className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-teal-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    CVV
                  </label>
                  <input
                    type="text"
                    required
                    maxLength={4}
                    placeholder="123"
                    value={form.cvv}
                    onChange={(e) => setForm({ ...form, cvv: e.target.value })}
                    className="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-teal-500"
                  />
                </div>
              </div>

              <p className="text-xs text-gray-400 text-center">
                🔒 Pago simulado — no se cobra ningún monto real
              </p>

              <button
                type="submit"
                disabled={loading}
                className="bg-teal-700 text-white py-3 rounded-lg font-semibold hover:bg-teal-800 transition disabled:opacity-50"
              >
                {loading
                  ? "Procesando..."
                  : `Pagar $${reservation.finalAmount?.toLocaleString()}`}
              </button>
            </form>
          ) : (
            <div className="flex flex-col gap-4 text-center">
              <div className="bg-green-50 border border-green-200 rounded-lg p-6">
                <p className="text-4xl mb-2">✅</p>
                <p className="text-green-700 font-bold text-lg">
                  ¡Pago aprobado!
                </p>
                <p className="text-gray-500 text-sm mt-1">
                  Transacción: <strong>{payment.transactionCode}</strong>
                </p>
              </div>
              <p className="text-xs text-gray-400">
                Tu voucher ha sido generado automáticamente.
              </p>
              <button
                onClick={() => navigate("/my-reservations")}
                className="bg-teal-700 text-white py-3 rounded-lg font-semibold hover:bg-teal-800 transition"
              >
                Ver mis reservas
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
