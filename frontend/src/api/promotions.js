import api from "./axios";

export const getAllPromotions = () => api.get(`/promotions`);
export const getPromotionById = (id) => api.get(`/promotions/${id}`);
export const createPromotion = (data) => api.post(`/promotions`, data);
export const updatePromotion = (id, data) => api.put(`/promotions/${id}`, data);
export const deletePromotion = (id) => api.delete(`/promotions/${id}`);
