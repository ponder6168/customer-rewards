# Retailer Rewards - Spring Boot Example

This project demonstrates how to calculate customer reward points for orders over a three-month period using Spring Boot.

Summary
- Reward rules (per order):
    - 0 points for ≤ $50
    - 1 point per dollar for dollars between $50 and $100
    - 2 points per dollar for dollars over $100
    - Points are calculated per whole dollar (amount is floored to integer dollars)
- Java 17+, Spring Boot 3.x, Maven, H2 (for demo), Lombok, springdoc OpenAPI

Prerequisites
- JDK 17+ installed and configured
- Maven 3.6+
- (Recommended) Lombok plugin and annotation processing enabled in your IDE

Quick start - build & run
1. Build:
   mvn -U clean package

2. Run (dev):
   mvn spring-boot:run

   Or run the packaged jar:
   java -jar target/customer-rewards-0.0.1-SNAPSHOT.jar

Application defaults
- Server port: 8080
- H2 console: http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE)
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Seed data
- src/main/resources/sample_transactions.csv contains sample orders used by SeedDataLoader.
- SeedDataLoader inserts the sample orders into the H2 DB at startup if the DB is empty.

Postman collection
- A Postman collection with example requests is available in the project root: postman/Customer_Rewards_Collection.postman_collection

API endpoints 
- POST /orders
  - Creates a single order. Accepts a single OrderRequest JSON.
    - Example request body:
      - {
         "orderId": "O-test1",
         "customerId": "C-test1",
         "orderDate": "2024-07-02",
         "amount": 220.00
        }
  - Returns persisted OrderResponse.
    - Example response body:
      - {
          "id": 15,
          "orderId": "O-test1",
          "customerId": "C-test1",
          "orderDate": "2024-07-02",
          "amount": 220.00,
          "points": 290
        }
- POST /orders/batch
    - Creates multiple orders. Accepts  a JSON array of OrderRequest.
      - Example request body:
        - [
          {
          "orderId": "A01",
          "customerId": "customer1",
          "orderDate": "2026-01-10",
          "amount": 120.50
          },
          {
          "orderId": "B02",
          "customerId": "customer2",
          "orderDate": "2026-01-05",
          "amount": 75.00
          },
          {
          "orderId": "B03",
          "customerId": "customer2",
          "orderDate": "2026-01-20",
          "amount": 200.00
          },
          {
          "orderId": "C01",
          "customerId": "customer3",
          "orderDate": "2026-01-02",
          "amount": 45.00
          },
          {
          "orderId": "C02",
          "customerId": "customer3",
          "orderDate": "2026-01-15",
          "amount": 130.00
          },
          {
          "orderId": "C03",
          "customerId": "customer3",
          "orderDate": "2026-01-28",
          "amount": 220.75
          },
          {
          "orderId": "C04",
          "customerId": "customer3",
          "orderDate": "2026-02-18",
          "amount": 100
          },
          {
          "orderId": "C05",
          "customerId": "customer3",
          "orderDate": "2026-02-28",
          "amount": 200
          }
          ]
    - Returns list of persisted OrderResponses.
      - Example response body:
        - [
          {
          "id": 15,
          "orderId": "A01",
          "customerId": "customer1",
          "orderDate": "2026-01-10",
          "amount": 120.50,
          "points": 90
          },
          {
          "id": 16,
          "orderId": "B02",
          "customerId": "customer2",
          "orderDate": "2026-01-05",
          "amount": 75.00,
          "points": 25
          },
          {
          "id": 17,
          "orderId": "B03",
          "customerId": "customer2",
          "orderDate": "2026-01-20",
          "amount": 200.00,
          "points": 250
          },
          {
          "id": 18,
          "orderId": "C01",
          "customerId": "customer3",
          "orderDate": "2026-01-02",
          "amount": 45.00,
          "points": 0
          },
          {
          "id": 19,
          "orderId": "C02",
          "customerId": "customer3",
          "orderDate": "2026-01-15",
          "amount": 130.00,
          "points": 110
          },
          {
          "id": 20,
          "orderId": "C03",
          "customerId": "customer3",
          "orderDate": "2026-01-28",
          "amount": 220.75,
          "points": 290
          },
          {
          "id": 21,
          "orderId": "C04",
          "customerId": "customer3",
          "orderDate": "2026-02-18",
          "amount": 100,
          "points": 50
          },
          {
          "id": 22,
          "orderId": "C05",
          "customerId": "customer3",
          "orderDate": "2026-02-28",
          "amount": 200,
          "points": 250
          }
          ]
- GET /rewards
    - Optional query params: startDate, endDate (format: yyyy-MM-dd). Defaults to last 3 full months if no dates are provided.
      - If one date is provided it defaults to a 3 full month range based on the given date.
      - Example request:
          - GET /rewards?startDate=2025-09-01&endDate=2025-11-30
      - Returns the points per month and total points for each customer.
        - Example response body:
            [
                {
                "customerId": "C1",
                "monthlyPoints": {
                "2025-09": 115,
                "2025-10": 70,
                "2025-11": 0
                },
                "totalPoints": 185
                },
                {
                "customerId": "C2",
                "monthlyPoints": {
                "2025-09": 1,
                "2025-10": 250,
                "2025-11": 49
                },
                "totalPoints": 300
                },
                {
                "customerId": "C3",
                "monthlyPoints": {
                "2025-09": 90,
                "2025-10": 10,
                "2025-11": 102
                },
                "totalPoints": 202
                },
                {
                "customerId": "C4",
                "monthlyPoints": {
                "2025-10": 150
                },
                "totalPoints": 150
                }
            ]

- GET /rewards/{customerId}
    - Optional query params: startDate, endDate (format: yyyy-MM-dd). Defaults to last 3 full months if no dates are provided.
        - If one date is provided it defaults to a 3 full month range based on the given date.
        - Example request:
            - GET /rewards/customer3?startDate=2026-01-01&endDate=2026-02-28
        - Returns the points per month and total points for the given customer.
          - Example response body:
            - {
              "customerId": "customer3",
              "monthlyPoints": {
              "2026-01": 400,
              "2026-02": 300
              },
              "totalPoints": 700
              }

Testing
- Run unit/integration tests with:
  mvn test

