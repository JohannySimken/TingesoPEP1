import api from "./axios";

export const getAllReservations = () => api.get("/reservations");
export const getReservationById = (id) => api.get(`/reservations/${id}`);
export const getReservationsByUser = (userId) =>
  api.get(`/reservations/user/${userId}`);
export const createReservation = (data) => api.post("/reservations", data);
export const cancelReservation = (id) =>
  api.patch(`/reservations/${id}/cancel`);
export const confirmReservation = (id) =>
  api.patch(`/reservations/${id}/confirm`);
