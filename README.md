This is our HCL Training hackathon Project.
# 🛒 Full Stack Retail Ordering System

A complete **Full-Stack E-Commerce / Retail Ordering System** built using **Spring Boot, MySQL, and React**.
This project demonstrates real-world backend architecture, REST API development, and frontend integration.

---

## 🚀 Project Overview

This application simulates a real-world retail platform where users can:

* Browse products
* Add items to cart
* Manage cart
* Place orders

It follows a **clean layered architecture** and industry best practices.

---

## 🧠 Tech Stack

### 🔹 Backend

* Java (Spring Boot)
* Spring Data JPA
* Hibernate
* REST APIs

### 🔹 Database

* MySQL

### 🔹 Frontend

* React.js


---

## 🏗️ Architecture

```
Frontend (React)
        ↓
Controller Layer (REST APIs)
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (JPA)
        ↓
MySQL Database
```

---

## ⚙️ Features

### 🛍️ Product Management

* View all products
* Fetch product details

### 🛒 Cart System

* Add items to cart
* View cart items
* Remove items from cart

### 🔗 API Integration

* Fully functional REST APIs
* JSON-based communication

### 🧩 Scalable Design

* Layered architecture
* Clean code separation
* Easily extendable (orders, payments, auth)

---

## 🔥 Key Highlights

✔ Built using **industry-standard architecture**
✔ No SQL queries — handled via JPA
✔ Fully functional backend with API testing
✔ Real-time frontend integration
✔ Hackathon-ready project

---

## 📡 API Endpoints

### Products

* `GET /products` → Get all products

### Cart

* `POST /cart` → Add item
* `GET /cart` → Get all items
* `DELETE /cart/{id}` → Remove item

---

## ▶️ How to Run

### Backend

1. Run Spring Boot application
2. Ensure MySQL is running
3. Configure `application.properties`

### Frontend

```
npm install
npm start
```

---

## 🎯 Future Enhancements


* Order Management System
* Payment Integration
* Admin Dashboard
* UI Improvements

---

## 💡 What We Learned

* Building REST APIs using Spring Boot
* Database integration with JPA
* Layered backend architecture
* Frontend-backend communication
* Debugging real-world issues

---

