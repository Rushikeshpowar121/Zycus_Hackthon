import React, { useState, useEffect } from 'react';
import { Navbar } from './components/Navbar';
import { Dashboard } from './pages/Dashboard';
import { SimulatorPage } from './pages/SimulatorPage';
import { AuditPage } from './pages/AuditPage';
import { MerchandisingPage } from './pages/MerchandisingPage';
import { StrategyModal } from './components/StrategyModal';
import { StockModal } from './components/StockModal';
import { productService, pricingService, analyticsService, advisorService } from './services/api';
import { Product, AnalyticsSummary, PriceAudit, InventoryLog, PricingSuggestion, ReorderSuggestion } from './types';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<string>('merchandising');

  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [summary, setSummary] = useState<AnalyticsSummary | null>(null);
  const [recentAudits, setRecentAudits] = useState<PriceAudit[]>([]);
  const [inventoryLogs, setInventoryLogs] = useState<InventoryLog[]>([]);
  const [pricingSuggestions, setPricingSuggestions] = useState<PricingSuggestion[]>([]);
  const [reorderSuggestions, setReorderSuggestions] = useState<ReorderSuggestion[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [isRepricing, setIsRepricing] = useState<boolean>(false);

  // Modal States
  const [selectedProductForPricing, setSelectedProductForPricing] = useState<Product | null>(null);
  const [selectedProductForStock, setSelectedProductForStock] = useState<Product | null>(null);

  const refreshAllData = async () => {
    try {
      const [prods, cats, sum, audits, logs, pSuggests, rSuggests] = await Promise.all([
        productService.getAll().catch(() => []),
        productService.getCategories().catch(() => []),
        analyticsService.getSummary().catch(() => null),
        analyticsService.getPriceAudits().catch(() => []),
        analyticsService.getInventoryLogs().catch(() => []),
        advisorService.getPricingSuggestions().catch(() => []),
        advisorService.getReorderSuggestions().catch(() => []),
      ]);
      setProducts(prods);
      setCategories(cats);
      setSummary(sum);
      setRecentAudits(audits);
      setInventoryLogs(logs);
      setPricingSuggestions(pSuggests);
      setReorderSuggestions(rSuggests);
    } catch (err) {
      console.error('Failed to load StockPulse data from backend API:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAllData();
    // Auto-refresh every 8 seconds to pick up async agentic loop suggestions
    const interval = setInterval(refreshAllData, 8000);
    return () => clearInterval(interval);
  }, []);

  const handleApprovePricing = async (id: number) => {
    try {
      await advisorService.approvePricingSuggestion(id);
      await refreshAllData();
    } catch (err) {
      console.error('Failed to approve pricing suggestion:', err);
    }
  };

  const handleRejectPricing = async (id: number) => {
    try {
      await advisorService.rejectPricingSuggestion(id);
      await refreshAllData();
    } catch (err) {
      console.error('Failed to reject pricing suggestion:', err);
    }
  };

  const handleApproveReorder = async (id: number) => {
    try {
      await advisorService.approveReorderSuggestion(id);
      await refreshAllData();
    } catch (err) {
      console.error('Failed to approve reorder suggestion:', err);
    }
  };

  const handleRejectReorder = async (id: number) => {
    try {
      await advisorService.rejectReorderSuggestion(id);
      await refreshAllData();
    } catch (err) {
      console.error('Failed to reject reorder suggestion:', err);
    }
  };

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

  const pendingCount = pricingSuggestions.filter(s => s.status === 'PENDING').length
    + reorderSuggestions.filter(s => s.status === 'PENDING').length;

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        onBatchReprice={handleBatchReprice}
        isRepricing={isRepricing}
        pendingCount={pendingCount}
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
            {activeTab === 'merchandising' && (
              <MerchandisingPage
                products={products}
                pricingSuggestions={pricingSuggestions}
                reorderSuggestions={reorderSuggestions}
                onRefresh={refreshAllData}
                onApprovePricing={handleApprovePricing}
                onRejectPricing={handleRejectPricing}
                onApproveReorder={handleApproveReorder}
                onRejectReorder={handleRejectReorder}
              />
            )}

            {(activeTab === 'dashboard' || activeTab === 'inventory') && (
              <Dashboard
                summary={summary}
                products={products}
                categories={categories}
                recentAudits={recentAudits}
                pricingSuggestions={pricingSuggestions}
                reorderSuggestions={reorderSuggestions}
                onOpenPricingModal={(product) => setSelectedProductForPricing(product)}
                onOpenStockModal={(product) => setSelectedProductForStock(product)}
                onApprovePricing={handleApprovePricing}
                onRejectPricing={handleRejectPricing}
                onApproveReorder={handleApproveReorder}
                onRejectReorder={handleRejectReorder}
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
        StockPulse AI Inventory & Dynamic Pricing Engine • Spring Boot 3 + Java 17 + React + Vite • Agentic Loop: Observe → Reason → Act → Checkpoint
      </footer>
    </div>
  );
};
