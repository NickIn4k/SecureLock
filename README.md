# SecureLock — Smart Key Management System

> An IoT-based secure key management system for garages and private facilities, built with Android (Kotlin), Node.js, MariaDB, and ESP32.

![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Backend](https://img.shields.io/badge/backend-Node.js%20%2B%20Express-green)
![Database](https://img.shields.io/badge/database-MariaDB-orange)
![Hardware](https://img.shields.io/badge/hardware-ESP32-blue)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Hardware](#hardware)
- [Software Stack](#software-stack)
- [Database Schema](#database-schema)
- [App Navigation](#app-navigation)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Future Improvements](#future-improvements)

---

## Overview

SecureLock is a smart, locally-hosted key management system designed to securely store and control access to vehicle keys. Each physical drawer in the unit is electronically locked and can only be opened through verified authentication — facial recognition or fingerprint — via a dedicated Android app.

The system is entirely **self-hosted on a local WiFi network**. No data leaves the premises.

### Use Cases

- Family homes with multiple vehicles
- Private garages or parking facilities
- Hotels or tourist facilities offering vehicles or bikes to guests

---

## Features

- **Facial recognition** — real-time face detection and embedding comparison using ML Kit and FaceNet
- **Fingerprint authentication** — native Android BiometricPrompt integration
- **Smart drawer control** — servo-motor-based locking mechanism triggered by the ESP32
- **Key presence detection** — IR sensors (TCRT5000) detect whether keys are in the drawer
- **RGB LED indicators** — visual status per drawer (green = key present, red = key missing, blue = opening)
- **Admin web dashboard** — access logs viewable from any browser on the local network
- **Full access logging** — who opened which drawer, when, and with which method
- **System event logging** — hardware errors, resets, servo and sensor events

---

## System Architecture

```
┌─────────────────────────────────────┐
│        Android App (Kotlin)         │
│  One device · Local WiFi network    │
│  [Face Auth] [Fingerprint] [Admin]  │
└──────────────┬──────────────────────┘
               │ HTTP REST
               ▼
┌─────────────────────────────────────┐
│     Backend — Node.js + Express     │
│  Always-on PC · Local network       │
│  [Auth API] [Face Match] [ESP32 Cmd]│
└────────┬──────────────┬─────────────┘
         │ SQL           │ HTTP
         ▼               ▼
┌──────────────┐  ┌──────────────────┐
│   MariaDB    │  │      ESP32       │
│  Local DB    │  │  Servo × 4       │
│  users       │  │  IR Sensors      │
│  embeddings  │  │  RGB LEDs        │
│  access_logs │  └──────────────────┘
└──────────────┘
```

All communication happens exclusively over the local WiFi network — no cloud, no external services.

---

## Hardware

| Component | Purpose | Notes |
|---|---|---|
| ESP32 Dev Kit C V4 | Main controller | Built-in WiFi, 30+ GPIO pins |
| Servo SG90 × 4 | Drawer lock/unlock mechanism | One per drawer |
| IR Sensor TCRT5000 × 4 | Key presence detection | Direct breadboard connection |
| RGB LED 5mm (common cathode) × 4 | Visual drawer status | One per drawer |
| 220Ω resistors | LED current limiting | 3 per RGB LED |
| 100µF / 16V capacitors | Servo power stabilization | Prevents ESP32 resets |
| 4×AAA battery pack (6V) | Servo power supply | Dedicated, with on/off switch |
| USB 5V charger | ESP32 power supply | Dedicated, always on |
| Breadboard + jumper wires | Connections | No soldering required |

### Drawer Opening Mechanism

Each drawer uses a spring-latch system:

1. **Locked state** — servo arm is down, physically blocking the drawer. Spring is compressed.
2. **Signal received** — ESP32 rotates the servo, arm lifts up.
3. **Drawer opens** — spring pushes the drawer out 2–3 cm.
4. **User opens fully** — takes or deposits the keys.
5. **Manual close** — user pushes the drawer in, spring recompresses, servo arm returns to locked position.

---

## Software Stack

### Android App
- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0)
- **Libraries:**
  - `ML Kit Face Detection` — real-time face detection
  - `TensorFlow Lite + FaceNet` — face embedding generation
  - `AndroidX Biometric` — fingerprint authentication
  - `CameraX` — camera access
  - `Retrofit` — HTTP calls to the Express backend

### Backend
- **Runtime:** Node.js
- **Framework:** Express
- **Libraries:** `bcrypt`, `cors`, `mysql2`

### Database
- **Engine:** MariaDB (local)

### Web Dashboard
- Plain HTML + CSS + JavaScript, served directly by Express
- Accessible from any browser on the local network
- Admin-only login

### ESP32 Firmware
- Arduino IDE or PlatformIO
- Libraries: `WiFi`, `WebServer`, `ESP32Servo`

---

## Database Schema

### `users`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | User ID |
| name | VARCHAR(100) | Full name |
| username | VARCHAR(50) UNIQUE | Login username |
| password_hash | VARCHAR(255) | bcrypt hashed password |
| role | ENUM('admin','user') | System role |
| auth_method | ENUM('face','fingerprint','both') | Enabled auth methods |
| is_active | BOOLEAN DEFAULT TRUE | Soft delete flag |
| created_at | DATETIME | Creation timestamp |

### `face_embeddings`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Embedding ID |
| user_id | INT FK → users.id | Associated user |
| embed_vector | JSON | FaceNet embedding vector |
| created_at | DATETIME | Registration date |

### `fingerprints`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Record ID |
| user_id | INT FK → users.id | Associated user |
| print_code | INT | Android biometric reference ID |
| created_at | DATETIME | Registration date |

### `vehicles`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Vehicle ID |
| name | VARCHAR(100) | Vehicle name (e.g. "Blue Bike") |
| type | VARCHAR(50) | Car / Bicycle / Other |

### `slots`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Drawer ID |
| status | VARCHAR(20) | open / closed |
| has_key | BOOLEAN | IR sensor reading |
| last_updated | DATETIME | Last status update |
| vehicle_id | INT NULL FK → vehicles.id | Associated vehicle |

### `access_logs`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Log entry ID |
| user_id | INT FK → users.id | User who accessed |
| slot_id | INT FK → slots.id | Accessed drawer |
| action | VARCHAR(20) | open / close |
| timestamp | DATETIME | Date and time |
| auth_method | ENUM('face','fingerprint') | Method used |
| success | BOOLEAN | Whether access was granted |

### `system_events`
| Field | Type | Description |
|---|---|---|
| id | INT AUTO_INCREMENT PK | Event ID |
| type | VARCHAR(50) | error / reset / servo / sensor |
| description | TEXT | Event details |
| timestamp | DATETIME | Event time |

---

## App Navigation

```
[First launch only]
        │
        ▼
  ┌─────────────┐
  │ Initial     │  Create admin profile
  │ Setup       │
  └──────┬──────┘
         │
         ▼
  ┌─────────────────────────────┐
  │           Home              │  No login required
  │  [Open Drawer] [Manage]     │
  └──────┬──────────────┬───────┘
         │              │
         ▼              ▼
  ┌────────────┐  ┌────────────┐
  │Recognition │  │ Admin Login│
  │Face/Finger │  │ User+Pass  │
  └──────┬─────┘  └─────┬──────┘
         │              │
         ▼              ▼
  ┌────────────┐  ┌────────────┐
  │  Drawer    │  │  Profile   │
  │  Opened    │  │ Management │
  └────────────┘  └────────────┘
```

---

## API Reference

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/setup` | Create admin profile (first run only) | None |
| POST | `/api/auth/face` | Face embedding match + open drawer | None |
| POST | `/api/auth/fingerprint` | Fingerprint confirmed + open drawer | None |
| POST | `/api/profiles` | Create new user profile | Admin |
| GET | `/api/profiles` | List all profiles | Admin |
| DELETE | `/api/profiles/:id` | Deactivate a profile | Admin |
| GET | `/api/logs` | Retrieve access logs | Admin |

### ESP32 Command
When access is authorized, the backend sends:
```
GET http://<ESP32_IP>/open?drawer=<ID>
```

---

## Getting Started

### Prerequisites
- Node.js 18+
- MariaDB 10.6+
- Android Studio (min SDK 26)
- Arduino IDE or PlatformIO
- A PC always connected to your local WiFi network

### Backend Setup
```bash
cd backend
npm install
# Configure your .env file
cp .env.example .env
# Start the server
node index.js
```

### Database Setup
```bash
mysql -u root -p < schema.sql
```

### ESP32 Firmware
Open the `firmware/` folder in Arduino IDE, set your WiFi credentials in `config.h`, and flash to the ESP32.

### Android App
Open the `android/` folder in Android Studio, update `BASE_URL` in `NetworkClient.kt` with your backend IP, and run on your device.

---

## Future Improvements

- QR code or NFC authentication support
- Advanced user role management
- Push notifications on key pickup or return
- Remote monitoring via web dashboard
- **MQTT protocol** — replacing HTTP for ESP32 communication, enabling real-time pub/sub messaging, lower network overhead, and better scalability

---

## Project Structure

```
securelock/
├── android/          ← Kotlin Android app
├── backend/          ← Node.js + Express server
│   ├── routes/
│   ├── middleware/
│   └── index.js
├── database/
│   └── schema.sql    ← MariaDB table definitions
├── firmware/         ← ESP32 Arduino sketch
└── README.md
```

---

*SecureLock — All data stays local. All access stays controlled.*
