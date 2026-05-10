import { useState, useEffect } from "react";
import Navbar from "../../components/Navbar";
import {
  getAllPackages,
  createPackage,
  updatePackage,
  changePackageStatus,
} from "../../api/packages";

const EMPTY_FORM = {
  name: "",
  destination: "",
  description: "",
  startDate: "",
  endDate: "",
  price: "",
  totalSlots: "",
  includedServices: "",
  conditions: "",
  restrictions: "",
  tripType: "",
  season: "",
  category: "",
};

const STATUS_OPTS = ["AVAILABLE", "SOLD_OUT", "NOT_VALID", "CANCELLED"];
const STATUS_COLOR = {
  AVAILABLE: "bg-green-100 text-green-700",
  SOLD_OUT: "bg-red-100 text-red-600",
  NOT_VALID: "bg-gray-100 text-gray-500",
  CANCELLED: "bg-orange-100 text-orange-600",
};

export default function AdminPackages() {
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const fetchPackages = async () => {
    setLoading(true);
    try {
      const { data } = await getAllPackages();
      setPackages(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setError("");
    setShowModal(true);
  };

  const openEdit = (pkg) => {
    setEditing(pkg);
    setForm({ ...pkg });
    setError("");
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      if (editing) {
        const { data } = await updatePackage(editing.id, form);
        setPackages((prev) =>
          prev.map((p) => (p.id === editing.id ? data : p)),
        );
      } else {
        const { data } = await createPackage(form);
        setPackages((prev) => [...prev, data]);
      }
      setShowModal(false);
    } catch (err) {
      setError(err.response?.data?.message || "Error al guardar el paquete.");
    } finally {
      setSaving(false);
    }
  };

  const handleStatusChange = async (id, status) => {
    try {
      const { data } = await changePackageStatus(id, status);
      setPackages((prev) => prev.map((p) => (p.id === id ? data : p)));
    } catch {
      alert("Error al cambiar el estado.");
    }
  };

  const fields = [
    { key: "name", label: "Nombre", type: "text", required: true },
    { key: "destination", label: "Destino", type: "text", required: true },
    {
      key: "description",
      label: "Descripción",
      type: "textarea",
      required: true,
    },
    { key: "startDate", label: "Fecha inicio", type: "date", required: true },
    { key: "endDate", label: "Fecha fin", type: "date", required: true },
    { key: "price", label: "Precio", type: "number", required: true },
    {
      key: "totalSlots",
      label: "Cupos totales",
      type: "number",
      required: true,
    },
    { key: "tripType", label: "Tipo de viaje", type: "text", required: false },
    { key: "season", label: "Temporada", type: "text", required: false },
    { key: "category", label: "Categoría", type: "text", required: false },
    {
      key: "includedServices",
      label: "Servicios incluidos",
      type: "textarea",
      required: false,
    },
    {
      key: "conditions",
      label: "Condiciones",
      type: "textarea",
      required: false,
    },
    {
      key: "restrictions",
      label: "Restricciones",
      type: "textarea",
      required: false,
    },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800">
            Gestión de Paquetes
          </h2>
          <button
            onClick={openCreate}
            className="bg-teal-700 text-white px-5 py-2 rounded-lg font-medium hover:bg-teal-800 transition"
          >
            + Nuevo Paquete
          </button>
        </div>

        {loading ? (
          <div className="text-center py-16 text-gray-400">Cargando...</div>
        ) : (
          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 uppercase text-xs">
                <tr>
                  {[
                    "ID",
                    "Nombre",
                    "Destino",
                    "Fechas",
                    "Precio",
                    "Cupos",
                    "Estado",
                    "Acciones",
                  ].map((h) => (
                    <th key={h} className="px-4 py-3 text-left">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {packages.map((pkg) => (
                  <tr key={pkg.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-400">{pkg.id}</td>
                    <td className="px-4 py-3 font-medium text-gray-800">
                      {pkg.name}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {pkg.destination}
                    </td>
                    <td className="px-4 py-3 text-gray-400 text-xs">
                      {pkg.startDate}
                      <br />
                      {pkg.endDate}
                    </td>
                    <td className="px-4 py-3 font-medium">
                      ${pkg.price?.toLocaleString()}
                    </td>
                    <td className="px-4 py-3">
                      {pkg.availableSlots}/{pkg.totalSlots}
                    </td>
                    <td className="px-4 py-3">
                      <select
                        value={pkg.status}
                        onChange={(e) =>
                          handleStatusChange(pkg.id, e.target.value)
                        }
                        className={`text-xs px-2 py-1 rounded-full font-medium border-0 cursor-pointer ${STATUS_COLOR[pkg.status]}`}
                      >
                        {STATUS_OPTS.map((s) => (
                          <option key={s} value={s}>
                            {s}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => openEdit(pkg)}
                        className="text-teal-700 hover:underline text-xs font-medium"
                      >
                        Editar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto p-8">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-bold text-gray-800">
                {editing ? "Editar Paquete" : "Nuevo Paquete"}
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-400 hover:text-gray-600 text-2xl leading-none"
              >
                ×
              </button>
            </div>

            {error && (
              <p className="bg-red-100 text-red-600 px-4 py-2 rounded mb-4 text-sm">
                {error}
              </p>
            )}

            <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-4">
              {fields.map((f) => (
                <div
                  key={f.key}
                  className={f.type === "textarea" ? "col-span-2" : ""}
                >
                  <label className="block text-xs font-medium text-gray-600 mb-1">
                    {f.label}
                  </label>
                  {f.type === "textarea" ? (
                    <textarea
                      required={f.required}
                      value={form[f.key] || ""}
                      onChange={(e) =>
                        setForm({ ...form, [f.key]: e.target.value })
                      }
                      rows={2}
                      className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                  ) : (
                    <input
                      type={f.type}
                      required={f.required}
                      value={form[f.key] || ""}
                      onChange={(e) =>
                        setForm({ ...form, [f.key]: e.target.value })
                      }
                      className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal-500"
                    />
                  )}
                </div>
              ))}

              <div className="col-span-2 flex gap-3 mt-2">
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 bg-teal-700 text-white py-2 rounded-lg font-semibold hover:bg-teal-800 transition disabled:opacity-50"
                >
                  {saving ? "Guardando..." : "Guardar"}
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 border border-gray-300 text-gray-600 py-2 rounded-lg hover:bg-gray-50 transition"
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
