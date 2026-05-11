import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import PackageDetail from "./pages/PackageDetail";
import Reservation from "./pages/Reservations";
import Payment from "./pages/Payment";
import MyReservations from "./pages/MyReservations";

import AdminPackages from "./pages/admin/AdminPackages";
import AdminReservations from "./pages/admin/AdminReservations";
import AdminPromotions from "./pages/admin/AdminPromotions";
import AdminReports from "./pages/admin/AdminReports";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Públicas */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/" element={<Home />} />

          {/* Cliente */}
          <Route
            path="/packages/:id"
            element={
              <ProtectedRoute>
                <PackageDetail />
              </ProtectedRoute>
            }
          />
          <Route
            path="/reservations/new/:packageId"
            element={
              <ProtectedRoute>
                <Reservation />
              </ProtectedRoute>
            }
          />
          <Route
            path="/payments/:reservationId"
            element={
              <ProtectedRoute>
                <Payment />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-reservations"
            element={
              <ProtectedRoute>
                <MyReservations />
              </ProtectedRoute>
            }
          />

          {/* Admin */}
          <Route
            path="/admin/packages"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminPackages />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/reservations"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminReservations />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/promotions"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminPromotions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin/reports"
            element={
              <ProtectedRoute role="ADMIN">
                <AdminReports />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
