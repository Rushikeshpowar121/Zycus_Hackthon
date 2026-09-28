import axios from 'axios';
import {
  AnalyticsSummary,
  InventoryLog,
  PriceAudit,
  PricingCalculationResult,
  PricingRule,
  Product,
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const productService = {
  getAll: async (category?: string, status?: string, riskLevel?: string): Promise<Product[]> => {
    const params = new URLSearchParams();
    if (category) params.append('category', category);
    if (status) params.append('status', status);
    if (riskLevel) params.append('riskLevel', riskLevel);
    const response = await api.get<Product[]>(`/products?${params.toString()}`);
    return response.data;
  },

  getById: async (id: number): Promise<Product> => {
    const response = await api.get<Product>(`/products/${id}`);
    return response.data;
  },

  getCategories: async (): Promise<string[]> => {
    const response = await api.get<string[]>('/products/categories');
    return response.data;
  },

  create: async (data: Partial<Product>): Promise<Product> => {
    const response = await api.post<Product>('/products', data);
    return response.data;
  },

  updateStock: async (id: number, quantityChange: number, action: string, notes?: string): Promise<Product> => {
    const response = await api.patch<Product>(`/products/${id}/stock`, {
      quantityChange,
      action,
      notes,
    });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/products/${id}`);
  },
};

export const pricingService = {
  getRecommendation: async (productId: number, overrideStrategy?: string): Promise<PricingCalculationResult> => {
    const params = overrideStrategy ? `?overrideStrategy=${overrideStrategy}` : '';
    const response = await api.get<PricingCalculationResult>(`/pricing/recommendation/${productId}${params}`);
    return response.data;
  },

  applyRecommendation: async (productId: number, recommendation: PricingCalculationResult): Promise<Product> => {
    const response = await api.post<Product>(`/pricing/apply/${productId}`, recommendation);
    return response.data;
  },

  batchReprice: async (): Promise<PricingCalculationResult[]> => {
    const response = await api.post<PricingCalculationResult[]>('/pricing/batch-reprice');
    return response.data;
  },

  getRules: async (): Promise<PricingRule[]> => {
    const response = await api.get<PricingRule[]>('/pricing/rules');
    return response.data;
  },

  updateRule: async (ruleData: Partial<PricingRule>): Promise<PricingRule> => {
    const response = await api.put<PricingRule>('/pricing/rules', ruleData);
    return response.data;
  },
};

export const simulationService = {
  run: async (request: {
    productId: number;
    simulationType: string;
    viewMultiplier?: number;
    salesVelocityBoost?: number;
    competitorPriceAdjustment?: number;
    reduceExpiryDaysBy?: number;
  }): Promise<PricingCalculationResult> => {
    const response = await api.post<PricingCalculationResult>('/simulation/run', request);
    return response.data;
  },
};

export const analyticsService = {
  getSummary: async (): Promise<AnalyticsSummary> => {
    const response = await api.get<AnalyticsSummary>('/analytics/summary');
    return response.data;
  },

  getPriceAudits: async (productId?: number): Promise<PriceAudit[]> => {
    const url = productId ? `/analytics/price-audit/${productId}` : '/analytics/price-audit/recent';
    const response = await api.get<PriceAudit[]>(url);
    return response.data;
  },

  getInventoryLogs: async (): Promise<InventoryLog[]> => {
    const response = await api.get<InventoryLog[]>('/analytics/inventory-logs');
    return response.data;
  },
};
