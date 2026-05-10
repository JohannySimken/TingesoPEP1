import api from "./axios";

export const getAllPackages = () => api.get("/packages");
export const getAvailablePackages = () => api.get("/packages/available");
export const getPackageById = (id) => api.get(`/packages/${id}`);
export const searchPackages = (params) =>
  api.get(`/packages/search`, { params });
export const createPackage = (data) => api.post(`/packages`, data);
export const updatePackage = (id, data) => api.put(`/packages/${id}`, data);
export const changePackageStatus = (id, status) =>
  api.patch(`/packages/${id}/status`, null, { params: { status } });
