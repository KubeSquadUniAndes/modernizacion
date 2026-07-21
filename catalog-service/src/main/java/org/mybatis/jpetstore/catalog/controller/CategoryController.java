package org.mybatis.jpetstore.catalog.controller;

import java.util.List;
import org.mybatis.jpetstore.catalog.domain.Category;
import org.mybatis.jpetstore.catalog.domain.Product;
import org.mybatis.jpetstore.catalog.exception.ResourceNotFoundException;
import org.mybatis.jpetstore.catalog.service.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

  private final CatalogService catalogService;

  public CategoryController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping
  public ResponseEntity<List<Category>> listCategories() {
    log.info("GET /api/categories");
    return ResponseEntity.ok(catalogService.getCategoryList());
  }

  @GetMapping("/{categoryId}")
  public ResponseEntity<Category> getCategory(@PathVariable String categoryId) {
    log.info("GET /api/categories/{}", categoryId);
    return catalogService.getCategory(categoryId)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
  }

  @GetMapping("/{categoryId}/products")
  public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String categoryId) {
    log.info("GET /api/categories/{}/products", categoryId);
    catalogService.getCategory(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    return ResponseEntity.ok(catalogService.getProductListByCategory(categoryId));
  }
}
