import React from 'react';
import { Activity, Database, Cpu, BarChart3, Layers, History, Play, ShoppingBag } from 'lucide-react';

interface NavbarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  onBatchReprice: () => void;
  isRepricing: boolean;
  pendingCount?: number;
}

export const Navbar: React.FC<NavbarProps> = ({
  activeTab,
  setActiveTab,
  onBatchReprice,
  isRepricing,
  pendingCount = 0,
}) => {
  return (
    <header className="glass-panel" style={{ borderRadius: 0, borderTop: 0, borderLeft: 0, borderRight: 0, marginBottom: '24px' }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto', padding: '16px 24px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
        
        {/* Brand Logo & Live Indicator */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '12px', background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 0 20px rgba(99,102,241,0.5)' }}>
            <Cpu size={24} color="#ffffff" />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h1 style={{ fontSize: '1.4rem', fontWeight: 800 }} className="gradient-text">StockPulse</h1>
              <span className="badge badge-optimal" style={{ fontSize: '0.65rem' }}>
                <span className="pulse-dot"></span> AI Engine Live
              </span>
            </div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Autonomous Inventory & Dynamic Pricing System</p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'rgba(0,0,0,0.2)', padding: '4px', borderRadius: '14px', border: '1px solid var(--border-color)' }}>
          {/* Merchandising Console — PRIMARY DEMO TAB */}
          <button
            onClick={() => setActiveTab('merchandising')}
            className={activeTab === 'merchandising' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '8px 16px', fontSize: '0.85rem', position: 'relative' }}
          >
            <ShoppingBag size={16} /> Merchandising
            {pendingCount > 0 && (
              <span style={{
                position: 'absolute', top: '-6px', right: '-6px',
                background: '#f59e0b', color: '#000', borderRadius: '99px',
                fontSize: '0.62rem', fontWeight: 900, padding: '1px 6px',
                minWidth: '18px', textAlign: 'center', animation: 'pulse 1.5s infinite',
              }}>
                {pendingCount}
              </span>
            )}
          </button>

          <button
            onClick={() => setActiveTab('dashboard')}
            className={activeTab === 'dashboard' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '8px 16px', fontSize: '0.85rem' }}
          >
            <BarChart3 size={16} /> Dashboard
          </button>

          <button
            onClick={() => setActiveTab('inventory')}
            className={activeTab === 'inventory' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '8px 16px', fontSize: '0.85rem' }}
          >
            <Layers size={16} /> Inventory
          </button>

          <button
            onClick={() => setActiveTab('simulator')}
            className={activeTab === 'simulator' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '8px 16px', fontSize: '0.85rem' }}
          >
            <Play size={16} /> AI Simulator
          </button>

          <button
            onClick={() => setActiveTab('audit')}
            className={activeTab === 'audit' ? 'btn-primary' : 'btn-secondary'}
            style={{ padding: '8px 16px', fontSize: '0.85rem' }}
          >
            <History size={16} /> Audit Logs
          </button>
        </nav>

        {/* Action Controls */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <a
            href="http://localhost:8080/h2-console"
            target="_blank"
            rel="noreferrer"
            className="btn-secondary"
            style={{ fontSize: '0.8rem', textDecoration: 'none' }}
          >
            <Database size={16} color="#38bdf8" /> H2 Console
          </a>

          <button
            onClick={onBatchReprice}
            disabled={isRepricing}
            className="btn-primary"
            style={{ fontSize: '0.85rem' }}
          >
            <Activity size={16} className={isRepricing ? 'spin' : ''} />
            {isRepricing ? 'AI Repricing...' : 'Batch Reprice'}
          </button>
        </div>

      </div>
    </header>
  );
};
