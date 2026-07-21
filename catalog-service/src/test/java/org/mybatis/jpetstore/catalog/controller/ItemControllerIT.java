package org.mybatis.jpetstore.catalog.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
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
class ItemControllerIT {

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
  void getItem_existingId_returns200() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/items/EST-1",
        HttpMethod.GET, null,
        new ParameterizedTypeReference<Map>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("itemId")).isEqualTo("EST-1");
    assertThat(response.getBody().get("quantity")).isEqualTo(10000);

    @SuppressWarnings("unchecked")
    Map<String, Object> product = (Map<String, Object>) response.getBody().get("product");
    assertThat(product).isNotNull();
    assertThat(product.get("productId")).isEqualTo("FI-SW-01");
    assertThat(product.get("name")).isEqualTo("Angelfish");
  }

  @Test
  void getItem_nonExistingId_returns404() {
    ResponseEntity<Map> response = restTemplate.exchange(
        "http://localhost:" + port + "/api/items/UNKNOWN",
        HttpMethod.GET, null,
        new ParameterizedTypeReference<Map>() {}
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().get("status")).isEqualTo(404);
    assertThat(response.getBody().get("message").toString()).contains("UNKNOWN");
  }
}
