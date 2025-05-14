# 📚 Library Management System (LMS)

## Project Overview

The Library Management System (LMS) is composed of two main services:

1. **LMS-Core**:

   - Implements all core functionalities using a RESTful architecture.
   - Uses PostgreSQL for database interactions via Spring Data JPA and Hibernate.
   - Secured with JWT-based authentication and authorization.
   - Includes comprehensive unit and integration testing.
   - Key actions and errors are logged for traceability and observability.

2. **LMS-WebFlux**:
   - Built entirely with Spring WebFlux, adopting a fully reactive programming model.
   - Features reactive repositories and controllers.
   - Focuses solely on **real-time book availability**, implemented via a bulk return endpoint that updates book statuses in real-time.

## Project Features

- **RESTful Architecture**: Fully structured REST API with clearly separated layers (Controllers, Services, Repositories).
- **Database Integration**: PostgreSQL is used as the main database, with interaction handled by Spring Data JPA and Hibernate.
- **JWT Security**:
  - Three roles are supported: **Admin**, **Librarian**, and **Patron**.
  - Role-based access control is enforced across all endpoints (except authentication and documentation).
- **Testing**:
  - Repository layer is covered with unit tests.
  - Service layer has detailed unit tests.
  - Controller layer is validated via integration tests.
- **Logging**:
  - Logs are created for significant events such as user registration or marking borrowings as overdue.
  - All server errors (5xx) are captured and logged for diagnostics.

## Extra Features

- **Custom Response Wrapper**:

  - All API responses follow a standardized format using `LmsApiResponse`, which includes:
    - `status`: Indicates success (`true`) or failure (`false`)
    - `data`: Contains returned data if successful, or `null` on failure
    - `message`: Human-readable message explaining the result

- **Custom Messaging Templates**:

  - Reusable messages are defined in `messages.properties`, such as:
    - `fail.not.found=There is no {0} with this {1}`
    - Example output: "There is no user with this email"
  - This reduces redundancy and improves consistency across messages.

- **Custom Validation Annotations**:

  - Implemented annotations like `@PhoneNumber` and `@PositiveNumber`
  - Supports both reusable logic and dynamic message templates.

- **Global Exception Handling**:

  - All exceptions are managed centrally with a global handler.
  - Custom `LmsException` base class is used for domain-specific errors.

- **Scheduled Jobs**:
  - A scheduled method runs daily at 19:00 to mark books as **OVERDUE** if their return deadline has passed and they’re still marked as **BORROWED**.

## 🛠️ Technology Stack

This application is built using the following technologies:

- **Language**: Java 21 (Latest LTS)
- **Framework**: Spring Boot 3.4.5
- **Build Tool**: Maven
- **Database**:
  - **PostgreSQL** (Production)
  - **H2** (In-memory, for testing)
- **Containerization**: Docker
- **Security**: JWT-based authentication and authorization

### 📦 Dependencies

#### Spring Boot Starters

- `spring-boot-starter-data-jpa`: JPA and Hibernate integration for database persistence
- `spring-boot-starter-security`: Security configuration and filters
- `spring-boot-starter-web`: RESTful web services
- `spring-boot-starter-validation`: Bean validation (JSR 380)

#### Database Drivers

- `postgresql`: PostgreSQL database driver
- `h2`: Lightweight in-memory database for testing

#### Serialization

- `jackson-datatype-jsr310`: Support for Java 8 Date/Time API in Jackson

#### Authentication

- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`: JSON Web Token (JWT) implementation for secure stateless authentication

#### Code Generation and Mapping

- `mapstruct`: Compile-time bean mapping between DTOs and entities
- `lombok`: Reduces boilerplate code with annotations like `@Getter`, `@Setter`, etc.

#### Developer Tools

- `spring-boot-devtools`: Auto-restart and live reload for faster development

#### API Documentation

- `springdoc-openapi-starter-webmvc-ui`: Swagger/OpenAPI 3 integration
- `swagger-annotations`: OpenAPI annotations for documenting API models and endpoints

#### Testing

- `spring-boot-starter-test`: Includes JUnit, Mockito, Hamcrest, etc.
- `spring-security-test`: Testing support for Spring Security

## 🚀 How to Run the Application

Before running the application, ensure the following steps are completed:

### ⚙️ 1. Prepare `application-local.properties`

To avoid exposing sensitive information, this project uses `application-local.properties`, which is excluded from version control.

- Each module (`lms-core` and `lms-webflux`) includes an `application-local.properties.template` file containing all the necessary configuration keys and default values.
- You must rename these files by removing the `.template` extension using the following commands:

```bash
mv lms-core/src/main/resources/application-local.properties.template lms-core/src/main/resources/application-local.properties
mv lms-webflux/src/main/resources/application-local.properties.template lms-webflux/src/main/resources/application-local.properties
```

> **Note:**
>
> - If you change the database username or password, remember to update them in both the `.properties` files **and** `docker-compose.yml`.
> - The `jwt.secret` can be any random string and does **not** require synchronization with other files.

### 🔨 2. Build the Projects with Maven

If you're not running the project from an IDE, you need to build both modules using Maven:

```bash
mvn clean package
```

To skip tests during the build:

```bash
mvn clean package -DskipTests
```

> On Windows, you can use the Spring Boot wrapper:
>
> ```bash
> .\mvnw clean package
> ```

### 🐳 3. Run with Docker (Preferred)

Running the application via Docker is simple. From the **project root** directory, execute:

```bash
docker-compose up
```

Docker will:

- Build both modules
- Launch containers for `lms-core`, `lms-webflux`, and a PostgreSQL database

### 🖥️ 4. Run Without Docker

If Docker is not used, follow these steps manually:

1. **Start PostgreSQL**:

   - Ensure a PostgreSQL instance is running on `localhost:5432`
   - Create a database named `lmsdb`
   - Ensure credentials match those in `application-local.properties`

2. **Run the JAR files**:

```bash
# Core service
cd lms-core/target
java -jar lms-core-0.0.1-SNAPSHOT.jar

# WebFlux service
cd ../../lms-webflux/target
java -jar lms-webflux-0.0.1-SNAPSHOT.jar
```

### 💡 Extra: Run via IDE

You can also run both services using your IDE of choice, such as **IntelliJ IDEA** or **Visual Studio Code**, provided that:

- Maven is set up properly
- The PostgreSQL database is running
- The `application-local.properties` file is correctly configured

Each module contains a standard Spring Boot `main` class that can be executed directly.

# 📡 API Endpoints

All API endpoints are documented using **Swagger/OpenAPI** to provide clear and interactive documentation for developers and testers.

You can explore all available endpoints, including request parameters, responses, and example payloads by navigating to:

👉 [http://localhost:8080/api/swagger-ui/index.html](http://localhost:8080/api/swagger-ui/index.html)

This interface provides:

- 📥 **Request Input Schemas**  
  View required fields, types, and validation constraints for each operation.

- 📤 **Response Structures**  
  Understand successful and error responses in a unified format.

- 🛡 **Authentication Requirements**  
  See which roles (Admin, Librarian, Patron) are authorized for each endpoint.

- 🧪 **Try-it-out Functionality**  
  Execute endpoints directly from the browser using live requests.

> ✅ The Swagger UI is auto-generated via `springdoc-openapi` and updated automatically as the code evolves.

# 📊 Database Design

The Library Management System (LMS) uses a relational database model implemented with **JPA** (Hibernate). It consists of a base entity and three core domain entities: `User`, `Book`, and `Borrowing`. The design focuses on data integrity, traceability, and clean domain modeling.

## 🧱 Base Entity: `LmsEntity`

All entities inherit from `LmsEntity`, which provides shared fields for ID and timestamps:

- `id`: Primary key (auto-generated).
- `createdAt`: Timestamp when the entity was created.
- `updatedAt`: Timestamp when the entity was last updated.

These fields are automatically managed through `@PrePersist` and `@PreUpdate` hooks, ensuring consistency across all tables.

## 👤 User Entity

Represents individuals interacting with the system. Users can have one or more roles:

- `ROLE_PATRON`: Regular library user.
- `ROLE_LIBRARIAN`: Can manage books and handle borrowing.
- `ROLE_ADMIN`: Full system privileges.

### Key Fields:

- `email`, `password`, `name`, `surname`, `phoneNumber`: Core user data.
- `roles`: Stored as a string (e.g., comma-separated), determines access level.
- `isActive`: Boolean flag used for soft deletion.

## 📚 Book Entity

Represents a library book. The system allows detailed metadata and flexible availability tracking.

### Key Fields:

- `title`, `author`, `description`, `isbn`, `publicationDate`: Basic book metadata.
- `genre`: Enum field to classify books (e.g., FICTION, BIOGRAPHY, etc.).
- `isAvailable`: Tracks whether the book is available for borrowing.

The book entity supports genre-based search and filtering functionality.

## 📖 Borrowing Entity

Captures the lending history between users and books. Every record logs a full borrowing lifecycle.

### Relationships:

- `User (borrower)`: One-to-many relationship with borrowings.
- `Book`: Each borrowing is linked to one book.

### Key Fields:

- `borrowDate`, `dueDate`, `returnDate`: Track borrowing timeline.
- `status`: Enum indicating the state (e.g., BORROWED, OVERDUE, RETURNED_TIMELY, etc.).

A helper method is used to determine whether a borrowing is considered "returned."

## 🔁 Entity Relationships Overview

- **User ↔ Borrowing**: One-to-many — A user can have multiple borrowings.
- **Book ↔ Borrowing**: One-to-many — A book can be borrowed multiple times.
- **Book & User**: Indirectly linked through the `Borrowing` entity.

## 📌 Additional Design Highlights

- **Auditability**: All records include creation and update timestamps.
- **Enum Usage**: Roles, genres, and borrowing statuses are strongly typed via enums for clarity and control.
- **Validation & Constraints**: All critical fields are non-nullable, enforcing data integrity.

This schema ensures the LMS can support complex features like role-based authorization, overdue tracking, and genre-specific book queries with clean and maintainable relationships.
