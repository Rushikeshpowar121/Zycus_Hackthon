import React from 'react';
import { PriceAudit, InventoryLog } from '../types';
import { History, FileText, CheckCircle, RefreshCcw } from 'lucide-react';

interface AuditPageProps {
  audits: PriceAudit[];
  logs: InventoryLog[];
}

export const AuditPage: React.FC<AuditPageProps> = ({ audits, logs }) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ padding: '8px', borderRadius: '10px', background: 'rgba(236,72,153,0.2)', color: '#ec4899' }}>
            <History size={22} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Audit Logs & System Event Trail</h2>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Complete immutable ledger of all Spring `@Async` event dispatches, dynamic repricing adjustments, and inventory stock changes.
            </p>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(450px, 1fr))', gap: '20px' }}>
        
        {/* Dynamic Price Audit History */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <FileText size={18} color="#818cf8" />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Dynamic Price Change Audits</h3>
          </div>

          <div className="table-container" style={{ maxHeight: '500px', overflowY: 'auto' }}>
            <table className="stock-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>SKU</th>
                  <th>Old → New</th>
                  <th>Strategy</th>
                  <th>Reasoning</th>
                </tr>
              </thead>
              <tbody>
                {audits.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: 'var(--text-muted)' }}>
                      No price audit logs found.
                    </td>
                  </tr>
                ) : (
                  audits.map((a) => (
                    <tr key={a.id}>
                      <td style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>
                        {new Date(a.timestamp).toLocaleTimeString()}
                      </td>
                      <td style={{ fontWeight: 700 }}>{a.productSku}</td>
                      <td>
                        <span style={{ fontSize: '0.8rem' }}>
                          ${a.oldPrice?.toFixed(2)} → <strong style={{ color: 'var(--accent-green)' }}>${a.newPrice.toFixed(2)}</strong>
                        </span>
                      </td>
                      <td>
                        <span className="badge badge-optimal" style={{ fontSize: '0.65rem' }}>{a.strategyUsed}</span>
                      </td>
                      <td style={{ fontSize: '0.75rem', color: 'var(--text-muted)', maxWidth: '200px' }}>
                        {a.reason}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Async Inventory Event Logs */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <RefreshCcw size={18} color="#34d399" />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700 }}>Async Inventory Event Logs</h3>
          </div>

          <div className="table-container" style={{ maxHeight: '500px', overflowY: 'auto' }}>
            <table className="stock-table">
              <thead>
                <tr>
                  <th>Time</th>
                  <th>SKU</th>
                  <th>Action</th>
                  <th>Stock Change</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {logs.length === 0 ? (
                  <tr>
                    <td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: 'var(--text-muted)' }}>
                      No inventory logs recorded.
                    </td>
                  </tr>
                ) : (
                  logs.map((l) => (
                    <tr key={l.id}>
                      <td style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>
                        {new Date(l.timestamp).toLocaleTimeString()}
                      </td>
                      <td style={{ fontWeight: 700 }}>{l.sku}</td>
                      <td>
                        <span style={{ fontSize: '0.7rem', fontWeight: 700, color: '#38bdf8' }}>{l.action}</span>
                      </td>
                      <td>
                        <span style={{ fontWeight: 700, color: l.quantityChange >= 0 ? '#34d399' : '#fb7185' }}>
                          {l.quantityChange >= 0 ? '+' : ''}{l.quantityChange} units ({l.previousStock} → {l.newStock})
                        </span>
                      </td>
                      <td style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        {l.notes || '-'}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
};
