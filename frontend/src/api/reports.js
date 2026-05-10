import api from "./axios";

export const getSalesReport = (startDate, endDate) =>
  api.get(`/reports/sales`, { params: { startDate, endDate } });
export const getRankingReport = (startDate, endDate) =>
  api.get(`/reports/ranking`, { params: { startDate, endDate } });
