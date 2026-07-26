/*
 *    Copyright 2010-2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mybatis.jpetstore.domain.Category;
import org.mybatis.jpetstore.domain.Item;
import org.mybatis.jpetstore.domain.Product;
import org.mybatis.jpetstore.mapper.CategoryMapper;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Implementación del servicio de catálogo que delega al catalog-service vía REST. No lleva @Service — se registra como
 * bean "catalogService" desde RestCatalogConfig cuando el perfil "rest" está activo, sobreescribiendo el bean de
 * HSQLDB. Patrón Strangler Fig: el monolito sigue exponiendo la misma UI y los mismos ActionBeans, pero las consultas
 * de catálogo las resuelve el microservicio.
 */
public class RestCatalogService extends CatalogService {

  private static final Logger log = LoggerFactory.getLogger(RestCatalogService.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public RestCatalogService(CategoryMapper categoryMapper, ItemMapper itemMapper, ProductMapper productMapper,
      String baseUrl) {
    super(categoryMapper, itemMapper, productMapper);
    this.baseUrl = baseUrl;
    this.restTemplate = new RestTemplate();
    log.info("RestCatalogService activo — delegando catálogo a {}", baseUrl);
  }

  @Override
  public List<Category> getCategoryList() {
    Category[] result = restTemplate.getForObject(baseUrl + "/api/categories", Category[].class);
    return result != null ? Arrays.asList(result) : Collections.emptyList();
  }

  @Override
  public Category getCategory(String categoryId) {
    try {
      return restTemplate.getForObject(baseUrl + "/api/categories/" + categoryId, Category.class);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }

  @Override
  public Product getProduct(String productId) {
    try {
      return restTemplate.getForObject(baseUrl + "/api/products/" + productId, Product.class);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }

  @Override
  public List<Product> getProductListByCategory(String categoryId) {
    Product[] result = restTemplate.getForObject(baseUrl + "/api/categories/" + categoryId + "/products",
        Product[].class);
    return result != null ? Arrays.asList(result) : Collections.emptyList();
  }

  @Override
  public List<Product> searchProductList(String keywords) {
    String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/products/search").queryParam("keyword", keywords)
        .toUriString();
    Product[] result = restTemplate.getForObject(url, Product[].class);
    return result != null ? Arrays.asList(result) : Collections.emptyList();
  }

  @Override
  public List<Item> getItemListByProduct(String productId) {
    Item[] result = restTemplate.getForObject(baseUrl + "/api/products/" + productId + "/items", Item[].class);
    return result != null ? Arrays.asList(result) : Collections.emptyList();
  }

  @Override
  public Item getItem(String itemId) {
    try {
      return restTemplate.getForObject(baseUrl + "/api/items/" + itemId, Item.class);
    } catch (HttpClientErrorException.NotFound e) {
      return null;
    }
  }

  @Override
  public boolean isItemInStock(String itemId) {
    Item item = getItem(itemId);
    return item != null && item.getQuantity() > 0;
  }
}
