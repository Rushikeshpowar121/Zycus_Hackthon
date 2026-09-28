import React, { useState, useEffect } from 'react';
import { Product, PricingCalculationResult, PricingStrategyType } from '../types';
import { pricingService } from '../services/api';
import { Sparkles, CheckCircle2, AlertCircle, X, ArrowRight, Zap } from 'lucide-react';

interface StrategyModalProps {
  product: Product | null;
  onClose: () => void;
  onApplied: () => void;
}

export const StrategyModal: React.FC<StrategyModalProps> = ({
  product,
  onClose,
  onApplied,
}) => {
  const [selectedStrategy, setSelectedStrategy] = useState<PricingStrategyType>('AI_HEURISTIC');
  const [recommendation, setRecommendation] = useState<PricingCalculationResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [applying, setApplying] = useState(false);

  useEffect(() => {
    if (product) {
      setSelectedStrategy(product.activeStrategyType || 'AI_HEURISTIC');
      fetchRecommendation(product.id, product.activeStrategyType || 'AI_HEURISTIC');
    }
  }, [product]);

  const fetchRecommendation = async (productId: number, strategy: PricingStrategyType) => {
    setLoading(true);
    try {
      const res = await pricingService.getRecommendation(productId, strategy);
      setRecommendation(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleStrategyChange = (strat: PricingStrategyType) => {
    setSelectedStrategy(strat);
    if (product) {
      fetchRecommendation(product.id, strat);
    }
  };

  const handleApplyPrice = async () => {
    if (!product || !recommendation) return;
    setApplying(true);
    try {
      await pricingService.applyRecommendation(product.id, recommendation);
      onApplied();
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setApplying(false);
    }
  };

  if (!product) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="glass-panel modal-content" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ padding: '8px', borderRadius: '10px', background: 'rgba(99,102,241,0.2)', color: '#818cf8' }}>
              <Zap size={20} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>AI Dynamic Pricing Engine</h3>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Product: {product.name} ({product.sku})</p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        {/* Strategy Selector Pills */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '8px', fontWeight: 600 }}>
            Select Active Pricing Strategy Algorithm:
          </label>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '8px' }}>
            {(['DEMAND_BASED', 'COMPETITOR_BASED', 'AGING_INVENTORY', 'AI_HEURISTIC'] as PricingStrategyType[]).map((strat) => (
              <button
                key={strat}
                onClick={() => handleStrategyChange(strat)}
                className={selectedStrategy === strat ? 'btn-primary' : 'btn-secondary'}
                style={{ fontSize: '0.78rem', justifyContent: 'center', padding: '8px 12px' }}
              >
                <Sparkles size={14} /> {strat.replace('_', ' ')}
              </button>
            ))}
          </div>
        </div>

        {/* Calculation Result Panel */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
            Calculating neural strategy recommendation...
          </div>
        ) : recommendation ? (
          <div style={{ background: 'rgba(0,0,0,0.3)', padding: '20px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Current Price</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-muted)' }}>
                  ${recommendation.currentPrice.toFixed(2)}
                </div>
              </div>

              <ArrowRight size={20} color="var(--text-dim)" />

              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-green)', fontWeight: 600 }}>Recommended AI Price</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-green)' }}>
                  ${recommendation.recommendedPrice.toFixed(2)}
                </div>
              </div>

              <div
                style={{
                  padding: '6px 12px',
                  borderRadius: '12px',
                  fontSize: '0.85rem',
                  fontWeight: 800,
                  background: recommendation.priceChangePercent >= 0 ? 'rgba(16,185,129,0.2)' : 'rgba(244,63,94,0.2)',
                  color: recommendation.priceChangePercent >= 0 ? '#34d399' : '#fb7185',
                  border: `1px solid ${recommendation.priceChangePercent >= 0 ? 'rgba(16,185,129,0.3)' : 'rgba(244,63,94,0.3)'}`,
                }}
              >
                {recommendation.priceChangePercent >= 0 ? '+' : ''}{recommendation.priceChangePercent.toFixed(2)}%
              </div>
            </div>

            {/* AI Reasoning Text */}
            <div style={{ background: 'rgba(255,255,255,0.03)', padding: '12px', borderRadius: '8px', borderLeft: '3px solid #6366f1' }}>
              <div style={{ fontSize: '0.72rem', color: '#818cf8', fontWeight: 700, marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <CheckCircle2 size={14} /> AI Decision Trace (Confidence: {(recommendation.confidenceScore * 100).toFixed(0)}%):
              </div>
              <p style={{ fontSize: '0.82rem', color: 'var(--text-main)', lineHeight: 1.4 }}>
                {recommendation.reasoning}
              </p>
            </div>
          </div>
        ) : null}

        {/* Footer Actions */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <button onClick={onClose} className="btn-secondary">Cancel</button>
          <button
            onClick={handleApplyPrice}
            disabled={applying || !recommendation}
            className="btn-primary"
          >
            {applying ? 'Updating Price...' : 'Apply AI Price to Live Inventory'}
          </button>
        </div>
      </div>
    </div>
  );
};
