# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that serves as an MQTT-based device management system for radar devices. It integrates with EMQX MQTT broker to handle device communication, authentication, and real-time data processing.

## Build and Run Commands

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```

### Compile Protocol Buffers
```bash
mvn protobuf:compile
mvn protobuf:compile-custom
```

### Run tests
```bash
mvn test
```

### Package as JAR
```bash
mvn clean package
```

## Configuration

### Application Configuration Files
- `src/main/resources/application.properties` - Server port and HTTPS configuration
- `src/main/resources/application.yml` - MQTT broker configuration

### HTTPS Configuration
The application is configured to run with HTTPS on port 8889:
- Certificate: `src/main/resources/keystore.p12`
- Access URL: `https://localhost:8889`
- The self-signed certificate is for development only

### MQTT Configuration
MQTT settings are in `application.yml`:
- EMQX broker connection details (host, port, credentials)
- Server-side MQTT client configuration
- Radar device MQTT configuration

## Architecture

### Core Components

**MQTT Client Layer** (`com.qinglan.example.device_point.mqtt`)
- `EmqxClient`: MQTT async client wrapper using Eclipse Paho MQTTv5
- `EmqxCallback`: Handles incoming MQTT messages
- `EmqxStart`: ApplicationRunner that connects to MQTT broker on startup
- `ServerClientMqttConfig` & `MqttClientConfiguration`: Spring configuration for MQTT clients

**Device Session Management** (`mqtt.session`)
- `DeviceRegSession`: Manages device online/offline state and message response caching
  - Uses Guava Cache for temporary message storage (4-second TTL)
  - Implements request-response pattern with BlockingQueue for synchronous MQTT communication
  - Tracks device connections via ConcurrentHashMap

**Device Communication Service** (`mqtt.service`)
- `MqttRadarMessage`: Interface defining device operations (OTA, property get/set, voice, etc.)
- `MqttRadarMessageCore`: Implementation of device communication protocol

**REST API Layer** (`controller`)
- `DeviceController`: Exposes HTTP endpoints for device operations
  - `/test` - Subscribe to device position/breath data
  - `/get/prop` - Get device properties
  - `/set/prop` - Set device properties
  - `/startVoice` - Initiate voice call
  - `/ota` - Trigger OTA firmware update
  - `/auth/connect` - Webhook for EMQX device connect/disconnect events
  - `/prod-api/thirdmqtt/v2/auth/device` - Device authentication endpoint

**Business Logic** (`service`)
- `DeviceService`: Device authentication and business logic

### Key Design Patterns

**Request-Response Pattern over MQTT**
The application implements synchronous request-response over asynchronous MQTT:
1. Client sends request via REST API
2. `DeviceRegSession.initReceiveMsg(key)` creates a BlockingQueue for the response
3. Request is published to MQTT topic
4. `DeviceRegSession.waitReceiveMsg(key)` blocks waiting for response (4s timeout)
5. When device responds via MQTT, `setReceiveMsg(key, msg)` adds to queue
6. Original request unblocks and returns response to REST client

**Device Lifecycle Management**
- Devices connect to EMQX with clientId format: `radar_{uid}`
- EMQX webhooks notify this application of connect/disconnect events via `/auth/connect`
- `DeviceRegSession` maintains online device registry
- All device operations check `isReg(uid)` before proceeding

### Protocol Buffers
The project uses Protocol Buffers (protobuf) for message serialization:
- Proto files should be placed in `src/main/proto/`
- Maven plugin automatically generates Java classes during compilation
- Generated code goes to `src/main/java/`

## Important Notes

### MQTT Topic Structure
Device topics follow a pattern based on device UID. Check `MqttRadarMessageCore` for specific topic formats.

### Device Authentication
The `/prod-api/thirdmqtt/v2/auth/device` endpoint handles device authentication requests with uid, type, and auth parameters.

### Session Management
- Device sessions use in-memory storage (ConcurrentHashMap)
- Response caching has 4-second TTL via Guava Cache
- No persistence layer - all state is lost on restart

### Dependencies
- Spring Boot 2.7.0
- Eclipse Paho MQTT v5 Client 1.2.5
- Protocol Buffers 3.22.0
- gRPC 1.11.0
- Fastjson2 2.0.20
- Guava (via transitive dependencies)

## Development Workflow

1. Ensure EMQX broker is running and accessible at configured host/port
2. Update MQTT credentials in `application.yml` if needed
3. Place any new `.proto` files in `src/main/proto/`
4. Run `mvn protobuf:compile` to generate Java classes from proto files
5. Implement device message handlers in `MqttRadarMessageCore`
6. Add REST endpoints in `DeviceController` as needed
7. Test with actual radar devices or MQTT client simulators
