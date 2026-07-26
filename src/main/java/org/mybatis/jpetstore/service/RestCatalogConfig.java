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

import org.mybatis.jpetstore.mapper.CategoryMapper;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Activa con el perfil "rest". Registra RestCatalogService como bean "catalogService", sobreescribiendo el bean de
 * HSQLDB que registra CatalogService vía @Service. Stripes resuelve @SpringBean por nombre de campo ("catalogService"),
 * así que el nombre del bean es lo que determina cuál implementación se inyecta.
 */
@Configuration
@Profile("rest")
public class RestCatalogConfig {

  @Bean("catalogService")
  @Primary
  public CatalogService catalogService(CategoryMapper categoryMapper, ItemMapper itemMapper,
      ProductMapper productMapper, @Value("${CATALOG_SERVICE_URL:http://localhost:8081}") String baseUrl) {
    return new RestCatalogService(categoryMapper, itemMapper, productMapper, baseUrl);
  }
}
