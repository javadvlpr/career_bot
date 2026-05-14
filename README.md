# Career Counselling Telegram Bot

Telegram-based intelligent assistant that helps users find suitable job opportunities based on their profession, skills, and experience.

## Features

### For Job Seekers (Telegram Bot)
- Register and create a personal job profile
- Get smart job recommendations via bidirectional matching algorithm
- Save interesting job offers for later
- Receive daily/weekly job notifications
- Manage search status (Active / Passive / Not Looking)

### For Companies/HR (Telegram Bot)
- Register company with admin verification
- Post job vacancies (with moderation)
- Find matching candidates for posted jobs
- Send job offers directly to matching users

### Admin Panel (Web)
- Dashboard with statistics
- Company approval/rejection
- Job posting moderation
- User management
- Jooble API import management

## Tech Stack

- **Backend:** Java 17, Spring Boot 4.0.5
- **Database:** PostgreSQL 17
- **Bot:** Telegram Bot API (TelegramBots library)
- **Admin Panel:** Thymeleaf, HTML, CSS
- **External API:** Jooble API (job aggregation)
- **Security:** Spring Security (BCrypt)
- **API Docs:** Swagger / OpenAPI 3.0
- **Build:** Maven
- **Containerization:** Docker, Docker Compose

## Matching Algorithm

Bidirectional weighted scoring system:

```
score = (0.40 x skillMatch) + (0.25 x experienceMatch) + (0.20 x locationMatch) + (0.15 x salaryMatch)
```

- **Skill Match:** Ratio of matching skills to required skills
- **Experience Match:** Distance-based scoring (exact = 1.0, one level difference = 0.6)
- **Location Match:** Exact = 1.0, Remote = 0.8, No match = 0.0
- **Salary Match:** In-range = 1.0, proportional decrease with distance

## Project Structure

```
src/main/java/uz/career/career_bot/
├── bot/                  # Telegram bot handlers
│   ├── handler/          # Message and callback handlers
│   └── keyboard/         # Keyboard factory
├── config/               # Spring configurations
├── controller/           # REST API and Admin controllers
├── dto/                  # Data transfer objects
├── entity/               # JPA entities
├── enums/                # Status and type enums
├── repository/           # Spring Data JPA repositories
├── scheduler/            # Scheduled tasks
└── service/              # Business logic
```

## Setup and Run

### Prerequisites
- Java 17+
- PostgreSQL 17
- Maven 3.8+

### Local Development

1. Clone the repository:
```bash
git clone https://github.com/it-park-university-capstone-projects/BS23-SE-Abdurahmon-Imomnazarov.git
```

2. Create PostgreSQL database:
```sql
CREATE DATABASE career_bot;
```

3. Create `.env` file in project root:
```
DB_USERNAME=postgres
DB_PASSWORD=your_password
BOT_USERNAME=your_bot_username
BOT_TOKEN=your_bot_token
JOOBLE_API_KEY=your_jooble_key
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

### Docker

```bash
docker-compose up --build
```

### Access Points
- **Telegram Bot:** @careercounselling_bot
- **Admin Panel:** http://localhost:8080/admin/login (admin / admin123)
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **REST API:** http://localhost:8080/api/

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/jobs | Get all approved jobs |
| GET | /api/jobs/{id} | Get job by ID |
| GET | /api/users | Get all users |
| GET | /api/users/{id} | Get user by ID |
| GET | /api/categories | Get categories with skills |
| POST | /api/jooble/import | Import jobs from Jooble |
| GET | /api/stats | System statistics |

## Database Schema

Main tables: `users`, `companies`, `jobs`, `skills`, `categories`, `saved_jobs`, `job_offers`, `notifications`, `admin_users`

## Testing

```bash
./mvnw test
```

## Author

**Abdurahmon Imomnazarov** — BS23-SE, IT Park University

Diploma Project: Career Counselling Telegram Bot