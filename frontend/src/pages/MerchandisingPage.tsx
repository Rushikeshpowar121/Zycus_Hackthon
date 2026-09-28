import React, { useState } from 'react';
import { Product, PricingSuggestion, ReorderSuggestion } from '../types';
import { productService } from '../services/api';
import {
  ShoppingBag, Zap, AlertTriangle, TrendingUp, Package,
  Play, CheckCircle, Clock, ArrowRight, Activity
} from 'lucide-react';

interface MerchandisingPageProps {
  products: Product[];
  pricingSuggestions: PricingSuggestion[];
  reorderSuggestions: ReorderSuggestion[];
  onRefresh: () => void;
  onApprovePricing: (id: number) => void;
  onRejectPricing: (id: number) => void;
  onApproveReorder: (id: number) => void;
  onRejectReorder: (id: number) => void;
}

export const MerchandisingPage: React.FC<MerchandisingPageProps> = ({
  products,
  pricingSuggestions,
  reorderSuggestions,
  onRefresh,
  onApprovePricing,
  onRejectPricing,
  onApproveReorder,
  onRejectReorder,
}) => {
  const [simulatingId, setSimulatingId] = useState<number | null>(null);
  const [saleQty, setSaleQty] = useState<Record<number, number>>({});
  const [feedMessages, setFeedMessages] = useState<{ id: number; text: string; color: string; ts: string }[]>([]);

  const addFeed = (text: string, color = '#34d399') => {
    const ts = new Date().toLocaleTimeString();
    setFeedMessages(prev => [{ id: Date.now(), text, color, ts }, ...prev.slice(0, 9)]);
  };

  const handleSimulateSale = async (product: Product) => {
    const qty = saleQty[product.id] || 1;
    setSimulatingId(product.id);
    try {
      await productService.simulateSale(product.id, qty);
      addFeed(`📉 Simulated ${qty}x sale on "${product.name}" — Agentic loop evaluating...`, '#fbbf24');
      await new Promise(r => setTimeout(r, 800));
      await onRefresh();
      addFeed(`✅ Inventory updated. Check Pending Recommendations above.`, '#34d399');
    } catch (err: any) {
      addFeed(`❌ Error: ${err.message || 'Sale simulation failed'}`, '#f87171');
    } finally {
      setSimulatingId(null);
    }
  };

  const lowStockProducts = products.filter(p =>
    p.stockQuantity <= (p.reorderThreshold || p.reorderPoint || 20)
  );
  const pendingTotal = pricingSuggestions.filter(s => s.status === 'PENDING').length
    + reorderSuggestions.filter(s => s.status === 'PENDING').length;

  const getStockPct = (p: Product) => {
    const threshold = p.reorderThreshold || p.reorderPoint || 20;
    if (p.stockQuantity <= 0) return 0;
    if (p.stockQuantity < threshold) return Math.round((p.stockQuantity / threshold) * 50);
    return Math.min(100, Math.round((p.stockQuantity / (p.maxStockLimit || 100)) * 100));
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>

      {/* Header */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '8px' }}>
          <div style={{ padding: '10px', borderRadius: '12px', background: 'linear-gradient(135deg, #f59e0b, #d97706)', color: '#fff' }}>
            <ShoppingBag size={24} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 900, margin: 0 }}>Merchandising Console</h2>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', margin: 0 }}>
              Simulate sales, watch the agentic loop fire, and approve or reject AI recommendations.
            </p>
          </div>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: '12px', alignItems: 'center' }}>
            {pendingTotal > 0 && (
              <div style={{
                padding: '8px 16px', borderRadius: '99px',
                background: 'rgba(245,158,11,0.2)', border: '1px solid rgba(245,158,11,0.4)',
                color: '#fbbf24', fontWeight: 700, fontSize: '0.85rem',
                animation: 'pulse 1.5s infinite',
              }}>
                ⚡ {pendingTotal} Pending Approval
              </div>
            )}
          </div>
        </div>

        {/* Agentic Loop Diagram */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap',
          marginTop: '16px', padding: '14px 18px', borderRadius: '10px',
          background: 'rgba(0,0,0,0.25)', border: '1px solid var(--border-color)',
        }}>
          {[
            { icon: <Activity size={14} />, label: 'Inventory Signal', color: '#f87171' },
            { icon: <ArrowRight size={14} />, label: '', color: 'var(--text-dim)' },
            { icon: <Zap size={14} />, label: 'Agentic Loop Fires', color: '#fbbf24' },
            { icon: <ArrowRight size={14} />, label: '', color: 'var(--text-dim)' },
            { icon: <Package size={14} />, label: 'AI Generates Suggestions', color: '#818cf8' },
            { icon: <ArrowRight size={14} />, label: '', color: 'var(--text-dim)' },
            { icon: <CheckCircle size={14} />, label: 'Human Approves', color: '#34d399' },
            { icon: <ArrowRight size={14} />, label: '', color: 'var(--text-dim)' },
            { icon: <TrendingUp size={14} />, label: 'Price/Stock Updated', color: '#10b981' },
          ].map((step, i) => (
            <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '5px', color: step.color, fontSize: '0.75rem', fontWeight: 600 }}>
              {step.icon}
              {step.label && <span>{step.label}</span>}
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(500px, 1fr))', gap: '20px' }}>

        {/* Simulate Sale Panel */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
            <div style={{ padding: '6px', borderRadius: '8px', background: 'rgba(239,68,68,0.15)', color: '#f87171' }}>
              <Play size={18} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 800, margin: 0 }}>Demo Path — Simulate Sales</h3>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                Post orders to trigger the inventory-low or demand-spike agentic loop.
              </p>
            </div>
          </div>

          {/* Low-stock products pinned first for demo */}
          {[...products]
            .sort((a, b) => {
              const aTh = a.reorderThreshold || a.reorderPoint || 20;
              const bTh = b.reorderThreshold || b.reorderPoint || 20;
              const aRatio = a.stockQuantity / aTh;
              const bRatio = b.stockQuantity / bTh;
              return aRatio - bRatio;
            })
            .map((p) => {
              const threshold = p.reorderThreshold || p.reorderPoint || 20;
              const isLow = p.stockQuantity <= threshold;
              const isDanger = p.stockQuantity <= threshold * 0.5;
              const pct = getStockPct(p);
              const vel = p.demandVelocity || p.salesVelocity || 0;

              return (
                <div
                  key={p.id}
                  style={{
                    marginBottom: '10px',
                    padding: '14px 16px',
                    borderRadius: 'var(--radius-md)',
                    background: isDanger ? 'rgba(239,68,68,0.08)' : isLow ? 'rgba(245,158,11,0.08)' : 'rgba(0,0,0,0.2)',
                    border: isDanger ? '1px solid rgba(239,68,68,0.3)' : isLow ? '1px solid rgba(245,158,11,0.25)' : '1px solid var(--border-color)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px', gap: '8px', flexWrap: 'wrap' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        {isDanger && <AlertTriangle size={14} color="#f87171" />}
                        {isLow && !isDanger && <AlertTriangle size={14} color="#fbbf24" />}
                        <span style={{ fontWeight: 700, fontSize: '0.88rem' }}>{p.name}</span>
                        <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>{p.sku}</span>
                      </div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                        Stock: <strong style={{ color: isDanger ? '#f87171' : isLow ? '#fbbf24' : '#34d399' }}>{p.stockQuantity}</strong> / Threshold: <strong>{threshold}</strong>
                        {' · '}Velocity: <strong style={{ color: '#818cf8' }}>{vel}/day</strong>
                        {' · '}Price: <strong style={{ color: '#10b981' }}>${Number(p.currentPrice).toFixed(2)}</strong>
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <input
                        type="number"
                        min={1}
                        max={20}
                        value={saleQty[p.id] || 1}
                        onChange={(e) => setSaleQty(prev => ({ ...prev, [p.id]: Math.max(1, Number(e.target.value)) }))}
                        style={{
                          width: '52px', padding: '6px 8px', background: 'var(--bg-input)',
                          border: '1px solid var(--border-color)', color: '#fff', borderRadius: '6px',
                          fontSize: '0.85rem', textAlign: 'center',
                        }}
                        title="Quantity to sell"
                      />
                      <button
                        onClick={() => handleSimulateSale(p)}
                        disabled={simulatingId === p.id || p.stockQuantity <= 0}
                        className="btn-primary"
                        style={{
                          padding: '6px 14px', fontSize: '0.78rem',
                          background: isDanger ? 'linear-gradient(135deg, #dc2626, #b91c1c)' : isLow ? 'linear-gradient(135deg, #f59e0b, #d97706)' : 'linear-gradient(135deg, #6366f1, #4f46e5)',
                          opacity: p.stockQuantity <= 0 ? 0.5 : 1,
                        }}
                      >
                        <Play size={13} />
                        {simulatingId === p.id ? 'Selling...' : `Sell ${saleQty[p.id] || 1}`}
                      </button>
                    </div>
                  </div>

                  {/* Stock bar */}
                  <div style={{ height: '5px', background: 'rgba(255,255,255,0.06)', borderRadius: '99px', overflow: 'hidden' }}>
                    <div style={{
                      height: '100%', width: `${pct}%`, borderRadius: '99px',
                      background: isDanger ? 'linear-gradient(90deg, #dc2626, #f87171)' : isLow ? 'linear-gradient(90deg, #f59e0b, #fbbf24)' : 'linear-gradient(90deg, #6366f1, #10b981)',
                      transition: 'width 0.4s ease',
                    }} />
                  </div>

                  {(isDanger || isLow) && (
                    <div style={{ marginTop: '6px', fontSize: '0.68rem', color: isDanger ? '#f87171' : '#fbbf24', fontWeight: 600 }}>
                      {isDanger
                        ? '⚠️ CRITICAL: Next sale will trigger INVENTORY_LOW agentic loop immediately'
                        : '⚡ LOW STOCK: Already below threshold — sell to trigger AI suggestions'}
                    </div>
                  )}
                </div>
              );
            })}
        </div>

        {/* Live Activity Feed */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '18px' }}>
            <div style={{ padding: '6px', borderRadius: '8px', background: 'rgba(99,102,241,0.15)', color: '#818cf8' }}>
              <Activity size={18} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.05rem', fontWeight: 800, margin: 0 }}>Live Agentic Loop Feed</h3>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', margin: '2px 0 0' }}>
                Real-time event log as inventory signals are processed.
              </p>
            </div>
          </div>

          {feedMessages.length === 0 ? (
            <div style={{
              textAlign: 'center', padding: '40px 20px',
              color: 'var(--text-dim)', fontSize: '0.82rem',
            }}>
              <Clock size={32} style={{ opacity: 0.3, marginBottom: '10px' }} />
              <div>No activity yet. Simulate a sale to watch the agentic loop fire.</div>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {feedMessages.map((msg) => (
                <div
                  key={msg.id}
                  style={{
                    padding: '10px 14px',
                    borderRadius: '8px',
                    background: 'rgba(0,0,0,0.25)',
                    borderLeft: `3px solid ${msg.color}`,
                    animation: 'slideIn 0.3s ease',
                  }}
                >
                  <div style={{ fontSize: '0.82rem', color: msg.color, fontWeight: 600 }}>
                    {msg.text}
                  </div>
                  <div style={{ fontSize: '0.68rem', color: 'var(--text-dim)', marginTop: '2px' }}>
                    {msg.ts}
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Summary stats */}
          <div style={{
            marginTop: '16px', padding: '14px', borderRadius: '10px',
            background: 'rgba(0,0,0,0.2)', border: '1px solid var(--border-color)',
            display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px',
          }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '1.4rem', fontWeight: 900, color: '#f87171' }}>
                {products.filter(p => p.stockQuantity <= (p.reorderThreshold || p.reorderPoint || 20)).length}
              </div>
              <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>Below Threshold</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '1.4rem', fontWeight: 900, color: '#fbbf24' }}>
                {pricingSuggestions.filter(s => s.status === 'PENDING').length}
              </div>
              <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>Price Pending</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '1.4rem', fontWeight: 900, color: '#818cf8' }}>
                {reorderSuggestions.filter(s => s.status === 'PENDING').length}
              </div>
              <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)' }}>Restock Pending</div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
};
