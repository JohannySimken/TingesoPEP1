import api from "./axios";

export const processPayment = (data) => api.post(`/payments`, data);
export const getPaymentsByReservation = (reservationId) =>
  api.get(`/payments/reservation/${reservationId}`);
