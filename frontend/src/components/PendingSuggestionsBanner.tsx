import React from 'react';
import { PricingSuggestion, ReorderSuggestion, Product } from '../types';
import { Sparkles, CheckCircle2, XCircle, ShoppingCart, TrendingUp, AlertTriangle } from 'lucide-react';

interface PendingSuggestionsBannerProps {
  pricingSuggestions: PricingSuggestion[];
  reorderSuggestions: ReorderSuggestion[];
  products: Product[];
  onApprovePricing: (id: number) => void;
  onRejectPricing: (id: number) => void;
  onApproveReorder: (id: number) => void;
  onRejectReorder: (id: number) => void;
}

export const PendingSuggestionsBanner: React.FC<PendingSuggestionsBannerProps> = ({
  pricingSuggestions,
  reorderSuggestions,
  products,
  onApprovePricing,
  onRejectPricing,
  onApproveReorder,
  onRejectReorder,
}) => {
  const pendingPricing = pricingSuggestions.filter((s) => s.status === 'PENDING');
  const pendingReorder = reorderSuggestions.filter((s) => s.status === 'PENDING');

  if (pendingPricing.length === 0 && pendingReorder.length === 0) {
    return null;
  }

  const getProductName = (productId: number) => {
    const p = products.find((prod) => prod.id === productId);
    return p ? `${p.name} (${p.sku})` : `Product #${productId}`;
  };

  return (
    <div
      className="glass-panel"
      style={{
        padding: '20px 24px',
        marginBottom: '24px',
        border: '1px solid rgba(245, 158, 11, 0.4)',
        background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.08) 0%, rgba(99, 102, 241, 0.08) 100%)',
        borderRadius: 'var(--radius-lg)',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
        <div style={{ padding: '6px', borderRadius: '8px', background: 'rgba(245, 158, 11, 0.2)', color: '#fbbf24' }}>
          <Sparkles size={20} />
        </div>
        <div>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
            Agentic Recommendations Requiring Human Approval
            <span
              style={{
                fontSize: '0.72rem',
                padding: '2px 8px',
                borderRadius: '99px',
                background: 'rgba(245, 158, 11, 0.2)',
                color: '#fbbf24',
                border: '1px solid rgba(245, 158, 11, 0.4)',
              }}
            >
              {pendingPricing.length + pendingReorder.length} PENDING ACTION
            </span>
          </h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: '2px 0 0 0' }}>
            The event-driven engine evaluated inventory signals and queued suggestions. Review & approve to apply live changes.
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {/* Pending Pricing Suggestions */}
        {pendingPricing.map((s) => (
          <div
            key={`pricing-${s.id}`}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '14px 18px',
              background: 'rgba(0, 0, 0, 0.35)',
              border: '1px solid rgba(99, 102, 241, 0.3)',
              borderRadius: 'var(--radius-md)',
              flexWrap: 'wrap',
              gap: '12px',
            }}
          >
            <div style={{ flex: 1, minWidth: '260px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                <span style={{ fontSize: '0.7rem', fontWeight: 700, padding: '2px 6px', borderRadius: '4px', background: '#6366f1', color: '#fff' }}>
                  PRICING SUGGESTION
                </span>
                <span style={{ fontSize: '0.88rem', fontWeight: 700 }}>{getProductName(s.productId)}</span>
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
                {s.reason}
              </p>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '0.95rem', fontWeight: 800 }}>
                  ${s.oldPrice?.toFixed(2)} → <span style={{ color: '#34d399' }}>${s.suggestedPrice?.toFixed(2)}</span>
                </div>
                <div style={{ fontSize: '0.72rem', color: '#818cf8', fontWeight: 600 }}>
                  Confidence: {((s.confidenceScore || 0.9) * 100).toFixed(0)}%
                </div>
              </div>

              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={() => onApprovePricing(s.id)}
                  className="btn-primary"
                  style={{
                    padding: '8px 14px',
                    fontSize: '0.8rem',
                    background: 'linear-gradient(135deg, #10b981, #059669)',
                  }}
                >
                  <CheckCircle2 size={15} /> Approve Price
                </button>
                <button
                  onClick={() => onRejectPricing(s.id)}
                  style={{
                    padding: '8px 12px',
                    fontSize: '0.8rem',
                    borderRadius: 'var(--radius-md)',
                    background: 'rgba(239, 68, 68, 0.15)',
                    border: '1px solid rgba(239, 68, 68, 0.3)',
                    color: '#f87171',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  <XCircle size={15} /> Reject
                </button>
              </div>
            </div>
          </div>
        ))}

        {/* Pending Reorder Suggestions */}
        {pendingReorder.map((r) => (
          <div
            key={`reorder-${r.id}`}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '14px 18px',
              background: 'rgba(0, 0, 0, 0.35)',
              border: '1px solid rgba(245, 158, 11, 0.3)',
              borderRadius: 'var(--radius-md)',
              flexWrap: 'wrap',
              gap: '12px',
            }}
          >
            <div style={{ flex: 1, minWidth: '260px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                <span style={{ fontSize: '0.7rem', fontWeight: 700, padding: '2px 6px', borderRadius: '4px', background: '#f59e0b', color: '#000' }}>
                  REORDER SUGGESTION
                </span>
                <span style={{ fontSize: '0.88rem', fontWeight: 700 }}>{getProductName(r.productId)}</span>
              </div>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
                {r.reason}
              </p>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '0.95rem', fontWeight: 800, color: '#fbbf24' }}>
                  +{r.suggestedQuantity} Units
                </div>
                <div style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>
                  Est Lead Time: {r.suggestedLeadTimeDays || 7} Days
                </div>
              </div>

              <div style={{ display: 'flex', gap: '8px' }}>
                <button
                  onClick={() => onApproveReorder(r.id)}
                  className="btn-primary"
                  style={{
                    padding: '8px 14px',
                    fontSize: '0.8rem',
                    background: 'linear-gradient(135deg, #f59e0b, #d97706)',
                  }}
                >
                  <ShoppingCart size={15} /> Approve Restock
                </button>
                <button
                  onClick={() => onRejectReorder(r.id)}
                  style={{
                    padding: '8px 12px',
                    fontSize: '0.8rem',
                    borderRadius: 'var(--radius-md)',
                    background: 'rgba(239, 68, 68, 0.15)',
                    border: '1px solid rgba(239, 68, 68, 0.3)',
                    color: '#f87171',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  <XCircle size={15} /> Reject
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
