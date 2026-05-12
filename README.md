# SecureLock — Smart Key Management System

> **Authors:** Nicola Creazzo & Andrea Bassan

[![Platform](https://img.shields.io/badge/platform-Android%20(Kotlin)-brightgreen?logo=android)](https://developer.android.com)
[![Backend](https://img.shields.io/badge/backend-Node--RED-red?logo=nodered)](https://nodered.org)
[![Database](https://img.shields.io/badge/database-MariaDB-orange?logo=mariadb)](https://mariadb.org)
[![Hardware](https://img.shields.io/badge/hardware-ESP32-blue?logo=espressif)](https://www.espressif.com)
[![MQTT](https://img.shields.io/badge/broker-HiveMQ%20Cloud-purple?logo=mqtt)](https://www.hivemq.com)
[![Dashboard](https://img.shields.io/badge/monitoring-Grafana-F46800?logo=grafana)](https://grafana.com)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

---

## Table of Contents

- [Overview](#overview)
- [Use Cases](#use-cases)
- [Key Highlights](#key-highlights)
- [System Architecture](#system-architecture)
- [Hardware](#hardware)
- [Software Stack](#software-stack)
- [Node-RED Backend — Flow Reference](#node-red-backend--flow-reference)
- [MQTT Protocol](#mqtt-protocol)
- [Database Schema](#database-schema)
- [App Screens & Navigation](#app-screens--navigation)
- [API Reference](#api-reference)
- [Grafana Dashboard](#grafana-dashboard)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)

---

## Overview

SecureLock is an IoT-based smart key management system designed to physically secure and control access to vehicle keys through verified biometric authentication. Each key drawer is electronically locked via a servo mechanism controlled by an ESP32 microcontroller.

Authentication is handled entirely on a dedicated Android app — using **facial recognition** (ML Kit + FaceNet) or a **password** — and commands are dispatched to the hardware through **MQTT over TLS**, enabling real-time, reliable communication even across multiple devices.

The system supports **multiple independent buildings**, each managed by its own admin and connected to its own ESP32 device. This makes SecureLock equally suited for private homes, shared garages, hotel facilities, or any multi-tenant environment.

---

## Use Cases

| Context | Description |
|---|---|
| 🏠 **Private Home** | A family manages their vehicle keys centrally — no more lost keys, no more guessing who took what |
| 🏨 **Hotel / B&B** | Guests receive controlled access to specific drawers; admins track pickups in real time |
| 🏫 **School / Institution** | A single superadmin provisions multiple buildings from one app; each facility has its own admin |
| 🅿️ **Private Garage** | Shared facilities with clear ownership boundaries per slot and per vehicle |

---

## Key Highlights

- **Facial Recognition on Android** — FaceNet 128-dimension embeddings generated on-device with ML Kit, compared server-side via cosine similarity + Euclidean distance. A double threshold (`score ≥ 0.93` and `distance ≤ 0.40`) prevents false positives. This represents one of the most advanced uses of on-device ML in Android/Kotlin today.
- **Multi-building support** — Each building is isolated: its own admin, device, slots, users, vehicles, and access logs. The superadmin can provision new facilities at any time from the app.
- **MQTT over TLS** — ESP32 communicates with Node-RED via HiveMQ Cloud. Commands and status updates travel as JSON on structured topics (`securelock/devices/{uid}/slots/{id}/cmd` and `/status`), enabling async and reliable messaging.
- **Full audit trail** — Every login attempt, drawer command, and hardware event is logged to the database with timestamp, user, method, and outcome.
- **Grafana monitoring** — Admins can access a live dashboard (linked from the app) showing registered users, today's accesses, access logs, and system events — all filtered by building.

---

## System Architecture

```
┌─────────────────────────────────────────────────┐
│               Android App (Kotlin)               │
│  Jetpack Compose · CameraX · ML Kit · FaceNet   │
│  [Face Auth] [Password Auth] [Admin Panel]       │
└────────────────────┬────────────────────────────┘
                     │ HTTP REST (Retrofit)
                     ▼
┌─────────────────────────────────────────────────┐
│          Node-RED — Backend & API Layer          │
│  HTTP In nodes · Function nodes · MySQL nodes   │
│  MQTT Out · Token-based admin session           │
└─────────┬──────────────────────┬────────────────┘
          │ SQL (mysql2)          │ MQTT over TLS (QoS 1)
          ▼                       ▼
┌──────────────────┐    ┌─────────────────────────┐
│     MariaDB      │    │     HiveMQ Cloud Broker  │
│  db_securelock   │    │  TLS · Port 8883         │
│  users           │    └──────────────┬───────────┘
│  slots           │                   │ MQTT subscribe/publish
│  buildings       │                   ▼
│  devices         │    ┌─────────────────────────┐
│  access_logs     │    │   ESP32 Dev Kit C V4     │
│  system_events   │    │  Servo × 3 · IR Sensors  │
│  face_embeddings │    │  RGB LEDs · WiFi         │
│  vehicles        │    └─────────────────────────┘
│  user_slots      │
└──────────────────┘
                     │ MariaDB (direct)
                     ▼
          ┌──────────────────┐
          │   Grafana v13    │
          │  Live Dashboard  │
          │  per building_id │
          └──────────────────┘
```

> All HTTP traffic stays on the local WiFi network. Only MQTT commands travel over the internet (HiveMQ Cloud, TLS encrypted). No user data ever leaves the local database.

---

## Hardware

| Component | Qty | Purpose |
|---|---|---|
| ESP32 Dev Kit C V4 | 1 | Main controller — WiFi, GPIO, MQTT client |
| Servo SG90 | 3 | Drawer locking/unlocking mechanism |
| IR Sensor TCRT5000 | 3 | Key presence detection per drawer |
| RGB LED 5mm (common cathode) | 3 | Visual status indicator per drawer |
| 220Ω resistors | 9 | LED current limiting (3 per RGB LED) |
| 100µF / 16V capacitors | 3 | Servo power stabilisation — prevents resets |
| 4×AAA battery pack (6V) | 1 | Dedicated servo power supply |
| USB 5V charger | 1 | Dedicated ESP32 power supply |
| Breadboard + jumper wires | — | No soldering required |

### LED Status Indicators

| Colour | Meaning |
|---|---|
| 🟢 Green | Key is present in the drawer |
| 🔴 Red | Key is missing from the drawer |
| 🔵 Blue | Drawer is opening / in transition |

### Drawer Locking Mechanism

Each drawer uses a spring-latch system:

1. **Locked** — servo arm is down, blocking the drawer. Spring is compressed.
2. **Command received** — Node-RED publishes an MQTT command; ESP32 rotates the servo arm up.
3. **Drawer pops open** — spring pushes it out 2–3 cm.
4. **User takes/deposits keys** — IR sensor detects key presence change.
5. **Manual close** — user pushes the drawer in; servo returns to locked position on next auto-close event.

---

## Software Stack

### Android App
| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Min SDK | 26 (Android 8.0) |
| Camera | CameraX |
| Face detection | ML Kit Face Detection |
| Face embedding | TensorFlow Lite + FaceNet (128-dim) |
| Networking | Retrofit 2 |
| Local storage | SharedPreferences (`SetupPrefs`) |

### Backend
| Component | Technology |
|---|---|
| Runtime | Node-RED |
| Database | MariaDB 10.4+ via `node-red-node-mysql` |
| MQTT broker | HiveMQ Cloud (TLS, port 8883) |
| Password hashing | SHA-256 (via Node.js `crypto`) |
| Admin sessions | In-memory token with expiry (`flow.get/set`) |

### Monitoring
| Component | Technology |
|---|---|
| Dashboard | Grafana v13 |
| Data source | MariaDB (`db_securelock`) |
| Filter | `$building_id` variable per admin |

### ESP32 Firmware
| Component | Technology |
|---|---|
| IDE | Arduino IDE / PlatformIO |
| Libraries | `WiFi`, `PubSubClient` (MQTT), `ESP32Servo` |

---

## Node-RED Backend — Flow Reference

The entire backend logic lives in a single Node-RED flow. Each section is a self-contained pipeline of `HTTP In → Function → MySQL → Function → HTTP Response` nodes.

### Setup — `/api/setup/install` `POST`
Provisions a complete new building in one atomic operation:
1. Validates superadmin identity
2. Inserts building (name, address, GPS coordinates)
3. Creates admin user (SHA-256 password hash)
4. Registers ESP32 device (`device_uid`)
5. Creates N drawer slots (default: 3)
6. Assigns all slots to the new admin

### User Authentication

**Password login** — `/api/auth/login/password` `POST`  
Validates username + SHA-256 hashed password against `users` table. Returns userId, role, buildingId, and a Grafana dashboard URL.

**Face login** — `/api/auth/login/face` `POST`  
Receives a 128-float FaceNet embedding. Node-RED fetches all known embeddings from `face_embeddings`, then runs a JS function that:
- Normalises both vectors
- Computes **cosine similarity** and **Euclidean distance**
- Accepts the match only if `score ≥ 0.93` **and** `distance ≤ 0.40`

Returns the matched user's data or a 401 rejection.

### Face Enrollment (Admin)

**Duplicate check** — `/api/admin/face/checkduplicati` `POST`  
Before saving a new face, the same cosine + distance algorithm is run against all stored embeddings. Returns 409 if a match is found, protecting against accidental double-registration.

**Save face** — `/api/admin/face/save` `POST`  
Stores the validated embedding vector as JSON in `face_embeddings`.

### User Management (Admin)

| Flow | Endpoint | Description |
|---|---|---|
| Insert user | `/api/admin/insertuser` `POST` | Validates admin role, checks username uniqueness, creates user with SHA-256 password, assigns slots |
| List users | `/api/admin/building/users` `GET` | Returns all `role='user'` users belonging to the admin's building |
| Delete user | `/api/admin/user/delete` `POST` | Cascades to `user_slots` and `face_embeddings` via FK constraints |

### Slot & Vehicle Management

| Flow | Endpoint | Description |
|---|---|---|
| User dashboard | `/api/user/dashboard` `GET` | Returns all slots assigned to a user with status, key presence, and vehicle info |
| Slot detail | `/api/slot/detail` `GET` | Single slot detail with authorization check |
| Admin slots | `/api/admin/building/slots` `GET` | All slots in the admin's building |
| List vehicles | `/api/admin/building/vehicles` `GET` | All vehicles registered to the building |
| Create vehicle | `/api/admin/vehicle/create` `POST` | Adds a new vehicle (name + type) to the building |
| Delete vehicle | `/api/admin/vehicle/delete` `POST` | Removes vehicle, unlinks from slots |
| Assign vehicle | `/api/admin/slot/assignvehicle` `POST` | Associates a vehicle to a specific slot |

### Slot Action — `/api/slot/action` `POST`
The core flow that triggers physical drawer opening:

1. Validates `userId`, `deviceId`, `slotId`, `action`
2. Checks `user_slots` join to confirm the user has permission for that exact slot
3. Looks up `device_uid` from `devices`
4. **Publishes MQTT command** to `securelock/devices/{uid}/slots/{id}/cmd`
5. Returns HTTP 202 immediately (async — hardware confirmation via MQTT status)
6. Logs the command to `system_events`

### Admin Session & Grafana Link

**Admin login** — `/api/admin/login` `POST`  
Accepts `adminUserId`, verifies role is `admin`, generates a short-lived in-memory token (`flow.set`), and constructs a Grafana URL pre-filtered by `building_id`.

**Token check** — `/api/admin/check-token` `GET`  
Validates token value and expiry from flow context.

---

## MQTT Protocol

The ESP32 and Node-RED communicate entirely via MQTT over TLS (HiveMQ Cloud, port 8883).

### Topic Structure

```
securelock/devices/{device_uid}/slots/{slot_id}/cmd      ← Node-RED → ESP32
securelock/devices/{device_uid}/slots/{slot_id}/status   ← ESP32 → Node-RED
```

### Command Payload (Node-RED → ESP32)
```json
{
  "action": "open",
  "userId": 41,
  "deviceId": 3,
  "slotId": 1,
  "ts": "2026-05-07T09:18:26.037Z"
}
```

### Status Payload (ESP32 → Node-RED)
```json
{
  "state": "closed",
  "keyPresent": false,
  "slotId": 1,
  "deviceUid": "Esp_2345",
  "event": "auto_close",
  "ts": "..."
}
```

When Node-RED receives a status update it:
1. Updates `slots.status` and `slots.has_key` in MariaDB
2. Logs the event to `system_events` with full JSON payload

### MQTT Broker Configuration

| Setting | Value |
|---|---|
| Broker | `*.s1.eu.hivemq.cloud` |
| Port | `8883` (TLS) |
| Protocol | MQTT v3.1.1 |
| QoS | 1 (at least once) |
| Client ID (Node-RED) | `node_red_main` |
| TLS | Server certificate verification enabled |

---

## Database Schema

**Database name:** `db_securelock` — MariaDB 10.4+ — charset `utf8mb4`

### `buildings`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Building ID |
| name | VARCHAR(100) | Display name |
| address | VARCHAR(255) | Street address |
| lat / lng | DECIMAL(10,8) / DECIMAL(11,8) | GPS coordinates |
| created_at | DATETIME | Provisioning timestamp |

### `users`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | User ID |
| name | VARCHAR(100) | Full name |
| username | VARCHAR(50) UNIQUE | Login username |
| password_hash | VARCHAR(255) | SHA-256 hex digest |
| role | ENUM(`superadmin`, `admin`, `user`) | Access role |
| building_id | INT FK → buildings.id | Assigned building (NULL for superadmin) |
| created_at | DATETIME | Creation timestamp |

### `devices`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Device record ID |
| device_uid | VARCHAR(100) UNIQUE | ESP32 identifier (e.g. `Esp_1234`) |
| building_id | INT FK → buildings.id | Owning building |
| created_at | DATETIME | Registration timestamp |

### `slots`
| Column | Type | Description |
|---|---|---|
| id + device_id | Composite PK | Slot number + owning device |
| status | VARCHAR(20) | `open` / `closed` |
| has_key | TINYINT(1) | IR sensor reading |
| last_updated | DATETIME | Last status change |
| vehicle_id | INT FK → vehicles.id | Associated vehicle (nullable) |
| building_id | INT FK → buildings.id | Owning building |

### `user_slots`
| Column | Type | Description |
|---|---|---|
| user_id + device_id + slot_id | Composite PK | Uniqueness constraint |
| assigned_at | DATETIME | Assignment timestamp |

### `vehicles`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Vehicle ID |
| building_id | INT FK → buildings.id | Owning building |
| name | VARCHAR(120) | Display name (e.g. `Blue Bike`) |
| type | VARCHAR(80) | `car`, `bike`, `key`, etc. |
| created_at | DATETIME | Creation timestamp |

### `face_embeddings`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Embedding record ID |
| user_id | INT FK → users.id CASCADE | Associated user |
| embed_vector | TEXT | JSON array of 128 floats (FaceNet) |
| created_at | DATETIME | Registration date |

### `access_logs`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Log entry ID |
| user_id | INT (nullable) | User who attempted access |
| building_id | INT (nullable) | Target building |
| action | VARCHAR(32) | `login`, etc. |
| success | TINYINT(1) | 1 = granted, 0 = denied |
| auth_method | VARCHAR(16) | `password`, `face` |
| event_time | TIMESTAMP | Event date/time |

### `system_events`
| Column | Type | Description |
|---|---|---|
| id | INT PK AUTO_INCREMENT | Event ID |
| type | VARCHAR(50) | `slot_command`, `mqtt_status`, `mqtt_error` |
| description | TEXT | Human-readable summary |
| device_uid | VARCHAR(64) | Originating device |
| slot_id | INT | Affected slot |
| user_id | INT (nullable) | User who triggered the event |
| action | VARCHAR(32) | `open`, `closed`, etc. |
| event_data | TEXT | Full JSON payload |
| timestamp | DATETIME | Event time |

---

## App Screens & Navigation

```
[First launch]
      │
      ▼
 ┌──────────────────┐
 │   Welcome Screen  │  Splash + branding
 └────────┬─────────┘
          │
          ▼
 ┌──────────────────┐
 │   Setup Screen   │  Superadmin provisions a new building
 │  (first run only)│  (name, address, GPS, admin, device UID)
 └────────┬─────────┘
          │
          ▼
 ┌──────────────────┐
 │   Home Screen    │  Entry point for all users
 │  [Face Auth]     │  No login required to reach this screen
 │  [Password Auth] │
 │  [Admin Panel]   │
 └─┬────────────────┘
   │
   ├──► Face Auth Screen ──► [Drawer open confirmation]
   │     CameraX live preview
   │     ML Kit face detection
   │     FaceNet embedding → API
   │
   ├──► Login Screen ──► [Drawer open confirmation]
   │     Username + password
   │
   └──► Admin Login Screen
         │
         └──► Admin Panel
               ├── User Management (create / delete)
               │     └── Face enrollment (per new user)
               ├── Vehicle Management (create / delete / assign to slot)
               ├── Slot Overview (status, key presence, vehicle)
               └── Grafana Dashboard (opens in browser, pre-filtered)
```

### Screens in Code

| Screen | File | Description |
|---|---|---|
| WelcomeScreen | `WelcomeScreen.kt` | Splash / branding |
| HomeScreen | `HomeScreen.kt` | Main entry, mode selection |
| SetupScreen | `SetupScreen.kt` | First-run building provisioning |
| LoginScreen | `LoginScreen.kt` | Password authentication |
| FaceAuthScreen | `FaceAuthScreen.kt` | Live camera + FaceNet auth |
| SlotDetailScreen | `SlotDetailScreen.kt` | Slot info + open action |
| AdminNewUserScreen | `AdminNewUserScreen.kt` | Create user + face enrollment |
| AdminVehicleScreen | `AdminVehicleScreen.kt` | Vehicle CRUD + slot assignment |
| CreditsScreen | `CreditsScreen.kt` | Authors & project info |

---

## API Reference

All endpoints are served by Node-RED on the local network (default port: `1880`).

### Authentication

| Method | Endpoint | Body / Query | Description |
|---|---|---|---|
| POST | `/api/auth/login/password` | `{username, password}` | Password login |
| POST | `/api/auth/login/face` | `{embedding: [float×128]}` | Face embedding login |

### Setup

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/setup/install` | `{superAdminId, buildingName, buildingAddress, lat, lng, adminFullName, adminUsername, adminPassword, deviceUid, numberOfSlots}` | Provision new building (requires superadmin) |

### Admin — Users

| Method | Endpoint | Body / Query | Description |
|---|---|---|---|
| POST | `/api/admin/login` | `{adminUserId}` | Admin session + Grafana URL |
| GET | `/api/admin/check-token` | `?token=` | Validate admin session token |
| POST | `/api/admin/insertuser` | `{adminUserId, fullName, username, password, slotIds[]}` | Create user |
| GET | `/api/admin/building/users` | `?adminUserId=` | List building users |
| POST | `/api/admin/user/delete` | `{adminUserId, userId}` | Delete user |

### Admin — Face

| Method | Endpoint | Body | Description |
|---|---|---|---|
| POST | `/api/admin/face/checkduplicati` | `{faceEmbedding: [float×128]}` | Check for duplicate face |
| POST | `/api/admin/face/save` | `{userId, faceEmbedding: [float×128]}` | Save face embedding |

### Admin — Vehicles & Slots

| Method | Endpoint | Body / Query | Description |
|---|---|---|---|
| GET | `/api/admin/building/slots` | `?adminUserId=` | All slots in building |
| GET | `/api/admin/building/vehicles` | `?adminUserId=` | All vehicles in building |
| POST | `/api/admin/vehicle/create` | `{adminUserId, name, type}` | Create vehicle |
| POST | `/api/admin/vehicle/delete` | `{adminUserId, vehicleId}` | Delete vehicle |
| POST | `/api/admin/slot/assignvehicle` | `{adminUserId, slotId, vehicleId}` | Assign vehicle to slot |

### User — Slots

| Method | Endpoint | Body / Query | Description |
|---|---|---|---|
| GET | `/api/user/dashboard` | `?userId=` | User's assigned slots |
| GET | `/api/slot/detail` | `?userId=&deviceId=&slotId=` | Single slot detail |
| POST | `/api/slot/action` | `{userId, deviceId, slotId, action}` | Open/close a slot (triggers MQTT) |

---

## Grafana Dashboard

The Grafana dashboard (`SecureLock` — UID `adp9jpd`) is linked directly from the app after admin login. It is pre-filtered by `$building_id` so each admin only sees their own data.

### Panels

| Panel | Type | Description |
|---|---|---|
| **Registered Users** | Stat | Count of non-superadmin users in the building |
| **Accesses Today** | Stat | Successful logins in the current day |
| **Last 20 Accesses** | Table | User, action, auth method, outcome (Authorised / Denied) |
| **Latest System Events** | Table | Device events — slot commands, MQTT status updates, errors |

### Configuration

1. Install Grafana and connect it to MariaDB as a data source
2. Import the provided `dashboard-*.json` file
3. Set the `building_id` variable to match the building you want to monitor
4. Grafana must be reachable on the local network (default: port `3000`)

---

## Getting Started

### Prerequisites

- A PC always connected to the local WiFi network (runs Node-RED + MariaDB)
- Node-RED installed (`npm install -g --unsafe-perm node-red`)
- MariaDB 10.4+
- Grafana v13 (optional, for monitoring)
- Arduino IDE or PlatformIO (for ESP32 firmware)
- Android Studio (min SDK 26) for the app

### 1 — Database Setup

```bash
mysql -u root -p < db_securelock.sql
```

This creates the `db_securelock` database with all tables and sample data.

### 2 — Node-RED Setup

```bash
# Install the MySQL node
npm install -g node-red-node-mysql

# Start Node-RED
node-red
```

Then open `http://localhost:1880`, go to **Menu → Import**, and import `flows.json`.

Configure the MySQL node:
- Host: `127.0.0.1`
- Port: `3306`
- Database: `db_securelock`
- Charset: `UTF8`

Configure the MQTT broker node with your HiveMQ Cloud credentials:
- Broker: `<your-cluster>.s1.eu.hivemq.cloud`
- Port: `8883`
- TLS: enabled
- Username / Password: your HiveMQ credentials

### 3 — Grafana Setup (optional)

```bash
# Start Grafana (if installed as service)
sudo systemctl start grafana-server
```

1. Open `http://localhost:3000`
2. Add MariaDB as a data source (host `127.0.0.1:3306`, database `db_securelock`)
3. Import `dashboard-*.json` via **Dashboards → Import**

### 4 — ESP32 Firmware

Open the firmware folder in Arduino IDE:

1. Install libraries: `PubSubClient`, `ESP32Servo`
2. Set your WiFi credentials and HiveMQ credentials in `config.h`
3. Set your `DEVICE_UID` to match the one you'll register in the app (e.g. `Esp_1234`)
4. Flash to the ESP32

### 5 — Android App

Open the `SecureLock/` folder in Android Studio:

1. In `ApiClient.kt`, set `BASE_URL` to your Node-RED server IP (e.g. `http://192.168.1.100:1880/`)
2. Run on a physical device (camera required for face auth)

### 6 — First Launch

On first app launch, the **Setup Screen** will appear. You need:

- The **superadmin ID** (default from the SQL dump: user ID `34`, username `superAdmin`)
- Building name, address, and GPS coordinates
- Admin credentials for the new building
- The **device UID** exactly as flashed on the ESP32 (e.g. `Esp_1234`)
- Number of physical drawers (default: 3)

After setup, the building, admin user, device, and slots are all provisioned in one step.

---

## Project Structure

```
SecureLock/
├── app/
│   └── src/main/java/com/example/securelock/
│       ├── auth/
│       │   └── FaceNetHelper.kt          ← FaceNet embedding generation
│       ├── network/
│       │   ├── ApiClient.kt              ← Retrofit client (BASE_URL here)
│       │   └── ApiService.kt             ← All API endpoint definitions
│       ├── storage/
│       │   └── SetupPrefs.kt             ← SharedPreferences wrapper
│       ├── ui/
│       │   ├── admin/                    ← Admin screens & components
│       │   ├── components/               ← Shared UI components
│       │   ├── theme/                    ← Compose theme (colors, typography)
│       │   ├── FaceAuthScreen.kt
│       │   ├── HomeScreen.kt
│       │   ├── LoginScreen.kt
│       │   ├── Navigation.kt             ← NavHost & route definitions
│       │   ├── SetupScreen.kt
│       │   ├── SlotDetailScreen.kt
│       │   └── WelcomeScreen.kt
│       └── MainActivity.kt
├── flows.json                            ← Node-RED flow export
├── dashboard-*.json                      ← Grafana dashboard export
└── db_securelock.sql                     ← MariaDB schema + seed data
```

---

*SecureLock — All access, controlled. All data, local.*
