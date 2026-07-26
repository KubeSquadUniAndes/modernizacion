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
package org.mybatis.jpetstore.catalog.controller;

import org.mybatis.jpetstore.catalog.domain.Item;
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
@RequestMapping("/api/items")
public class ItemController {

  private static final Logger log = LoggerFactory.getLogger(ItemController.class);

  private final CatalogService catalogService;

  public ItemController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping("/{itemId}")
  public ResponseEntity<Item> getItem(@PathVariable String itemId) {
    log.info("GET /api/items/{}", itemId);
    return catalogService.getItem(itemId)
        .map(ResponseEntity::ok)
        .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemId));
  }
}
