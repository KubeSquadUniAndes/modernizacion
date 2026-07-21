package org.mybatis.jpetstore.catalog.service;

import java.util.List;
import java.util.Optional;
import org.mybatis.jpetstore.catalog.domain.Category;
import org.mybatis.jpetstore.catalog.domain.Item;
import org.mybatis.jpetstore.catalog.domain.Product;
import org.mybatis.jpetstore.catalog.mapper.CategoryMapper;
import org.mybatis.jpetstore.catalog.mapper.ItemMapper;
import org.mybatis.jpetstore.catalog.mapper.ProductMapper;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

  private final CategoryMapper categoryMapper;
  private final ProductMapper productMapper;
  private final ItemMapper itemMapper;

  public CatalogService(CategoryMapper categoryMapper, ProductMapper productMapper, ItemMapper itemMapper) {
    this.categoryMapper = categoryMapper;
    this.productMapper = productMapper;
    this.itemMapper = itemMapper;
  }

  public List<Category> getCategoryList() {
    return categoryMapper.getCategoryList();
  }

  public Optional<Category> getCategory(String categoryId) {
    return Optional.ofNullable(categoryMapper.getCategory(categoryId));
  }

  public List<Product> getProductListByCategory(String categoryId) {
    return productMapper.getProductListByCategory(categoryId);
  }

  public Optional<Product> getProduct(String productId) {
    return Optional.ofNullable(productMapper.getProduct(productId));
  }

  public List<Product> searchProductList(String keywords) {
    return keywords.trim().split("\\s+").length == 1
        ? productMapper.searchProductList("%" + keywords.trim().toLowerCase() + "%")
        : java.util.Arrays.stream(keywords.trim().split("\\s+"))
            .flatMap(kw -> productMapper.searchProductList("%" + kw.toLowerCase() + "%").stream())
            .distinct()
            .collect(java.util.stream.Collectors.toList());
  }

  public List<Item> getItemListByProduct(String productId) {
    return itemMapper.getItemListByProduct(productId);
  }

  public Optional<Item> getItem(String itemId) {
    return Optional.ofNullable(itemMapper.getItem(itemId));
  }
}
