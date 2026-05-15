#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>
#include <time.h>
#include "secrets.h"

// Numero cassetti
const int NUM_DRAWERS = 3;

// SERVO
const int SERVO_PINS[NUM_DRAWERS] = {18, 19, 21};

// LED RGB
const int LED_R[NUM_DRAWERS] = {25, 26, 27};
const int LED_G[NUM_DRAWERS] = {14, 12, 13};
const int LED_B[NUM_DRAWERS] = {33, 32, 4};

// IR
const int IR_PINS[NUM_DRAWERS] = {34, 35, 22};

// Servo
const int SERVO_LOCKED = 0;
const int SERVO_OPEN   = 90;
const int OPEN_DELAY_MS = 5000;

// Oggetti
WiFiClientSecure wifiClient;
PubSubClient mqttClient(wifiClient);
Servo servos[NUM_DRAWERS];

// ############ FUNZIONI ############

// Stampa separata
void printLine() {
    Serial.println("--------------------------------------------------");
}

// Stampa setup WiFi
void printWiFiStatus() {
    Serial.print("[WIFI] Stato: ");
    Serial.println(WiFi.status());

    Serial.print("[WIFI] IP: ");
    Serial.println(WiFi.localIP());

    Serial.print("[WIFI] RSSI: ");
    Serial.println(WiFi.RSSI());
}

// Costruzione topic MQTT
void buildCmdTopic(char* out, size_t outSize, int slotId) {
    snprintf(out, outSize, "securelock/devices/%s/slots/%d/cmd", DEVICE_UID, slotId);
}

void buildStatusTopic(char* out, size_t outSize, int slotId) {
    snprintf(out, outSize, "securelock/devices/%s/slots/%d/status", DEVICE_UID, slotId);
}

void buildSlotsPrefix(char* out, size_t outSize) {
    snprintf(out, outSize, "securelock/devices/%s/slots/", DEVICE_UID);
}

// LED RGB
// HIGH = acceso, LOW = spento

void setLedRed(int i) {
    digitalWrite(LED_R[i], HIGH);
    digitalWrite(LED_G[i], LOW);
    digitalWrite(LED_B[i], LOW);
}

void setLedGreen(int i) {
    digitalWrite(LED_R[i], LOW);
    digitalWrite(LED_G[i], HIGH);
    digitalWrite(LED_B[i], LOW);
}

void setLedBlue(int i) {
    digitalWrite(LED_R[i], LOW);
    digitalWrite(LED_G[i], LOW);
    digitalWrite(LED_B[i], HIGH);
}

void setLedOff(int i) {
    digitalWrite(LED_R[i], LOW);
    digitalWrite(LED_G[i], LOW);
    digitalWrite(LED_B[i], LOW);
}

// IR
// LOW = chiave presente
// HIGH = nessuna chiave
bool hasKey(int i) {
    return digitalRead(IR_PINS[i]) == HIGH;
}

// MQTT
void buildIsoTimestamp(char* out, size_t outSize) {
    time_t now = time(nullptr);
    struct tm tmUtc;
    gmtime_r(&now, &tmUtc);

    int ms = (int)(millis() % 1000);

    snprintf(
            out,
            outSize,
            "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ",
            tmUtc.tm_year + 1900,
            tmUtc.tm_mon + 1,
            tmUtc.tm_mday,
            tmUtc.tm_hour,
            tmUtc.tm_min,
            tmUtc.tm_sec,
            ms
    );
}

void publishStatus(int i, const char* status) {
    char topic[128];
    buildStatusTopic(topic, sizeof(topic), i + 1);

    bool keyPresent = hasKey(i);
    const char* eventName = (strcmp(status, "closed") == 0) ? "auto_close" : "open";

    char ts[40];
    buildIsoTimestamp(ts, sizeof(ts));

    char payload[256];
    snprintf(
            payload,
            sizeof(payload),
            "{\"state\":\"%s\",\"keyPresent\":%s,\"slotId\":%d,\"deviceUid\":\"%s\",\"event\":\"%s\",\"ts\":\"%s\"}",
            status,
            keyPresent ? "true" : "false",
            i + 1,
            DEVICE_UID,
            eventName,
            ts
    );

    bool ok = mqttClient.publish(topic, payload);

    Serial.print("[MQTT] publish status | topic: ");
    Serial.print(topic);
    Serial.print(" | payload: ");
    Serial.print(payload);
    Serial.print(" | ok: ");
    Serial.println(ok ? "true" : "false");
}

// Attesa senza bloccare del tutto MQTT
void waitWithMqtt(unsigned long ms) {
    unsigned long start = millis();
    while (millis() - start < ms) {
        mqttClient.loop();
        delay(10);
    }
}

// Apertura cassetti
void openDrawer(int i) {
    printLine();
    Serial.print("[DRAWER] Apro cassetto ");
    Serial.println(i + 1);

    setLedBlue(i);

    Serial.print("[SERVO] Drawer ");
    Serial.print(i + 1);
    Serial.print(" -> OPEN (");
    Serial.print(SERVO_OPEN);
    Serial.println(")");
    servos[i].write(SERVO_OPEN);

    publishStatus(i, "open");

    Serial.print("[DRAWER] Attendo ");
    Serial.print(OPEN_DELAY_MS);
    Serial.println(" ms");
    waitWithMqtt(OPEN_DELAY_MS);

    Serial.print("[SERVO] Drawer ");
    Serial.print(i + 1);
    Serial.print(" -> LOCKED (");
    Serial.print(SERVO_LOCKED);
    Serial.println(")");
    servos[i].write(SERVO_LOCKED);

    bool key = hasKey(i);

    Serial.print("[IR] Drawer ");
    Serial.print(i + 1);
    Serial.print(" hasKey = ");
    Serial.println(key ? "true" : "false");

    // ← publishEvent rimossa — lo stato chiave è già incluso nel publishStatus
    publishStatus(i, "closed");

    if (key) {
        setLedGreen(i);
        Serial.print("[LED] Drawer ");
        Serial.print(i + 1);
        Serial.println(" -> GREEN");
    } else {
        setLedRed(i);
        Serial.print("[LED] Drawer ");
        Serial.print(i + 1);
        Serial.println(" -> RED");
    }

    printLine();
}

// Verifica comando
bool isOpenCommand(String msg) {
    msg.trim();
    msg.replace(" ", "");
    msg.replace("\n", "");
    msg.replace("\r", "");
    msg.toLowerCase();

    if (msg == "open") return true;
    if (msg.indexOf("\"action\":\"open\"") >= 0) return true;
    if (msg.indexOf("\"cmd\":\"open\"") >= 0) return true;

    return false;
}

// Callback MQTT
void callback(char* topic, byte* payload, unsigned int length) {
    String msg = "";
    for (unsigned int i = 0; i < length; i++) {
        msg += (char)payload[i];
    }

    printLine();
    Serial.println("[MQTT] Messaggio ricevuto");
    Serial.print("[MQTT] Topic: ");
    Serial.println(topic);
    Serial.print("[MQTT] Payload: ");
    Serial.println(msg);
    printLine();

    char prefix[128];
    buildSlotsPrefix(prefix, sizeof(prefix));

    if (strncmp(topic, prefix, strlen(prefix)) != 0) {
        Serial.println("[MQTT] Topic fuori prefisso device, ignoro");
        return;
    }

    // topic atteso:
    // securelock/devices/{deviceUid}/slots/{slotId}/cmd
    const char* slotStr = topic + strlen(prefix);
    int slotId = atoi(slotStr);

    if (slotId < 1 || slotId > NUM_DRAWERS) {
        Serial.println("[MQTT] Slot non valido, ignoro");
        return;
    }

    if (!isOpenCommand(msg)) {
        Serial.println("[MQTT] Comando non valido, ignoro");
        return;
    }

    Serial.print("[MQTT] Apro slot ");
    Serial.println(slotId);

    openDrawer(slotId - 1);
}

// WiFi
void connectWiFi() {
    Serial.print("[WIFI] Connessione a SSID: ");
    Serial.println(WIFI_SSID);

    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

    unsigned long start = millis();
    while (WiFi.status() != WL_CONNECTED) {
        delay(500);
        Serial.print(".");

        if (millis() - start > 20000) {
            Serial.println();
            Serial.println("[WIFI] Timeout connessione WiFi");
            break;
        }
    }

    Serial.println();

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("[WIFI] Connesso correttamente");
        printWiFiStatus();
    } else {
        Serial.println("[WIFI] Connessione fallita");
    }
}

// Connessione a MQTT Broker
void connectMQTT() {
    Serial.println("[MQTT] Connessione al broker...");

    while (!mqttClient.connected()) {
        Serial.print("[MQTT] Provo connessione con clientId: ");
        Serial.println(MQTT_CLIENT_ID);

        if (mqttClient.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASS)) {
            Serial.println("[MQTT] Connesso al broker!");

            char subTopic[128];
            snprintf(subTopic, sizeof(subTopic), "securelock/devices/%s/slots/+/cmd", DEVICE_UID);

            bool ok = mqttClient.subscribe(subTopic);

            Serial.print("[MQTT] Subscribe topic: ");
            Serial.print(subTopic);
            Serial.print(" | ok: ");
            Serial.println(ok ? "true" : "false");

            printLine();
        } else {
            Serial.print("[MQTT] Connessione fallita, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" -> ritento tra 5 secondi");
            delay(5000);
        }
    }
}

// Setup
void setup() {
    Serial.begin(115200);
    delay(1000);

    Serial.println();
    Serial.println("======================================");
    Serial.println("       AVVIO SECURELOCK ESP32         ");
    Serial.println("======================================");

    wifiClient.setInsecure(); // TLS senza certificati, utile per test

    for (int i = 0; i < NUM_DRAWERS; i++) {
        pinMode(LED_R[i], OUTPUT);
        pinMode(LED_G[i], OUTPUT);
        pinMode(LED_B[i], OUTPUT);
        pinMode(IR_PINS[i], INPUT);

        setLedOff(i);

        servos[i].attach(SERVO_PINS[i]);
        servos[i].write(SERVO_LOCKED);

        setLedRed(i);

        Serial.print("[INIT] Slot ");
        Serial.print(i + 1);
        Serial.println(" inizializzato");
    }

    connectWiFi();

    mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
    mqttClient.setCallback(callback);

    Serial.print("[MQTT] Broker: ");
    Serial.println(MQTT_BROKER);
    Serial.print("[MQTT] Porta: ");
    Serial.println(MQTT_PORT);
    Serial.print("[MQTT] Device UID: ");
    Serial.println(DEVICE_UID);

    connectMQTT();

    Serial.println("[SETUP] Completato");
    printLine();
}

// Loop
void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.println("[WIFI] Disconnesso, riconnessione...");
        connectWiFi();
    }

    if (!mqttClient.connected()) {
        Serial.println("[MQTT] Disconnesso, riconnessione...");
        connectMQTT();
    }

    mqttClient.loop();
}