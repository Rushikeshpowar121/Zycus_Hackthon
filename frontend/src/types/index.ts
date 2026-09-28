export type PricingStrategyType = 
  | 'DEMAND_BASED'
  | 'COMPETITOR_BASED'
  | 'AGING_INVENTORY'
  | 'AI_HEURISTIC'
  | 'MANUAL_OVERRIDE';

export type InventoryStatus = 
  | 'OPTIMAL'
  | 'LOW_STOCK'
  | 'OUT_OF_STOCK'
  | 'OVERSTOCKED'
  | 'EXPIRING_SOON';

export type DemandLevel = 
  | 'VERY_LOW'
  | 'LOW'
  | 'NORMAL'
  | 'HIGH'
  | 'SURGE';

export type RiskLevel = 
  | 'LOW'
  | 'MEDIUM'
  | 'HIGH'
  | 'CRITICAL';

export interface Product {
  id: number;
  sku: string;
  name: string;
  category: string;
  currentPrice: number;
  basePrice: number;
  costPrice: number;
  minPrice: number;
  maxPrice: number;
  stockQuantity: number;
  reorderPoint: number;
  maxStockLimit: number;
  daysInStock: number;
  daysToExpiry?: number;
  competitorPriceIndex?: number;
  salesVelocity?: number;
  viewsCount?: number;
  activeStrategyType: PricingStrategyType;
  status: InventoryStatus;
  demandLevel: DemandLevel;
  riskLevel: RiskLevel;
  createdAt: string;
  updatedAt?: string;
}

export interface PricingCalculationResult {
  productId: number;
  sku: string;
  productName: string;
  currentPrice: number;
  recommendedPrice: number;
  priceChangePercent: number;
  strategyType: PricingStrategyType;
  confidenceScore: number;
  reasoning: string;
  autoApplied: boolean;
}

export interface PriceAudit {
  id: number;
  productId: number;
  productSku: string;
  oldPrice: number;
  newPrice: number;
  priceChangePercent: number;
  strategyUsed: PricingStrategyType;
  reason: string;
  timestamp: string;
}

export interface PricingRule {
  id: number;
  strategyType: PricingStrategyType;
  surgeMultiplier?: number;
  discountRate?: number;
  competitorMarginTarget?: number;
  maxDiscountPercent?: number;
  aiConfidenceScore?: number;
  active: boolean;
}

export interface AnalyticsSummary {
  totalProducts: number;
  optimalStockCount: number;
  lowStockCount: number;
  outOfStockCount: number;
  overstockedCount: number;
  expiringSoonCount: number;
  criticalRiskCount: number;
  totalInventoryValue: number;
  totalPotentialRevenue: number;
  averageProfitMarginPercent: number;
  totalRepricedEvents: number;
  strategyDistribution: Record<string, number>;
  categoryDistribution: Record<string, number>;
}

export interface InventoryLog {
  id: number;
  productId: number;
  sku: string;
  action: string;
  quantityChange: number;
  previousStock: number;
  newStock: number;
  notes?: string;
  timestamp: string;
}

export interface PricingSuggestion {
  id: number;
  productId: number;
  suggestedPrice: number;
  oldPrice: number;
  direction: 'INCREASE' | 'DECREASE' | 'HOLD';
  confidenceScore: number;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface ReorderSuggestion {
  id: number;
  productId: number;
  supplierId: number;
  suggestedQuantity: number;
  suggestedLeadTimeDays?: number;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}
