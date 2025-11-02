# Brokerage Service

A backend service project for a brokerage firm. This application allows employees to manage stock orders for customers. Users can create, list, and cancel orders.

## Features

- **Create Order**: Create a new order (BUY/SELL) for a customer. Orders start with `PENDING` status.  
- **List Orders**: List orders for a specific customer and date range.  
- **Delete Order**: Cancel only `PENDING` orders.  
- **List Assets**: View the assets owned by a customer.  
- **Authorization**: All endpoints are protected with admin username and password.  

## Technologies Used

- Java 21, Spring Boot 3  
- Spring Data JPA, H2 Database (in-memory)  
- Spring Security (Basic Auth)  
- Maven
- Docker (optional for containerized deployment)  

## Running the Application

### Using Maven
```bash
mvn clean package
java -jar target/brokerage-service-0.0.1-SNAPSHOT.jar

### Using Docker
docker build -t brokerage-service .
docker run -p 8080:8080 brokerage-service

### ALL Endpoints

1- Create Order
curl -X POST http://localhost:8080/api/{customerId}/orders \
-H "Content-Type: application/json" \
-u admin:adminpwd \
-d '{
      "assetName": "BTC",
      "orderSide": "BUY",
      "size": 2.5,
      "price": 120000
    }'

2- List Orders
curl -X GET "http://localhost:8080/api/{customerId}/orders?startDate=2025-01-01&endDate=2025-12-31" \
-u admin:adminpwd

3- Delete Order
curl -X DELETE http://localhost:8080/api/{customerId}/orders/{orderId} \
-u admin:adminpwd

4- List Assets
curl -X GET http://localhost:8080/api/{customerId}/assets \
-u admin:adminpwd
