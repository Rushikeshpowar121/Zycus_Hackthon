package com.stockpulse.service;

import com.stockpulse.dto.CreateProductRequest;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStockRequest;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.RiskLevel;
import com.stockpulse.event.AsyncEventPublisher;
import com.stockpulse.event.ProductUpdatedEvent;
import com.stockpulse.event.StockLevelChangeEvent;
import com.stockpulse.exception.InsufficientStockException;
import com.stockpulse.exception.ResourceNotFoundException;
import com.stockpulse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final AsyncEventPublisher asyncEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts(String category, InventoryStatus status, RiskLevel riskLevel) {
        List<Product> products;
        if (category != null && !category.isBlank()) {
            products = productRepository.findByCategory(category);
        } else if (status != null) {
            products = productRepository.findByStatus(status);
        } else if (riskLevel != null) {
            products = productRepository.findByRiskLevel(riskLevel);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        return mapToDTO(getProductEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return mapToDTO(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw(new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists"));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .category(request.getCategory())
                .currentPrice(request.getBasePrice())
                .basePrice(request.getBasePrice())
                .costPrice(request.getCostPrice())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .stockQuantity(request.getStockQuantity())
                .reorderPoint(request.getReorderPoint())
                .maxStockLimit(request.getMaxStockLimit())
                .daysInStock(request.getDaysInStock() != null ? request.getDaysInStock() : 0)
                .daysToExpiry(request.getDaysToExpiry())
                .competitorPriceIndex(request.getCompetitorPriceIndex())
                .salesVelocity(request.getSalesVelocity() != null ? request.getSalesVelocity() : 0.0)
                .viewsCount(request.getViewsCount() != null ? request.getViewsCount() : 0)
                .activeStrategyType(request.getActiveStrategyType())
                .build();

        Product saved = productRepository.save(product);

        // Publish stock initialized event asynchronously
        asyncEventPublisher.publishStockChange(StockLevelChangeEvent.builder()
                .productId(saved.getId())
                .sku(saved.getSku())
                .previousStock(0)
                .newStock(saved.getStockQuantity())
                .action("PRODUCT_CREATED")
                .notes("Initial stock established on creation")
                .build());

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO updateStock(Long id, UpdateStockRequest request) {
        Product product = getProductEntityById(id);
        int previousStock = product.getStockQuantity();
        int change = request.getQuantityChange();

        if (change < 0 && previousStock + change < 0) {
            throw new InsufficientStockException(String.format("Cannot process reduction of %d units. Available stock is %d.", 
                    Math.abs(change), previousStock));
        }

        int newStock = previousStock + change;
        product.setStockQuantity(newStock);
        if ("STOCK_SALE".equalsIgnoreCase(request.getAction())) {
            double currentVel = product.getSalesVelocity() != null ? product.getSalesVelocity() : 0.0;
            product.setSalesVelocity(currentVel + (Math.abs(change) * 0.1));
            int currentViews = product.getViewsCount() != null ? product.getViewsCount() : 0;
            product.setViewsCount(currentViews + (Math.abs(change) * 3));
        }

        product.updateComputedAttributes();
        Product updated = productRepository.save(product);

        // Publish Async Event for stock change
        asyncEventPublisher.publishStockChange(StockLevelChangeEvent.builder()
                .productId(updated.getId())
                .sku(updated.getSku())
                .previousStock(previousStock)
                .newStock(newStock)
                .action(request.getAction())
                .notes(request.getNotes() != null ? request.getNotes() : "Stock updated via API")
                .build());

        // Publish ProductUpdatedEvent for agentic loop
        asyncEventPublisher.publishProductUpdated(ProductUpdatedEvent.builder()
                .productId(updated.getId())
                .sku(updated.getSku())
                .previousStock(previousStock)
                .newStock(newStock)
                .eventType("STOCK_SALE".equalsIgnoreCase(request.getAction()) ? "ORDER_CREATED" : "STOCK_CHANGE")
                .timestamp(java.time.LocalDateTime.now())
                .build());

        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductEntityById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    @Override
    public ProductDTO mapToDTO(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .category(p.getCategory())
                .currentPrice(p.getCurrentPrice())
                .basePrice(p.getBasePrice())
                .costPrice(p.getCostPrice())
                .minPrice(p.getMinPrice())
                .maxPrice(p.getMaxPrice())
                .stockQuantity(p.getStockQuantity())
                .reorderPoint(p.getReorderPoint())
                .maxStockLimit(p.getMaxStockLimit())
                .daysInStock(p.getDaysInStock())
                .daysToExpiry(p.getDaysToExpiry())
                .competitorPriceIndex(p.getCompetitorPriceIndex())
                .salesVelocity(p.getSalesVelocity())
                .viewsCount(p.getViewsCount())
                .activeStrategyType(p.getActiveStrategyType())
                .status(p.getStatus())
                .demandLevel(p.getDemandLevel())
                .riskLevel(p.getRiskLevel())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
