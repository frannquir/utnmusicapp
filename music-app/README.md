# 🎵 UTN Music App

A comprehensive music application developed by UTN students, featuring Spotify integration, social reviews, and user authentication. This Spring Boot application provides a complete music platform where users can discover, review, and interact with music content.

## 🚀 Features

### Core Music Features
- **Music Discovery**: Browse artists, albums, and songs
- **Spotify Integration**: Seamless integration with Spotify Web API for rich music data
- **Search Functionality**: Advanced search capabilities across all music content

### Social Features
- **Review System**: Write and read reviews for both albums and songs
- **Comment System**: Comment on reviews to foster discussions
- **Reaction System**: Like/dislike reviews and comments
- **User Profiles**: Comprehensive user management and profiles

### Advanced Features
- **Statistics Dashboard**: Analytics and insights on user activity and music trends
- **OAuth2 Authentication**: Secure login with Google OAuth2
- **JWT Security**: Token-based authentication for API security
- **RESTful API**: Complete REST API
- **API Documentation**: Interactive Swagger/OpenAPI documentation

## 🛠️ Tech Stack

### Backend
- **Java 21** - Latest LTS version for optimal performance
- **Spring Boot 3.4.5** - Modern Spring framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database abstraction layer

### Security & Authentication
- **JWT (JSON Web Tokens)** - Stateless authentication
- **OAuth2** - Google OAuth2 integration
- **Spring Security** - Comprehensive security framework

### External Integrations
- **Spotify Web API** - Official Spotify integration
- **Google OAuth2** - Social login functionality

### Development Tools
- **Lombok** - Reduces boilerplate code
- **Maven** - Dependency management and build tool
- **Swagger/OpenAPI** - API documentation
- **Environment Variables** - Secure configuration management

## 📁 Project Structure

```
src/main/java/com/musicspring/app/music_app/
├── config/              # Configuration classes
├── controller/          # REST API controllers
│   ├── AlbumController.java
│   ├── ArtistController.java
│   ├── SongController.java
│   ├── AlbumReviewController.java
│   ├── SongReviewController.java
│   ├── UserController.java
│   ├── ReactionController.java
│   └── StatisticController.java
├── exception/           # Global exception handling
├── model/               # Data models and DTOs
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA Entities
│   ├── enums/          # Enumerations
│   └── mapper/         # Entity-DTO mappers
├── repository/          # Data access layer
├── security/           # Security configuration
├── service/            # Business logic layer
├── spotify/            # Spotify integration module
│   ├── config/         # Spotify configuration
│   ├── controller/     # Spotify API controllers
│   ├── mapper/         # Spotify data mappers
│   ├── model/          # Spotify data models
│   ├── service/        # Spotify services
│   └── specification/  # Search specifications
└── MusicAppApplication.java  # Main application class
```
## 🔧 Setup and Installation

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Git


## 📚 API Documentation

The application provides comprehensive API documentation through Swagger/OpenAPI:

- **Interactive Documentation**: Available at `/swagger-ui.html`
- **OpenAPI Specification**: Available at `/v3/api-docs`

### Main API Endpoints

#### Music Endpoints
- `GET /api/artists` - List all artists
- `GET /api/albums` - List all albums  
- `GET /api/songs` - List all songs
- `GET /api/artists/{id}` - Get artist details

#### Review Endpoints
- `POST /api/albums/{id}/reviews` - Create album review
- `POST /api/songs/{id}/reviews` - Create song review
- `GET /api/reviews/{id}/comments` - Get review comments

#### User Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/users/profile` - Get user profile

#### Spotify Integration
- `GET /api/spotify/search` - Search Spotify catalog
- `GET /api/spotify/artist/{id}` - Get Spotify artist data


## 📄 License

This project is developed as part of UTN coursework.

## 👥 Authors

- Francisco Quiroga
- Manuel Palacios Inza
- Pablo Salom Pita
- Julieta Ramos
- Valient Cerezuela

---

**Built with ❤️ by UTN Students**