package com.stockpulse.service;

import com.stockpulse.dto.CreateProductRequest;
import com.stockpulse.dto.ProductDTO;
import com.stockpulse.dto.UpdateStockRequest;
import com.stockpulse.entity.Product;
import com.stockpulse.enums.InventoryStatus;
import com.stockpulse.enums.RiskLevel;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getAllProducts(String category, InventoryStatus status, RiskLevel riskLevel);
    ProductDTO getProductById(Long id);
    ProductDTO getProductBySku(String sku);
    Product getProductEntityById(Long id);
    ProductDTO createProduct(CreateProductRequest request);
    ProductDTO updateStock(Long id, UpdateStockRequest request);
    void deleteProduct(Long id);
    List<String> getAllCategories();
    ProductDTO mapToDTO(Product product);
}
