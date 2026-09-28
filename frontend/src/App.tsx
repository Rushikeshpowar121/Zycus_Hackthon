import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { Dashboard } from './pages/Dashboard';
import { SimulatorPage } from './pages/SimulatorPage';
import { AuditPage } from './pages/AuditPage';
import { StrategyModal } from './components/StrategyModal';
import { StockModal } from './components/StockModal';
import { productService, pricingService, analyticsService } from './services/api';
import { Product, AnalyticsSummary, PriceAudit, InventoryLog } from './types';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('dashboard');

  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [recentAudits, setRecentAudits] = useState<PriceAudit[]>([]);
  const [inventoryLogs, setInventoryLogs] = useState<InventoryLog[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [isRepricing, setIsRepricing] = useState<boolean>(false);

  // Modal States
  const [selectedProductForPricing, setSelectedProductForPricing] = useState<Product | null>(null);
  const [selectedProductForStock, setSelectedProductForStock] = useState<Product | null>(null);

  const refreshAllData = async () => {
    try {
      const [prods, cats, sum, audits, logs] = await Promise.all([
        productService.getAll(),
        productService.getCategories(),
        analyticsService.getSummary(),
        analyticsService.getPriceAudits(),
        analyticsService.getInventoryLogs(),
      ]);
      setProducts(prods);
      setCategories(cats);
      setSummary(sum);
      setRecentAudits(audits);
      setInventoryLogs(logs);
    } catch (err) {
      console.error('Failed to load StockPulse data from backend API:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAllData();
    // Auto-refresh every 15 seconds to pick up async event changes
    const interval = setInterval(refreshAllData, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleBatchReprice = async () => {
    setIsRepricing(true);
    try {
      await pricingService.batchReprice();
      await refreshAllData();
    } catch (err) {
      console.error('Batch repricing failed:', err);
    } finally {
      setIsRepricing(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onBatchReprice={handleBatchReprice}
        isRepricing={isRepricing}
      />

      <main style={{ maxWidth: '1400px', margin: '0 auto', width: '100%', padding: '0 24px 40px 24px', flex: 1 }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-muted)' }}>
            <div style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '8px' }} className="gradient-text">
              StockPulse Engine Initializing...
            </div>
            <p style={{ fontSize: '0.85rem' }}>Connecting to Spring Boot REST Service & Spring Data JPA H2 Repository</p>
          </div>
        ) : (
          <>
            {(activeTab === 'dashboard' || activeTab === 'inventory') && (
              <Dashboard
                summary={summary}
                products={products}
                categories={categories}
                recentAudits={recentAudits}
                onOpenPricingModal={(product) => setSelectedProductForPricing(product)}
                onOpenStockModal={(product) => setSelectedProductForStock(product)}
              />
            )}

            {activeTab === 'simulator' && (
              <SimulatorPage products={products} onRefresh={refreshAllData} />
            )}

            {activeTab === 'audit' && (
              <AuditPage audits={recentAudits} logs={inventoryLogs} />
            )}
          </>
        )}
      </main>

      {/* Modals */}
      <StrategyModal
        product={selectedProductForPricing}
        onClose={() => setSelectedProductForPricing(null)}
        onApplied={refreshAllData}
      />

      <StockModal
        product={selectedProductForStock}
        onClose={() => setSelectedProductForStock(null)}
        onUpdated={refreshAllData}
      />

      {/* Footer */}
      <footer style={{ borderTop: '1px solid var(--border-color)', padding: '20px', textAlign: 'center', fontSize: '0.78rem', color: 'var(--text-dim)', background: 'rgba(0,0,0,0.3)' }}>
        StockPulse AI Inventory & Dynamic Pricing Engine • Powered by Spring Boot 3, Java 17 & React + Vite
      </footer>
    </div>
  );
};
