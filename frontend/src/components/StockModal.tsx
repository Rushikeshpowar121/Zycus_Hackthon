import React, { useState } from 'react';
import { Product } from '../types';
import { productService } from '../services/api';
import { Package, X, ArrowUpCircle, ArrowDownCircle, RefreshCw } from 'lucide-react';

interface StockModalProps {
  product: Product | null;
  onClose: () => void;
  onUpdated: () => void;
}

export const StockModal: React.FC<StockModalProps> = ({
  product,
  onClose,
  onUpdated,
}) => {
  const [action, setAction] = useState<string>('STOCK_RESTOCK');
  const [quantity, setQuantity] = useState<number>(10);
  const [notes, setNotes] = useState<string>('');
  const [updating, setUpdating] = useState<boolean>(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!product) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setUpdating(true);
    setErrorMsg(null);

    const change = action === 'STOCK_SALE' ? -Math.abs(quantity) : Math.abs(quantity);

    try {
      await productService.updateStock(product.id, change, action, notes || `${action} processed`);
      onUpdated();
      onClose();
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Failed to update stock quantity');
    } finally {
      setUpdating(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="glass-panel modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '480px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ padding: '8px', borderRadius: '10px', background: 'rgba(56,189,248,0.2)', color: '#38bdf8' }}>
              <Package size={20} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Update Inventory Stock</h3>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{product.name} (Current: {product.stockQuantity} units)</p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>
            <X size={20} />
          </button>
        </div>

        {errorMsg && (
          <div style={{ background: 'rgba(244,63,94,0.15)', border: '1px solid rgba(244,63,94,0.3)', color: '#fb7185', padding: '10px', borderRadius: '8px', fontSize: '0.8rem', marginBottom: '16px' }}>
            {errorMsg}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Action Type */}
          <div style={{ marginBottom: '16px' }}>
            <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: 600 }}>Action Type:</label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
              <button
                type="button"
                onClick={() => setAction('STOCK_RESTOCK')}
                className={action === 'STOCK_RESTOCK' ? 'btn-primary' : 'btn-secondary'}
                style={{ fontSize: '0.75rem', padding: '8px', justifyContent: 'center' }}
              >
                <ArrowUpCircle size={14} /> Restock
              </button>
              <button
                type="button"
                onClick={() => setAction('STOCK_SALE')}
                className={action === 'STOCK_SALE' ? 'btn-primary' : 'btn-secondary'}
                style={{ fontSize: '0.75rem', padding: '8px', justifyContent: 'center' }}
              >
                <ArrowDownCircle size={14} /> Sale
              </button>
              <button
                type="button"
                onClick={() => setAction('STOCK_ADJUSTMENT')}
                className={action === 'STOCK_ADJUSTMENT' ? 'btn-primary' : 'btn-secondary'}
                style={{ fontSize: '0.75rem', padding: '8px', justifyContent: 'center' }}
              >
                <RefreshCw size={14} /> Adjust
              </button>
            </div>
          </div>

          {/* Quantity */}
          <div style={{ marginBottom: '16px' }}>
            <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: 600 }}>Quantity Units:</label>
            <input
              type="number"
              min={1}
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
              style={{
                width: '100%',
                padding: '10px',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                color: '#fff',
                fontSize: '0.9rem',
              }}
              required
            />
          </div>

          {/* Notes */}
          <div style={{ marginBottom: '20px' }}>
            <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'block', marginBottom: '6px', fontWeight: 600 }}>Audit Notes (Optional):</label>
            <input
              type="text"
              placeholder="e.g. Shipment received from Supplier X"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              style={{
                width: '100%',
                padding: '10px',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-md)',
                color: '#fff',
                fontSize: '0.85rem',
              }}
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={updating} className="btn-primary">
              {updating ? 'Processing Async Event...' : 'Confirm Stock Event'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
