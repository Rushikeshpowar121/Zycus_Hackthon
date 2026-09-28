import React from 'react';
import { AnalyticsSummary, PriceAudit, Product } from '../types';
import { MetricCard } from '../components/MetricCard';
import { ProductTable } from '../components/ProductTable';
import { DollarSign, Layers, AlertTriangle, TrendingUp, Sparkles, Activity, Clock } from 'lucide-react';

interface DashboardProps {
  summary: AnalyticsSummary | null;
  products: Product[];
  categories: string[];
  recentAudits: PriceAudit[];
  onOpenPricingModal: (product: Product) => void;
  onOpenStockModal: (product: Product) => void;
}

export const Dashboard: React.FC<DashboardProps> = ({
  summary,
  products,
  categories,
  recentAudits,
  onOpenPricingModal,
  onOpenStockModal,
}) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Executive Metric Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(230px, 1fr))', gap: '16px' }}>
        <MetricCard
          title="Potential Revenue"
          value={summary ? `$${summary.totalPotentialRevenue.toLocaleString()}` : '$0'}
          subtitle={`Inv Value: $${summary?.totalInventoryValue.toLocaleString() || '0'}`}
          icon={DollarSign}
          color="16, 185, 129"
          trend="+14.2%"
          isPositive={true}
        />

        <MetricCard
          title="Total Products"
          value={summary?.totalProducts || 0}
          subtitle={`${summary?.optimalStockCount || 0} Optimal Stock`}
          icon={Layers}
          color="99, 102, 241"
        />

        <MetricCard
          title="Critical / Low Stock"
          value={(summary?.lowStockCount || 0) + (summary?.outOfStockCount || 0)}
          subtitle={`${summary?.outOfStockCount || 0} Out of Stock`}
          icon={AlertTriangle}
          color="245, 158, 11"
          trend={`${summary?.criticalRiskCount || 0} Critical`}
          isPositive={false}
        />

        <MetricCard
          title="AI Repriced Events"
          value={summary?.totalRepricedEvents || 0}
          subtitle={`Avg Profit Margin: ${summary?.averageProfitMarginPercent || 0}%`}
          icon={Sparkles}
          color="236, 72, 153"
          trend="Automated"
          isPositive={true}
        />
      </div>

      {/* Main Inventory Section */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Live Inventory & Dynamic Prices</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Real-time inventory levels, demand metrics & active strategy pricing.</p>
          </div>
        </div>

        <ProductTable
          products={products}
          categories={categories}
          onOpenPricingModal={onOpenPricingModal}
          onOpenStockModal={onOpenStockModal}
        />
      </div>

      {/* Recent Repricing Activity Feed */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
          <div style={{ padding: '6px', borderRadius: '8px', background: 'rgba(99,102,241,0.2)', color: '#818cf8' }}>
            <Clock size={18} />
          </div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Recent Dynamic Price Audit Activity</h3>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {recentAudits.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No recent repricing activity logged yet.</p>
          ) : (
            recentAudits.slice(0, 5).map((audit) => (
              <div
                key={audit.id}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '12px 16px',
                  background: 'rgba(0,0,0,0.25)',
                  borderRadius: 'var(--radius-md)',
                  border: '1px solid var(--border-color)',
                  flexWrap: 'wrap',
                  gap: '12px',
                }}
              >
                <div>
                  <div style={{ fontWeight: 700, fontSize: '0.88rem' }}>
                    SKU: {audit.productSku}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                    {audit.reason}
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>
                      ${audit.oldPrice?.toFixed(2)} → <span style={{ color: 'var(--accent-green)' }}>${audit.newPrice.toFixed(2)}</span>
                    </div>
                    <div style={{ fontSize: '0.7rem', color: audit.priceChangePercent >= 0 ? '#34d399' : '#fb7185' }}>
                      {audit.priceChangePercent >= 0 ? '+' : ''}{audit.priceChangePercent}%
                    </div>
                  </div>

                  <span
                    style={{
                      padding: '4px 8px',
                      borderRadius: '6px',
                      fontSize: '0.7rem',
                      fontWeight: 700,
                      background: 'rgba(99,102,241,0.15)',
                      color: '#818cf8',
                      border: '1px solid rgba(99,102,241,0.3)',
                    }}
                  >
                    {audit.strategyUsed}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

    </div>
  );
};
