import React, { useState } from 'react';
import { Product, PricingStrategyType, InventoryStatus } from '../types';
import { Sparkles, Edit3, TrendingUp, Search, Filter, ShoppingCart, AlertTriangle, Zap } from 'lucide-react';
import { productService } from '../services/api';

interface ProductTableProps {
  products: Product[];
  categories: string[];
  onOpenPricingModal: (product: Product) => void;
  onOpenStockModal: (product: Product) => void;
  onRefresh?: () => void;
}

export const ProductTable: React.FC<ProductTableProps> = ({
  products,
  categories,
  onOpenPricingModal,
  onOpenStockModal,
  onRefresh,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [simulatingId, setSimulatingId] = useState<number | null>(null);

  const filteredProducts = products.filter((p) => {
    const matchesSearch = p.name.toLowerCase().includes(searchTerm.toLowerCase()) || p.sku.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'ALL' || p.category === selectedCategory;
    const matchesStatus = selectedStatus === 'ALL' || p.status === selectedStatus;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  const getStatusBadgeClass = (status: InventoryStatus) => {
    switch (status) {
      case 'OPTIMAL': return 'badge-optimal';
      case 'LOW_STOCK': return 'badge-low-stock';
      case 'OUT_OF_STOCK': return 'badge-out-of-stock';
      case 'OVERSTOCKED': return 'badge-overstocked';
      case 'EXPIRING_SOON': return 'badge-expiring';
      case 'PRICE_REVIEW_PENDING': return 'badge-low-stock';
      default: return 'badge-optimal';
    }
  };

  const getStrategyColor = (strategy: PricingStrategyType) => {
    switch (strategy) {
      case 'DEMAND_BASED': return '#818cf8';
      case 'COMPETITOR_BASED': return '#38bdf8';
      case 'AGING_INVENTORY': return '#f472b6';
      case 'AI_HEURISTIC': return '#34d399';
      default: return '#9ca3af';
    }
  };

  const handleSimulateSale = async (product: Product) => {
    setSimulatingId(product.id);
    try {
      await productService.simulateSale(product.id, 1);
      if (onRefresh) await onRefresh();
    } catch (err) {
      console.error('Sale simulation failed:', err);
    } finally {
      setSimulatingId(null);
    }
  };

  return (
    <div className="glass-panel" style={{ padding: '24px' }}>
      {/* Search & Filter Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', marginBottom: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1, minWidth: '280px' }}>
          <div style={{ position: 'relative', width: '100%' }}>
            <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              type="text"
              placeholder="Search by Product Name or SKU..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{
                width: '100%',
                padding: '10px 12px 10px 38px',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                color: '#fff',
                fontSize: '0.85rem',
              }}
            />
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <Filter size={16} color="var(--text-muted)" />
            <select
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
              style={{
                padding: '10px 14px',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                color: '#fff',
                fontSize: '0.85rem',
              }}
            >
              <option value="ALL">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>{cat}</option>
              ))}
            </select>
          </div>

          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            style={{
              padding: '10px 14px',
              background: 'var(--bg-input)',
              border: '1px solid var(--border-color)',
              borderRadius: 'var(--radius-md)',
              color: '#fff',
              fontSize: '0.85rem',
            }}
          >
            <option value="ALL">All Statuses</option>
            <option value="OPTIMAL">Optimal</option>
            <option value="LOW_STOCK">Low Stock</option>
            <option value="OUT_OF_STOCK">Out of Stock</option>
            <option value="OVERSTOCKED">Overstocked</option>
            <option value="EXPIRING_SOON">Expiring Soon</option>
            <option value="PRICE_REVIEW_PENDING">Pending Review</option>
          </select>
        </div>
      </div>

      {/* Products Table */}
      <div className="table-container">
        <table className="stock-table">
          <thead>
            <tr>
              <th>Product Info</th>
              <th>Current Price</th>
              <th>Stock / Threshold</th>
              <th>Demand Velocity</th>
              <th>Active Strategy</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.length === 0 ? (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                  No inventory products matching criteria.
                </td>
              </tr>
            ) : (
              filteredProducts.map((p) => {
                const margin = (((p.currentPrice - p.costPrice) / p.currentPrice) * 100).toFixed(1);
                const threshold = p.reorderThreshold || p.reorderPoint || 20;
                const stockPercent = Math.min(100, Math.round((p.stockQuantity / (p.maxStockLimit || 100)) * 100));
                const isLow = p.stockQuantity <= threshold;
                const isDanger = p.stockQuantity <= threshold * 0.5;
                const vel = p.demandVelocity || p.salesVelocity || 0;

                return (
                  <tr key={p.id} style={{ background: isDanger ? 'rgba(239,68,68,0.05)' : isLow ? 'rgba(245,158,11,0.03)' : 'transparent' }}>
                    {/* Product Info */}
                    <td>
                      <div style={{ fontWeight: 700, fontSize: '0.95rem' }}>{p.name}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)', display: 'flex', gap: '8px' }}>
                        <span>SKU: {p.sku}</span>
                        <span>•</span>
                        <span style={{ color: '#a855f7' }}>{p.category}</span>
                      </div>
                    </td>

                    {/* Current Price */}
                    <td>
                      <div style={{ fontSize: '1.1rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                        ${Number(p.currentPrice).toFixed(2)}
                      </div>
                      <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>
                        Margin: <span style={{ color: '#34d399', fontWeight: 600 }}>{margin}%</span>
                      </div>
                    </td>

                    {/* Stock Level Bar */}
                    <td style={{ minWidth: '130px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '4px' }}>
                        <span style={{ fontWeight: 700, color: isDanger ? '#f87171' : isLow ? '#fbbf24' : '#fff' }}>
                          {isDanger && <AlertTriangle size={11} style={{ display: 'inline', marginRight: '3px' }} />}
                          {p.stockQuantity} units
                        </span>
                        <span style={{ color: 'var(--text-dim)', fontSize: '0.68rem' }}>
                          thr: {threshold}
                        </span>
                      </div>
                      <div style={{ height: '6px', width: '100%', background: 'rgba(255,255,255,0.08)', borderRadius: '99px', overflow: 'hidden' }}>
                        <div
                          style={{
                            height: '100%',
                            width: `${stockPercent}%`,
                            background: isDanger ? 'linear-gradient(90deg, #dc2626, #f87171)' : isLow ? 'linear-gradient(90deg, #f59e0b, #fbbf24)' : 'linear-gradient(90deg, #6366f1, #10b981)',
                            borderRadius: '99px',
                            transition: 'width 0.4s ease',
                          }}
                        />
                      </div>
                    </td>

                    {/* Demand Velocity */}
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Zap size={13} color={vel > 10 ? '#fbbf24' : vel > 5 ? '#818cf8' : 'var(--text-muted)'} />
                        <span style={{
                          fontWeight: 700,
                          fontSize: '0.9rem',
                          color: vel > 10 ? '#fbbf24' : vel > 5 ? '#818cf8' : 'var(--text-main)',
                        }}>
                          {vel.toFixed(1)}
                        </span>
                        <span style={{ fontSize: '0.68rem', color: 'var(--text-dim)' }}>/day</span>
                      </div>
                    </td>

                    {/* Strategy */}
                    <td>
                      <span
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '6px',
                          padding: '4px 8px',
                          borderRadius: '8px',
                          fontSize: '0.7rem',
                          fontWeight: 700,
                          background: `rgba(255,255,255,0.05)`,
                          border: `1px solid ${getStrategyColor(p.activeStrategyType)}44`,
                          color: getStrategyColor(p.activeStrategyType),
                        }}
                      >
                        <Sparkles size={11} />
                        {p.activeStrategyType.replace(/_/g, ' ')}
                      </span>
                    </td>

                    {/* Status */}
                    <td>
                      <span className={`badge ${getStatusBadgeClass(p.status)}`}>
                        {p.status.replace(/_/g, ' ')}
                      </span>
                    </td>

                    {/* Actions */}
                    <td>
                      <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                        <button
                          onClick={() => handleSimulateSale(p)}
                          disabled={simulatingId === p.id || p.stockQuantity <= 0}
                          className="btn-primary"
                          style={{
                            padding: '5px 10px', fontSize: '0.72rem',
                            background: 'linear-gradient(135deg, #f59e0b, #d97706)',
                            opacity: p.stockQuantity <= 0 ? 0.5 : 1,
                          }}
                          title="Simulate 1 sale order"
                        >
                          <ShoppingCart size={12} />
                          {simulatingId === p.id ? '...' : 'Sell 1'}
                        </button>
                        <button
                          onClick={() => onOpenPricingModal(p)}
                          className="btn-primary"
                          style={{ padding: '5px 10px', fontSize: '0.72rem' }}
                          title="Evaluate Dynamic AI Price Recommendation"
                        >
                          <TrendingUp size={12} /> Reprice
                        </button>
                        <button
                          onClick={() => onOpenStockModal(p)}
                          className="btn-secondary"
                          style={{ padding: '5px 8px', fontSize: '0.72rem' }}
                          title="Update Inventory Stock"
                        >
                          <Edit3 size={12} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
