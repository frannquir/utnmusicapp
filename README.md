# 🎵 UTN Music App (Echoed API)

A comprehensive music social platform developed by UTN students. This application seamlessly integrates with Spotify to allow users to discover music, write reviews, and interact with a community of music lovers, all powered by a robust and secure Spring Boot backend.

## 🚀 Features

### 🎧 Core Music Features
- **Music Discovery**: Browse and search for artists, albums, and songs using local data and external APIs.
- **Spotify Integration**:Seamless integration with Spotify Web API to fetch rich metadata, cover arts, and tracklists in real-time.
- **Advanced Search**: Unified search functionality across local DB and Spotify catalog.

### 🤝 Social & Interaction
- **Review System**: Users can rate (1-5 stars) and review albums and songs.
- **Comment Threads**: Nested discussions on specific reviews.
- **Reactions**: "Like" system for reviews and comments.
- **User Profiles**: Customized profiles with avatars, bio, and review history.

### 🛡️ Admin & Moderation (Backoffice)

- **User Management**: Admins can view comprehensive user lists and **Ban/Unban** users violating community guidelines.
- **Content Moderation**: Ability to delete inappropriate reviews or comments.
- **Statistics Dashboard**: Exclusive access to platform analytics (most active users, top-rated albums).

### ⚙️ Advanced Technical Features
- **Hybrid Authentication**: Secure login via Google OAuth2 or traditional Email/Password.
- **Stateless Security**: Full **JWT (JSON Web Token)** implementation for API protection.
- **Fail-Safe Notificationsy**: Resilient email service architecture designed for cloud constraints.

## 🛠️ Tech Stack

### Backend Core
- **Java 21** - Latest LTS version for optimal performance and modern syntax.
- **Spring Boot 3.4** - Modern framework for rapid application development.
- **Spring Data JPA** - Hibernate-based ORM for database abstraction.
- **PostgreSQL** - Relational database management.

### Security Architecture
- **Spring Security 6** - Comprehensive security framework.
- **OAuth2 Client** - Google Identity integration.
- **JWT** - Custom filter chain for token validation and generation.
- **RBAC** - Role-Based Access Control (ROLE_USER, ROLE_ADMIN).

### DevOps & Tools
- **Docker**: Containerization for consistent deployment.
- **Maven**: Dependency management.
- **Swagger/OpenAPI**: Interactive API documentation.
- **Lombok**: Boilerplate reduction.

### External Integrations
- **Spotify Web API** - Official Spotify integration

🏗️ Architecture & Design Decisions

1. **Fail-Safe Email Service** 📧
Due to strict SMTP port restrictions (blocking ports 587/465) on free cloud infrastructure tiers (e.g., Render), a "Fail-Safe" strategy was implemented in the AbstractEmailService.

- **Behavior**: The system attempts to send verification emails via SMTP. If the connection times out due to the firewall, the exception is caught, and the verification token is securely logged in the server console.

- **Rationale**: This ensures the **User Registration Transaction** is never rolled back due to infrastructure limitations, prioritizing User Experience (UX) and database integrity over external service availability.

2. **Security & Token Management** 🔐
The application uses a dual-token system for maximum security:

- **Access Token**: Short-lived JWT for API access.
- **Refresh Token**: Long-lived token to renew sessions without re-login.
- **OAuth2 Flow**: The backend handles the code exchange with Google, preventing the exposure of CLIENT_SECRET to the frontend.

## 📁 Project Structure

The project follows a layered architecture with a dedicated security module:

```
src/main/java/com/musicspring/app/music_app/
├── MusicAppApplication.java   # Entry Point
├── config/                    # Global Config (CORS, Swagger, Env)
├── controller/                # REST API Controllers
│   ├── AdminController.java   # Protected Admin Endpoints
│   ├── AuthController.java    # Login/Register/Refresh
│   ├── ... (Entity Controllers: Song, Album, Artist, Review)
├── exception/                 # Global Exception Handler & Custom Errors
├── model/                     # Domain Layer
│   ├── dto/                   # Data Transfer Objects (Req/Res)
│   ├── entity/                # JPA Entities (@Entity)
│   └── mapper/                # MapStruct/Custom Mappers
├── repository/                # Data Access Layer (JPA)
├── security/                  # 🛡️ CORE SECURITY MODULE
│   ├── config/                # SecurityFilterChain & PasswordEncoder
│   ├── filter/                # JwtAuthenticationFilter
│   ├── oauth2/                # CustomOAuth2UserService & Handlers
│   ├── service/               # AuthService, JwtService
│   └── util/                  # JWT Utilities
├── service/                   # Business Logic Layer
└── spotify/                   # Spotify Integration Module
    ├── config/                # SpotifyClient Config
    └── service/               # Spotify API Consumption Logic

```
## 🔧 Setup and Installation

### Prerequisites
- Java 21 JDK
- Maven 3.6+
- Docker (Optional)

### Environment Variables

Create a .env file or configure your IDE/Cloud provider with the following:

Variable	      Description
DB_URL	        JDBC URL (e.g., jdbc:postgresql://localhost:5432/musicdb)
DB_USERNAME	    Database username
DB_PASSWORD	    Database password
JWT_SECRET	    Secure 256-bit key for signing tokens
GOOGLE_CLIENT_ID	OAuth2 Client ID from Google Cloud Console
GOOGLE_CLIENT_SECRET	OAuth2 Client Secret
APP_OAUTH2_REDIRECT_URI	Frontend URL to redirect after login (e.g., http://localhost:4200/oauth2/redirect)


## 📚 API Documentation

The application provides comprehensive API documentation through Swagger/OpenAPI:

- **Interactive UI**: `[/swagger-ui.html](http://localhost:8080/swagger-ui/index.html)`
- **JSON Spec**: `/v3/api-docs`

### Main API Endpoints

#### 🔐 Auth & Users

- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - Classic login
- `GET /api/v1/users/profile` - Get current user details

#### 🛡️ Admin (Protected)

- `POST /api/v1/admin/users/{id}/ban` - - Ban a user
- `GET /api/v1/admin/stats` - System statistics

#### 🎵 Music & Spotify
- `GET /api/v1/spotify/search` - Search Spotify Catalog
- `GET /api/v1/songs` - List local songs
- `GET /api/v1/albums/{id}` - Get album details

#### 💬 Social
- `POST /api/v1/albums/{id}/reviews` - Review an album
- `POST /api/v1/reviews/{id}/comments` - Comment on a review
- `POST /api/v1/interactions/reaction` - Like/Unlike content

## 📄 License

This project is developed as part of UTN coursework.

## 👥 Authors

- Francisco Quiroga
- Manuel Palacios Inza
- Pablo Salom Pita
- Julieta Ramos
- Valentin Cerezuela

---

**Built with ❤️ by UTN Students**
