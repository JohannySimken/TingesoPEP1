import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../api/users";

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    phone: "",
    nationality: "",
  });
  const [error, setError] = useState("");
  const [loading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);
    try {
      await registerUser(form);
      navigate("/login");
    } catch {
      setError("Error al registrarse. El email ya puede estar en uso");
    } finally {
      setIsLoading(false);
    }
  };

  const fields = [
    {
      key: "name",
      label: "Nombre completo",
      type: "text",
      placeholder: "Juan Pérez",
    },
    {
      key: "email",
      label: "Email",
      type: "email",
      placeholder: "correo@ejemplo.com",
    },
    {
      key: "password",
      label: "Contraseña",
      type: "password",
      placeholder: "••••••••",
    },
    {
      key: "phone",
      label: "Teléfono",
      type: "text",
      placeholder: "+56912345678",
    },
    {
      key: "nationality",
      label: "Nacionalidad",
      type: "text",
      placeholder: "Chilena",
    },
  ];

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md">
        <h1 className="text-2xl font-bold text-teal-700 mb-6 text-center">
          Crear Cuenta
        </h1>

        {error && (
          <p className="bg-red-100 text-red-600 px-4 py-2 rounded mb-4 text-sm">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {fields.map((f) => (
            <div key={f.key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                {f.label}
              </label>
              <input
                type={f.type}
                required={["name", "email", "password"].includes(f.key)}
                placeholder={f.placeholder}
                value={form[f.key]}
                onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
                className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-teal-500"
              />
            </div>
          ))}

          <button
            type="submit"
            disabled={loading}
            className="bg-teal-700 text-white py-2 rounded-lg font-semibold hover:bg-teal-800 transition disabled:opacity-50"
          >
            {loading ? "Registrando..." : "Registrarse"}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-4">
          ¿Ya tienes cuenta?{" "}
          <Link
            to="/login"
            className="text-teal-700 font-medium hover:underline"
          >
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
}
