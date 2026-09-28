import React, { useState } from 'react';
import { PricingSuggestion, ReorderSuggestion, Product, TriggerReason } from '../types';
import {
  Sparkles, CheckCircle2, XCircle, ShoppingCart, TrendingUp,
  AlertTriangle, Zap, ChevronDown, ChevronUp, Bot, BarChart2
} from 'lucide-react';

interface PendingSuggestionsBannerProps {
  pricingSuggestions: PricingSuggestion[];
  reorderSuggestions: ReorderSuggestion[];
  products: Product[];
  onApprovePricing: (id: number) => void;
  onRejectPricing: (id: number) => void;
  onApproveReorder: (id: number) => void;
  onRejectReorder: (id: number) => void;
}

const getTriggerBadge = (trigger?: TriggerReason) => {
  switch (trigger) {
    case 'INVENTORY_LOW':
    case 'LOW_STOCK':
      return {
        label: '🔻 INVENTORY LOW',
        bg: 'rgba(239, 68, 68, 0.15)',
        border: 'rgba(239, 68, 68, 0.4)',
        color: '#f87171',
      };
    case 'DEMAND_SPIKE':
    case 'DEMAND_SURGE':
      return {
        label: '⚡ DEMAND SPIKE',
        bg: 'rgba(251, 191, 36, 0.15)',
        border: 'rgba(251, 191, 36, 0.4)',
        color: '#fbbf24',
      };
    case 'MANUAL':
      return {
        label: '👤 MANUAL',
        bg: 'rgba(99, 102, 241, 0.15)',
        border: 'rgba(99, 102, 241, 0.4)',
        color: '#818cf8',
      };
    default:
      return {
        label: '🤖 AUTO',
        bg: 'rgba(16, 185, 129, 0.15)',
        border: 'rgba(16, 185, 129, 0.4)',
        color: '#34d399',
      };
  }
};

export const PendingSuggestionsBanner: React.FC<PendingSuggestionsBannerProps> = ({
  pricingSuggestions,
  reorderSuggestions,
  products,
  onApprovePricing,
  onRejectPricing,
  onApproveReorder,
  onRejectReorder,
}) => {
  const [expandedId, setExpandedId] = useState<string | null>(null);

  const pendingPricing = pricingSuggestions.filter((s) => s.status === 'PENDING');
  const pendingReorder = reorderSuggestions.filter((s) => s.status === 'PENDING');

  if (pendingPricing.length === 0 && pendingReorder.length === 0) {
    return (
      <div
        className="glass-panel"
        style={{
          padding: '18px 24px',
          border: '1px solid rgba(16, 185, 129, 0.2)',
          background: 'linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(6, 78, 59, 0.05) 100%)',
          borderRadius: 'var(--radius-lg)',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
        }}
      >
        <div style={{ padding: '6px', borderRadius: '8px', background: 'rgba(16,185,129,0.15)', color: '#34d399' }}>
          <CheckCircle2 size={18} />
        </div>
        <div>
          <div style={{ fontWeight: 700, fontSize: '0.88rem', color: '#34d399' }}>All Clear — No Pending AI Recommendations</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>
            The agentic loop is monitoring inventory signals. Simulate a sale below to trigger AI suggestions.
          </div>
        </div>
      </div>
    );
  }

  const getProductName = (productId: number) => {
    const p = products.find((prod) => prod.id === productId);
    return p ? p.name : `Product #${productId}`;
  };

  const getProductSku = (productId: number) => {
    const p = products.find((prod) => prod.id === productId);
    return p ? p.sku : '';
  };

  const toggleExpand = (key: string) => {
    setExpandedId(prev => prev === key ? null : key);
  };

  const priceChangePct = (s: PricingSuggestion) => {
    if (!s.oldPrice || !s.suggestedPrice || s.oldPrice === 0) return 0;
    return (((s.suggestedPrice - s.oldPrice) / s.oldPrice) * 100).toFixed(1);
  };

  return (
    <div
      className="glass-panel"
      style={{
        padding: '20px 24px',
        border: '1px solid rgba(245, 158, 11, 0.35)',
        background: 'linear-gradient(135deg, rgba(245, 158, 11, 0.06) 0%, rgba(99, 102, 241, 0.06) 100%)',
        borderRadius: 'var(--radius-lg)',
      }}
    >
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
        <div style={{ padding: '8px', borderRadius: '10px', background: 'linear-gradient(135deg, #f59e0b, #d97706)', color: '#fff' }}>
          <Bot size={20} />
        </div>
        <div style={{ flex: 1 }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 800, margin: 0, display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
            🤖 Agentic Recommendations — Awaiting Human Approval
            <span
              style={{
                fontSize: '0.72rem',
                padding: '3px 10px',
                borderRadius: '99px',
                background: 'rgba(245, 158, 11, 0.2)',
                color: '#fbbf24',
                border: '1px solid rgba(245, 158, 11, 0.4)',
                fontWeight: 700,
                animation: 'pulse 2s infinite',
              }}
            >
              {pendingPricing.length + pendingReorder.length} PENDING ACTION
            </span>
          </h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', margin: '3px 0 0 0' }}>
            The event-driven agentic loop detected inventory signals and queued recommendations. <strong>Prices don't change until you approve.</strong>
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>

        {/* Pending Pricing Suggestions */}
        {pendingPricing.map((s) => {
          const key = `pricing-${s.id}`;
          const badge = getTriggerBadge(s.triggerReason);
          const isExpanded = expandedId === key;
          const pct = priceChangePct(s);
          const isIncrease = Number(pct) > 0;

          return (
            <div
              key={key}
              style={{
                background: 'rgba(0, 0, 0, 0.35)',
                border: '1px solid rgba(99, 102, 241, 0.3)',
                borderLeft: '3px solid #6366f1',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
              }}
            >
              {/* Main Row */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '14px 18px',
                  flexWrap: 'wrap',
                  gap: '12px',
                  cursor: 'pointer',
                }}
                onClick={() => toggleExpand(key)}
              >
                <div style={{ flex: 1, minWidth: '220px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', flexWrap: 'wrap' }}>
                    <span style={{
                      fontSize: '0.68rem', fontWeight: 700, padding: '2px 7px', borderRadius: '4px',
                      background: '#6366f1', color: '#fff', letterSpacing: '0.05em',
                    }}>
                      💰 PRICE SUGGESTION
                    </span>
                    {/* Trigger Badge */}
                    <span style={{
                      fontSize: '0.68rem', fontWeight: 700, padding: '2px 7px', borderRadius: '4px',
                      background: badge.bg, border: `1px solid ${badge.border}`, color: badge.color,
                    }}>
                      {badge.label}
                    </span>
                    <span style={{ fontSize: '0.88rem', fontWeight: 700 }}>
                      {getProductName(s.productId)}
                    </span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {getProductSku(s.productId)}
                    </span>
                  </div>
                  <p style={{
                    fontSize: '0.78rem', color: 'var(--text-muted)', margin: 0,
                    maxWidth: '420px',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  }}>
                    {s.reason}
                  </p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  {/* Price Change */}
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '0.9rem', fontWeight: 800 }}>
                      ${Number(s.oldPrice).toFixed(2)} →{' '}
                      <span style={{ color: isIncrease ? '#34d399' : '#f87171', fontSize: '1rem' }}>
                        ${Number(s.suggestedPrice).toFixed(2)}
                      </span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '6px' }}>
                      <span style={{
                        fontSize: '0.72rem', fontWeight: 700, padding: '1px 6px', borderRadius: '4px',
                        background: isIncrease ? 'rgba(52,211,153,0.15)' : 'rgba(248,113,113,0.15)',
                        color: isIncrease ? '#34d399' : '#f87171',
                      }}>
                        {isIncrease ? '+' : ''}{pct}%
                      </span>
                      <span style={{ fontSize: '0.68rem', color: '#818cf8' }}>
                        {((s.confidenceScore || 0) * 100).toFixed(0)}% confident
                      </span>
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={(e) => { e.stopPropagation(); onApprovePricing(s.id); }}
                      className="btn-primary"
                      style={{ padding: '8px 14px', fontSize: '0.78rem', background: 'linear-gradient(135deg, #10b981, #059669)', gap: '4px' }}
                    >
                      <CheckCircle2 size={14} /> Approve Price
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); onRejectPricing(s.id); }}
                      style={{
                        padding: '8px 12px', fontSize: '0.78rem', borderRadius: 'var(--radius-md)',
                        background: 'rgba(239, 68, 68, 0.12)', border: '1px solid rgba(239, 68, 68, 0.3)',
                        color: '#f87171', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px',
                      }}
                    >
                      <XCircle size={14} /> Reject
                    </button>
                    <button
                      style={{
                        padding: '8px 10px', borderRadius: 'var(--radius-md)',
                        background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)',
                        color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center',
                      }}
                      onClick={(e) => { e.stopPropagation(); toggleExpand(key); }}
                      title="View AI reasoning"
                    >
                      {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </button>
                  </div>
                </div>
              </div>

              {/* Expanded Reasoning */}
              {isExpanded && (
                <div style={{
                  padding: '12px 18px 16px',
                  borderTop: '1px solid rgba(99,102,241,0.15)',
                  background: 'rgba(0,0,0,0.2)',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                    <Sparkles size={14} color="#818cf8" />
                    <span style={{ fontSize: '0.72rem', fontWeight: 700, color: '#818cf8', letterSpacing: '0.05em' }}>
                      AI REASONING
                    </span>
                  </div>
                  <p style={{
                    fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.6,
                    background: 'rgba(99,102,241,0.07)', padding: '10px 14px', borderRadius: '8px',
                    borderLeft: '3px solid #6366f1', margin: 0,
                  }}>
                    {s.reason}
                  </p>
                  <div style={{ marginTop: '10px', display: 'flex', gap: '16px', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                    <span>Direction: <strong style={{ color: isIncrease ? '#34d399' : '#f87171' }}>{s.direction}</strong></span>
                    <span>Confidence: <strong style={{ color: '#818cf8' }}>{((s.confidenceScore || 0) * 100).toFixed(0)}%</strong></span>
                    <span>Queued: <strong>{new Date(s.createdAt).toLocaleTimeString()}</strong></span>
                  </div>
                </div>
              )}
            </div>
          );
        })}

        {/* Pending Reorder Suggestions */}
        {pendingReorder.map((r) => {
          const key = `reorder-${r.id}`;
          const badge = getTriggerBadge(r.triggerReason);
          const isExpanded = expandedId === key;

          return (
            <div
              key={key}
              style={{
                background: 'rgba(0, 0, 0, 0.35)',
                border: '1px solid rgba(245, 158, 11, 0.3)',
                borderLeft: '3px solid #f59e0b',
                borderRadius: 'var(--radius-md)',
                overflow: 'hidden',
              }}
            >
              {/* Main Row */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '14px 18px',
                  flexWrap: 'wrap',
                  gap: '12px',
                  cursor: 'pointer',
                }}
                onClick={() => toggleExpand(key)}
              >
                <div style={{ flex: 1, minWidth: '220px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px', flexWrap: 'wrap' }}>
                    <span style={{
                      fontSize: '0.68rem', fontWeight: 700, padding: '2px 7px', borderRadius: '4px',
                      background: '#f59e0b', color: '#000', letterSpacing: '0.05em',
                    }}>
                      📦 REORDER SUGGESTION
                    </span>
                    {/* Trigger Badge */}
                    <span style={{
                      fontSize: '0.68rem', fontWeight: 700, padding: '2px 7px', borderRadius: '4px',
                      background: badge.bg, border: `1px solid ${badge.border}`, color: badge.color,
                    }}>
                      {badge.label}
                    </span>
                    <span style={{ fontSize: '0.88rem', fontWeight: 700 }}>
                      {getProductName(r.productId)}
                    </span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {getProductSku(r.productId)}
                    </span>
                  </div>
                  <p style={{
                    fontSize: '0.78rem', color: 'var(--text-muted)', margin: 0,
                    maxWidth: '420px',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap',
                  }}>
                    {r.reason}
                  </p>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                  {/* Restock Info */}
                  <div style={{ textAlign: 'right' }}>
                    <div style={{ fontSize: '1.1rem', fontWeight: 800, color: '#fbbf24' }}>
                      +{r.suggestedQuantity} units
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '6px' }}>
                      <span style={{ fontSize: '0.68rem', color: 'var(--text-dim)' }}>
                        Lead: {r.suggestedLeadTimeDays || 7}d
                      </span>
                      {r.confidenceScore && (
                        <span style={{ fontSize: '0.68rem', color: '#818cf8' }}>
                          {((r.confidenceScore) * 100).toFixed(0)}% conf
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Action Buttons */}
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                      onClick={(e) => { e.stopPropagation(); onApproveReorder(r.id); }}
                      className="btn-primary"
                      style={{ padding: '8px 14px', fontSize: '0.78rem', background: 'linear-gradient(135deg, #f59e0b, #d97706)', gap: '4px' }}
                    >
                      <ShoppingCart size={14} /> Approve Restock
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); onRejectReorder(r.id); }}
                      style={{
                        padding: '8px 12px', fontSize: '0.78rem', borderRadius: 'var(--radius-md)',
                        background: 'rgba(239, 68, 68, 0.12)', border: '1px solid rgba(239, 68, 68, 0.3)',
                        color: '#f87171', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px',
                      }}
                    >
                      <XCircle size={14} /> Reject
                    </button>
                    <button
                      style={{
                        padding: '8px 10px', borderRadius: 'var(--radius-md)',
                        background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border-color)',
                        color: 'var(--text-muted)', cursor: 'pointer', display: 'flex', alignItems: 'center',
                      }}
                      onClick={(e) => { e.stopPropagation(); toggleExpand(key); }}
                      title="View reasoning"
                    >
                      {isExpanded ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </button>
                  </div>
                </div>
              </div>

              {/* Expanded Reasoning */}
              {isExpanded && (
                <div style={{
                  padding: '12px 18px 16px',
                  borderTop: '1px solid rgba(245,158,11,0.15)',
                  background: 'rgba(0,0,0,0.2)',
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
                    <BarChart2 size={14} color="#f59e0b" />
                    <span style={{ fontSize: '0.72rem', fontWeight: 700, color: '#f59e0b', letterSpacing: '0.05em' }}>
                      REORDER REASONING
                    </span>
                  </div>
                  <p style={{
                    fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.6,
                    background: 'rgba(245,158,11,0.07)', padding: '10px 14px', borderRadius: '8px',
                    borderLeft: '3px solid #f59e0b', margin: 0,
                  }}>
                    {r.reason}
                  </p>
                  <div style={{ marginTop: '10px', display: 'flex', gap: '16px', fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                    <span>Quantity: <strong style={{ color: '#fbbf24' }}>{r.suggestedQuantity} units</strong></span>
                    <span>Lead Time: <strong>{r.suggestedLeadTimeDays || 7} days</strong></span>
                    <span>Queued: <strong>{new Date(r.createdAt).toLocaleTimeString()}</strong></span>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>

      {/* Footer note */}
      <div style={{ marginTop: '14px', fontSize: '0.72rem', color: 'var(--text-dim)', display: 'flex', alignItems: 'center', gap: '6px' }}>
        <Zap size={12} color="#6366f1" />
        Suggestions are generated asynchronously by the agentic loop. Approving a price changes the live product price. Approving restock simulates inbound inventory.
      </div>
    </div>
  );
};
