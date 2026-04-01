# 🔐 JWT Authentication POC with Spring Boot & Kotlin

🔗 Repository: https://github.com/josephmora1810/PocJwtAuthentication

## 📌 Overview

This project is a **Proof of Concept (POC)** for implementing **JWT (JSON Web Token) authentication** using **Spring Boot 4.0.5** and **Kotlin**. It demonstrates a stateless authentication mechanism with role‑based access control, secured endpoints, and integration with a MySQL database via JPA/Hibernate.

The application provides:
- User registration (password encrypted with BCrypt)
- Login endpoint that returns a JWT
- Secure endpoints accessible only with a valid token
- Automatic table creation using Hibernate’s `ddl-auto=create-drop` (development mode)

---

## 🛠️ Technologies Used

| Technology               | Version         | Description                              |
|--------------------------|-----------------|------------------------------------------|
| **Spring Boot**          | 4.0.5           | Core framework                           |
| **Kotlin**               | 2.2.21          | Programming language                     |
| **Spring Security**      | 7.0.4           | Authentication & authorization           |
| **JJWT**                 | 0.12.6          | JWT handling                             |
| **Spring Data JPA**      | –               | ORM with Hibernate                       |
| **MySQL**                | 8.0 / MariaDB   | Database                                 |
| **Lombok** (optional)    | 1.18.30         | Boilerplate reduction (Java compatibility)|
| **Gradle**               | –               | Build tool                               |

---

## 📋 Prerequisites

Before running this project, ensure you have:

- **JDK 21** or later
- **MySQL** or **MariaDB** installed and running
- **Gradle** (or use the wrapper `./gradlew`)
- **Postman** or any HTTP client to test the endpoints

---

## 📂 Project Structure

# Spring Boot JWT Authentication POC

```text
src/main/kotlin/com/example/pocjwtauth/
├── PocJwtAuthApplication.kt          # Punto de entrada de la aplicación
├── config/                            # Configuración de Seguridad
│   ├── SecurityConfig.kt              # SecurityFilterChain y AuthenticationProvider
│   ├── UnAuthorizedUserAuthenticationEntryPoint.kt  # Manejador de errores 401
│   └── AppConfig.kt                   # Definición de Beans (BCryptPasswordEncoder)
├── controller/                        # Endpoints REST
│   └── UserRestController.kt
├── entity/                            # Entidades de JPA y DTOs
│   ├── User.kt                        # Entidad de Usuario
│   ├── UserRequest.kt                 # DTO para solicitudes de Login
│   └── UserResponse.kt                # DTO para respuesta con Token
├── filter/                            # Filtros de Seguridad
│   └── SecurityFilter.kt              # Filtro que valida el JWT en cada petición
├── repository/                        # Repositorios de Spring Data JPA
│   └── UserRepository.kt
├── service/                           # Lógica de Negocio
│   ├── UserServiceImpl.kt             # Implementación de UserDetailsService
│   └── iservice/IUserService.kt       # Interfaz del servicio de usuario
└── utils/                             # Utilidades y Constantes
    ├── Constants.kt                   # Constantes de URLs y configuración API
    └── JWTUtils.kt                    # Lógica de generación y validación de tokens
```


---

## ⚙️ Configuration

### Database (`application.properties`)

```properties
# Database
spring.datasource.url=
spring.datasource.driver-class-name=
spring.datasource.username=
spring.datasource.password=

# JPA
spring.jpa.show-sql=
spring.jpa.properties.hibernate.format_sql=
spring.jpa.hibernate.ddl-auto=
spring.jpa.open-in-view=

# JWT
jwt.secret.key=jwt.secret.key=${JWT_SECRET}

# Logging
logging.level.org.springframework.security=
logging.level.org.hibernate.SQL=
```

## 🚀 Guía de Ejecución (How to Run)

### 1. Clone the Repository
First, clone the repository or copy the files in your local machine:
```bash
git clone https://github.com/josephmora1810/PocJwtAuthentication.git
cd PocJwtAuthentication
```
### 2. Create the Database
```bash
CREATE DATABASE IF NOT EXISTS PocJwtAuth;
```

### 3. Build the Project & run the Application
```bash
./gradlew clean build
./gradlew bootRun 
```
---

## 📡 API Endpoints

All endpoints are prefixed with `/api/v1/users` (defined in `Constants.kt`).

---

### 1. Register a new user

**Endpoint:**  
`POST /api/v1/users/saveUsers`

**Request body (JSON):**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "secret123",
  "roles": ["ROLE_USER"]
}
```

**Response (200 OK):**
```
User with id 1 saved successfully!
```

---

### 2. Login and obtain JWT

**Endpoint:**  
`POST /api/v1/users/loginUser`

**Request body (JSON):**
```json
{
  "username": "john_doe",
  "password": "secret123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJqdGkiOiI...",
  "message": "Token generated!"
}
```

---

### 3. Access a protected endpoint

**Endpoint:**  
`POST /api/v1/users/getData`

**Headers:**
```
Authorization: Bearer <your-token>
```

**Response (200 OK):**
```
You´re successfully authenticated!, You´re: john_doe
```

If the token is missing or invalid, you’ll receive a **401 Unauthorized** response.

---

## 🔍 Code Highlights

### JWTUtils – Token handling

```kotlin
@Component
class JWTUtils(@Value("\${jwt.secret.key}") private val secretKey: String) {

    fun generateToken(subject: String): String {
        return Jwts.builder()
            .subject(subject)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)))
            .signWith(Keys.hmacShaKeyFor(secretKey.toByteArray()))
            .compact()
    }

    fun getSubject(token: String): String? = 
        try { getClaims(token).subject } catch (e: Exception) { null }
    // ... other methods
}
```

---

### SecurityFilter – JWT validation filter

```kotlin
@Component
class SecurityFilter(
    private val jwtUtils: JWTUtils,
    private val userDetailsService: UserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            val username = jwtUtils.getSubject(token)

            if (username != null && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(username)

                if (jwtUtils.isValidJwtToken(token, userDetails.username)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                }
            }
        }

        chain.doFilter(request, response)
    }
}
```

---

### SecurityConfig – Spring Security configuration

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserDetailsService,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val authenticationEntryPoint: UnAuthorizedUserAuthenticationEntryPoint,
    private val securityFilter: SecurityFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/api/v1/users/saveUsers",
                        "/api/v1/users/loginUser"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider =
        DaoAuthenticationProvider(userDetailsService).apply {
            setPasswordEncoder(bCryptPasswordEncoder)
        }
}
```

---

## 🧪 Testing with Postman

1. Register a new user (POST)  
   ![Postman register screenshot]

2. Login and copy the token from the response.

3. Access protected endpoint – add the token in the `Authorization` header with **Bearer** prefix.

---

## ⚠️ Troubleshooting

| Issue                          | Solution                                                                 |
|--------------------------------|-------------------------------------------------------------------------|
| Cannot load driver class       | Ensure the MySQL connector is in `build.gradle.kts`: <br> `runtimeOnly("com.mysql:mysql-connector-j")` |
| WeakKeyException               | Use a secret key with at least 32 characters (HS256) or 64 (HS512)       |
| 401 Unauthorized even with token | Check Bearer prefix removal and matching secret key                     |
| Table not created              | Set `spring.jpa.hibernate.ddl-auto=update` or `create-drop` and verify DB permissions |

---

## 🚧 Future Improvements

- Add refresh token mechanism
- Implement role-based authorization with `@PreAuthorize`
- Use environment variables for secrets
- Add integration tests
- Dockerize the application
- Switch to a production-ready database configuration

---

## 📄 License

This project is for educational purposes. Feel free to use and modify it.

---

## 👨‍💻 Author
(Joseph Mora/josephmora1810)

Developed as a Proof of Concept for JWT authentication with Spring Boot & Kotlin.

---

Enjoy coding! 🚀