# University Management System

A simple REST API built with Java 17, Spring Boot, Spring Security, Spring Data JPA, and PostgreSQL.

## Run with Docker Compose

From the project directory:

```bash
docker compose up --build
```

The API will be available at `http://localhost:8080`.
Open `http://localhost:8080` for the web application. Sign in to reach the role-based dashboard and navigate between account lists and forms.

Default administrator:
- Username: `admin`
- Password: `admin123`

The default administrator is created automatically on first startup. Change its password for any non-demo use.

## Web application

Sign in at `http://localhost:8080/login` using the default administrator account below. Admins can manage all accounts; teachers and students see and manage only their own account type. Use the dashboard cards to open a list, add an account, edit, or delete, and use the navigation links to return to the dashboard.

## REST API authentication

The web application uses a server-side session. The REST API login also creates a session; keep the `JSESSIONID` cookie returned by the login request and send it with subsequent API requests.

```http
POST /api/auth/login
Content-Type: application/json

{"username":"admin","password":"admin123"}
```

Logout:

```http
POST /api/auth/logout
```

## REST endpoints

All successful responses use JSON. Create returns `201 Created` with a `Location` header for the new resource, reads and updates return `200 OK`, and deletes return `204 No Content`. Missing resources return `404 Not Found`; invalid input returns `400 Bad Request`; unauthenticated and unauthorized requests return `401` and `403`.

| Resource | Endpoints | Allowed roles |
|---|---|---|
| Admins | `/api/admins` and `/api/admins/{id}` | ADMIN |
| Teachers | `/api/teachers` and `/api/teachers/{id}` | ADMIN, TEACHER |
| Students | `/api/students` and `/api/students/{id}` | ADMIN, STUDENT |

For each resource:
- `POST /api/{resource}` — create
- `GET /api/{resource}` — list all
- `GET /api/{resource}/{id}` — get one
- `PUT /api/{resource}/{id}` — update
- `DELETE /api/{resource}/{id}` — delete

Example create body (the resource endpoint determines the role; do not send a role field):

```json
{
  "username": "teacher1",
  "password": "pass123",
  "fullName": "Alex Rahman",
  "email": "alex@example.com",
  "department": "Computer Science"
}
```

For students, use `studentId` instead of `department`. For admins, use `department`.
Updates use `PUT` with the same fields, including a new password.

## Notes

- Passwords are stored using BCrypt hashing and are never included in API responses.
- Admins can manage all three resource types.
- Teachers can manage teacher resources only.
- Students can manage student resources only.
- Database data is persisted in the `postgres_data` Docker volume.
