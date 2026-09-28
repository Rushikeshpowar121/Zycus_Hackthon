import React, { useState } from 'react';
import { Product, PricingCalculationResult } from '../types';
import { simulationService, pricingService } from '../services/api';
import { Play, Sparkles, TrendingUp, AlertTriangle, ShieldCheck, Zap, ArrowRight } from 'lucide-react';

interface SimulatorPageProps {
  products: Product[];
  onRefresh: () => void;
}

export const SimulatorPage: React.FC<SimulatorPageProps> = ({ products, onRefresh }) => {
  const [selectedProductId, setSelectedProductId] = useState<number>(products[0]?.id || 1);
  const [simulationType, setSimulationType] = useState<string>('DEMAND_SURGE');
  const [viewMultiplier, setViewMultiplier] = useState<number>(5);
  const [salesVelocityBoost, setSalesVelocityBoost] = useState<number>(20);
  const [competitorAdjustment, setCompetitorAdjustment] = useState<number>(120);
  const [reduceExpiryDays, setReduceExpiryDays] = useState<number>(10);

  const [result, setResult] = useState<PricingCalculationResult | null>(null);
  const [simulating, setSimulating] = useState<boolean>(false);
  const [applying, setApplying] = useState<boolean>(false);

  const targetProduct = products.find((p) => p.id === selectedProductId) || products[0];

  const handleRunSimulation = async () => {
    if (!targetProduct) return;
    setSimulating(true);
    try {
      const res = await simulationService.run({
        productId: targetProduct.id,
        simulationType,
        viewMultiplier,
        salesVelocityBoost,
        competitorPriceAdjustment: competitorAdjustment,
        reduceExpiryDaysBy: reduceExpiryDays,
      });
      setResult(res);
      onRefresh();
    } catch (err) {
      console.error(err);
    } finally {
      setSimulating(false);
    }
  };

  const handleApplySimulatedPrice = async () => {
    if (!targetProduct || !result) return;
    setApplying(true);
    try {
      await pricingService.applyRecommendation(targetProduct.id, result);
      onRefresh();
    } catch (err) {
      console.error(err);
    } finally {
      setApplying(false);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <div style={{ padding: '8px', borderRadius: '10px', background: 'linear-gradient(135deg, #a855f7 0%, #ec4899 100%)', color: '#fff' }}>
            <Zap size={22} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>AI Dynamic Pricing Strategy Simulator</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Stress-test market scenarios (Demand Surge, Competitor Undercutting, Aging Inventory Expiry) and observe automated AI response pricing.
            </p>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
        
        {/* Controls Card */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '16px' }}>Simulation Parameters</h3>

          {/* Select Product */}
          <div style={{ marginBottom: '16px' }}>
            <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: 600 }}>
              Select Target Inventory Product:
            </label>
            <select
              value={selectedProductId}
              onChange={(e) => setSelectedProductId(Number(e.target.value))}
              style={{
                width: '100%',
                padding: '10px',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                color: '#fff',
                fontSize: '0.88rem',
              }}
            >
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} (${p.currentPrice.toFixed(2)}) - [{p.category}]
                </option>
              ))}
            </select>
          </div>

          {/* Scenario Picker */}
          <div style={{ marginBottom: '16px' }}>
            <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: 600 }}>
              Simulation Event Scenario:
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '8px' }}>
              {[
                { id: 'DEMAND_SURGE', label: 'Demand Surge' },
                { id: 'COMPETITOR_PRICE_DROP', label: 'Competitor Cut' },
                { id: 'AGING_SPIKE', label: 'Aging / Expiry Spike' },
                { id: 'FLASH_SALE', label: 'Flash Viral Traffic' },
              ].map((s) => (
                <button
                  key={s.id}
                  type="button"
                  onClick={() => setSimulationType(s.id)}
                  className={simulationType === s.id ? 'btn-primary' : 'btn-secondary'}
                  style={{ fontSize: '0.78rem', justifyContent: 'center', padding: '8px' }}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>

          {/* Dynamic Scenario Settings */}
          {simulationType === 'DEMAND_SURGE' && (
            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: '8px', marginBottom: '16px' }}>
              <label style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                Traffic Multiplier: {viewMultiplier}x Views
              </label>
              <input
                type="range"
                min={2}
                max={10}
                value={viewMultiplier}
                onChange={(e) => setViewMultiplier(Number(e.target.value))}
                style={{ width: '100%' }}
              />
            </div>
          )}

          {simulationType === 'COMPETITOR_PRICE_DROP' && (
            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: '8px', marginBottom: '16px' }}>
              <label style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
                Simulated Competitor Benchmark Price: ${competitorAdjustment}
              </label>
              <input
                type="number"
                value={competitorAdjustment}
                onChange={(e) => setCompetitorAdjustment(Number(e.target.value))}
                style={{ width: '100%', padding: '8px', background: 'var(--bg-input)', border: '1px solid var(--border-color)', color: '#fff', borderRadius: '6px' }}
              />
            </div>
          )}

          <button
            onClick={handleRunSimulation}
            disabled={simulating}
            className="btn-primary"
            style={{ width: '100%', justifyContent: 'center', padding: '12px', marginTop: '10px' }}
          >
            <Play size={16} /> {simulating ? 'Simulating Engine Response...' : 'Execute AI Scenario Simulation'}
          </button>
        </div>

        {/* Output Results Card */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '16px' }}>AI Engine Simulation Outcome</h3>

          {result ? (
            <div>
              <div style={{ background: 'rgba(0,0,0,0.3)', padding: '20px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)', marginBottom: '16px' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: '6px' }}>
                  Target: <strong>{result.productName}</strong> ({result.sku})
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '14px 0' }}>
                  <div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>Base/Prev Price</div>
                    <div style={{ fontSize: '1.2rem', fontWeight: 700 }}>${result.currentPrice.toFixed(2)}</div>
                  </div>

                  <ArrowRight size={20} color="var(--primary)" />

                  <div>
                    <div style={{ fontSize: '0.72rem', color: 'var(--accent-green)', fontWeight: 600 }}>Simulated AI Price</div>
                    <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                      ${result.recommendedPrice.toFixed(2)}
                    </div>
                  </div>

                  <div
                    style={{
                      padding: '6px 10px',
                      borderRadius: '10px',
                      fontSize: '0.85rem',
                      fontWeight: 800,
                      background: result.priceChangePercent >= 0 ? 'rgba(16,185,129,0.2)' : 'rgba(244,63,94,0.2)',
                      color: result.priceChangePercent >= 0 ? '#34d399' : '#fb7185',
                    }}
                  >
                    {result.priceChangePercent >= 0 ? '+' : ''}{result.priceChangePercent}%
                  </div>
                </div>

                <div style={{ background: 'rgba(255,255,255,0.03)', padding: '12px', borderRadius: '8px', borderLeft: '3px solid #34d399' }}>
                  <div style={{ fontSize: '0.72rem', color: '#34d399', fontWeight: 700, marginBottom: '4px' }}>
                    Calculated Reasoning & Margin Safeguards:
                  </div>
                  <p style={{ fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.4 }}>
                    {result.reasoning}
                  </p>
                </div>
              </div>

              <button
                onClick={handleApplySimulatedPrice}
                disabled={applying}
                className="btn-primary"
                style={{ width: '100%', justifyContent: 'center' }}
              >
                <ShieldCheck size={16} /> {applying ? 'Applying...' : 'Commit Simulated Price to Production Store'}
              </button>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
              Click "Execute AI Scenario Simulation" to run dynamic price recalculation.
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
