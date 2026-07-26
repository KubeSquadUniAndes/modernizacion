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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerIT {

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
  void listCategories_returns200WithFiveCategories() {
    ResponseEntity<List<Object>> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/categories",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().hasSize(5);
  }

  @Test
  void listCategories_responseContainsFishCategory() {
    ResponseEntity<List<java.util.Map>> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/categories",
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<>() {}
    );

    assertThat(response.getBody()).isNotNull();
    boolean hasFish = response.getBody().stream()
        .anyMatch(c -> "FISH".equals(c.get("categoryId")));
    assertThat(hasFish).isTrue();
  }

  @Test
  void healthEndpoint_returns200() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        "http://localhost:" + port + "/actuator/health",
        String.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void getCategory_existingId_returns200() {
    ResponseEntity<java.util.Map> response = restTemplate.getForEntity(
        "http://localhost:" + port + "/api/categories/FISH",
        java.util.Map.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("categoryId")).isEqualTo("FISH");
    assertThat(response.getBody().get("name")).isEqualTo("Fish");
  }

  @Test
  void getCategory_nonExistingId_returns404() {
    ResponseEntity<java.util.Map> response = restTemplate.getForEntity(
        "http://localhost:" + port + "/api/categories/UNKNOWN",
        java.util.Map.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("status")).isEqualTo(404);
    assertThat(response.getBody().get("message").toString()).contains("UNKNOWN");
  }

  @Test
  void getProductsByCategory_existingCategory_returnsProducts() {
    ResponseEntity<List<Object>> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/categories/FISH/products",
        HttpMethod.GET, null,
        new ParameterizedTypeReference<>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull().hasSize(4);
  }

  @Test
  void getProductsByCategory_nonExistingCategory_returns404() {
    ResponseEntity<java.util.Map> response = restTemplate.getForEntity(
        "http://localhost:" + port + "/api/categories/UNKNOWN/products",
        java.util.Map.class
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
