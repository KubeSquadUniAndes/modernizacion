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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductControllerIT {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("catalogdb")
      .withUsername("catalog")
      .withPassword("catalog");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort
  int port;

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void searchProducts_singleKeyword_returnsMatches() {
    ResponseEntity<List> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/search?keyword=fish",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<List>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().isNotEmpty();
  }

  @Test
  void searchProducts_multipleKeywords_returnsCombinedResults() {
    ResponseEntity<List> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/search?keyword=fish+shark",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<List>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().isNotEmpty();
  }

  @Test
  void searchProducts_noMatch_returnsEmptyList() {
    ResponseEntity<List> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/search?keyword=xyznotexists",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<List>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().isEmpty();
  }

  @Test
  void getProduct_existingId_returns200() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/FI-SW-01",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<Map>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("productId")).isEqualTo("FI-SW-01");
    assertThat(response.getBody().get("name")).isEqualTo("Angelfish");
    assertThat(response.getBody().get("categoryId")).isEqualTo("FISH");
  }

  @Test
  void getProduct_nonExistingId_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/UNKNOWN",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<Map>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().get("status")).isEqualTo(404);
    assertThat(response.getBody().get("message").toString()).contains("UNKNOWN");
  }

  @Test
  void getItemsByProduct_existingProduct_returnsItems() {
    ResponseEntity<List> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/FI-SW-01/items",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<List>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().hasSize(2);
  }

  @Test
  void getItemsByProduct_nonExistingProduct_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/products/UNKNOWN/items",
        org.springframework.http.HttpMethod.GET, null,
        new ParameterizedTypeReference<Map>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
