package org.mybatis.jpetstore.catalog.controller;

import java.util.List;
import org.mybatis.jpetstore.catalog.domain.Item;
import org.mybatis.jpetstore.catalog.domain.Product;
import org.mybatis.jpetstore.catalog.exception.ResourceNotFoundException;
import org.mybatis.jpetstore.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private static final Logger log = LoggerFactory.getLogger(ProductController.class);

  private final CatalogService catalogService;

  public ProductController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping("/search")
  public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
    log.info("GET /api/products/search?keyword={}", keyword);
    if (keyword == null || keyword.isBlank()) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(catalogService.searchProductList(keyword));
  }

  @GetMapping("/{productId}")
  public ResponseEntity<Product> getProduct(@PathVariable String productId) {
    log.info("GET /api/products/{}", productId);
    return catalogService.getProduct(productId)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
  }

  @GetMapping("/{productId}/items")
  public ResponseEntity<List<Item>> getItemsByProduct(@PathVariable String productId) {
    log.info("GET /api/products/{}/items", productId);
    catalogService.getProduct(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    return ResponseEntity.ok(catalogService.getItemListByProduct(productId));
  }
}
