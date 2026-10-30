
# Circle Backend

A Spring Boot backend application that connects a Java Swing circle rotation application to a React web interface.

The backend communicates with the Circle Rotation Demo through TCP, provides REST API endpoints for controlling the circle, and uses WebSocket/STOMP to deliver real-time status updates to the frontend.

## Features

- TCP communication with the Circle Rotation Demo
- REST API endpoints for controlling circle rotation
- Start, stop, and angle adjustment commands
- Real-time status updates using WebSocket/STOMP
- Integration between Java Swing and React applications
- Docker support

## Technologies Used

- Java 21
- Spring Boot
- REST APIs
- TCP
- WebSocket/STOMP
- Maven
- Docker

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/circle/status` | Retrieve the current circle status |
| POST | `/api/circle/start` | Start circle rotation |
| POST | `/api/circle/stop` | Stop circle rotation |
| PUT | `/api/circle/angle` | Set the circle angle |

## How to Run

**Requirements:** Java 21, Maven Wrapper (included), and the `network-lib` dependency installed locally.

1. Clone the repository:

```bash
git clone https://github.com/dnzalparslan/circle-backend.git
cd circle-backend
```

2. Start the Circle Rotation Demo application separately.

3. Run the Spring Boot backend:

```bash
./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

It connects to the Circle Rotation Demo at `localhost:50010` by default.

The TCP server address can be configured using the `CIRCLE_SERVER_HOST` and `CIRCLE_SERVER_PORT` environment variables.

## Related Projects

- [Network Library](https://github.com/dnzalparslan/network-lib) – Reusable Java networking library
- [Circle Rotation Demo](https://github.com/dnzalparslan/circle-rotation-demo) – Java Swing application controlled through network commands

## What I Learned

This project helped me gain experience developing backend services with Spring Boot and integrating multiple applications through different communication protocols. I also explored how REST APIs, TCP connections, and WebSockets can work together to support real-time application updates.
