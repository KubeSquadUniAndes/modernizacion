from locust import HttpUser, between, task


class CatalogServiceUser(HttpUser):
    wait_time = between(0.2, 1.0)

    @task(1)
    def health(self):
        self.client.get("/actuator/health", name="GET /actuator/health")

    @task(4)
    def list_categories(self):
        self.client.get("/api/categories", name="GET /api/categories")

    @task(3)
    def get_category(self):
        self.client.get("/api/categories/FISH", name="GET /api/categories/{categoryId}")

    @task(3)
    def products_by_category(self):
        self.client.get("/api/categories/FISH/products", name="GET /api/categories/{categoryId}/products")

    @task(3)
    def get_product(self):
        self.client.get("/api/products/FI-SW-01", name="GET /api/products/{productId}")

    @task(3)
    def items_by_product(self):
        self.client.get("/api/products/FI-SW-01/items", name="GET /api/products/{productId}/items")

    @task(3)
    def get_item(self):
        self.client.get("/api/items/EST-1", name="GET /api/items/{itemId}")

    @task(3)
    def search_products(self):
        self.client.get("/api/products/search?keyword=fish", name="GET /api/products/search")

    @task(1)
    def missing_category(self):
        with self.client.get(
            "/api/categories/UNKNOWN",
            name="GET /api/categories/{categoryId} 404 esperado",
            catch_response=True,
        ) as response:
            if response.status_code == 404:
                response.success()
            else:
                response.failure(f"Expected 404, got {response.status_code}")
