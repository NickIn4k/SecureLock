#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ESP32Servo.h>
#include "secrets.example.h" // Credenziali e dati (come .env in Node/Kt)

// Numero cassetti
const int NUM_DRAWERS = 3;

// ############ PIN ESP ############
// SERVO
const int SERVO_PINS[NUM_DRAWERS] = {18, 19, 21};

// LED RGB
const int LED_R[NUM_DRAWERS] = {0, 17, 23};
const int LED_G[NUM_DRAWERS] = {2, 16, 22};
const int LED_B[NUM_DRAWERS] = {15, 4, 5};

// IR
const int IR_PINS[NUM_DRAWERS] = {34, 35, 32};

// Servo 
const int SERVO_LOCKED = 0; // GUARDA 
const int SERVO_OPEN   = 90;
const int OPEN_DELAY_MS = 5000;

// Oggetti
WiFiClientSecure wifiClient;  // Connessioni wireless sicure
PubSubClient mqttClient(wifiClient); // Gestione MQTT (client: publisher / subscriber)
Servo servos[NUM_DRAWERS];  // Gestore sevo (Non nativa in ESP)

// ############ FUNZIONI ############

// LED RGB
/* funzionano al "contrario"
 * - HIGH --> spento
 * - LOW --> acceso
*/
void setLedRed(int i) {
  digitalWrite(LED_R[i], LOW);
  digitalWrite(LED_G[i], HIGH);
  digitalWrite(LED_B[i], HIGH);
}

void setLedGreen(int i) {
  digitalWrite(LED_R[i], HIGH);
  digitalWrite(LED_G[i], LOW);
  digitalWrite(LED_B[i], HIGH);
}

void setLedBlue(int i) {
  digitalWrite(LED_R[i], HIGH);
  digitalWrite(LED_G[i], HIGH);
  digitalWrite(LED_B[i], LOW);
}

// IR
/* Funzionamento:
 * - segnale basso (LOW): chiave presente
 * - segnale alto (HIGH): nessuna chiave 
*/
bool hasKey(int i) {
  return digitalRead(IR_PINS[i]) == LOW;
}

// MQTT
// Crea topic dinamico
void publishStatus(int i, const char* status) {
  char topic[80];
  // Scrivi dentro topic la stringa
  sprintf(topic, "securelock/%s/drawer/%d/status", BUILDING_ID, i + 1);
  mqttClient.publish(topic, status);
}

void publishHasKey(int i, bool val) {
  char topic[80];
  // Scrivi dentro topic la stringa
  sprintf(topic, "securelock/%s/drawer/%d/hasKey", BUILDING_ID, i + 1);
  mqttClient.publish(topic, val ? "true" : "false");
}

// Apertura cassetti
void openDrawer(int i) {
  setLedBlue(i);

  servos[i].write(SERVO_OPEN);
  publishStatus(i, "open");

  setLedGreen(i);
  delay(OPEN_DELAY_MS);

  servos[i].write(SERVO_LOCKED);

  bool key = hasKey(i);
  publishHasKey(i, key);
  publishStatus(i, "closed");

  if (key) 
    setLedGreen(i);
  else 
    setLedRed(i);
}

// Callback MQTT
/* Funzionamento:
 * Chiamata automaticamente ad ogni MQTT publish
 * Payload in byte (come MQTT)
*/
void callback(char* topic, byte* payload, unsigned int length) {
  // Conversione Byte array --> String
  String msg = "";
  for (int i = 0; i < length; i++) 
    msg += (char)payload[i];

  /* Per ogni cassetto:
   * - costruisci il topic che ti aspetti
   * - confronta con quello arrivato (strcmp() == 0, stringhe identiche)
  */ 
  for (int i = 0; i < NUM_DRAWERS; i++) {
    char expected[80];
    sprintf(expected, "securelock/%s/drawer/%d/open", BUILDING_ID, i + 1);

    if (strcmp(topic, expected) == 0 && msg == "true") {
      openDrawer(i);
    }
  }
}

// WiFi
void connectWiFi() {
  // Connettiti alla rete con SSID e pwd
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  // Riprova finchè non sei connesso
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }
}

// Connessione a MQTT Broker
void connectMQTT() {
  while (!mqttClient.connected()) {
    // Tenta la connessione
    if (mqttClient.connect(MQTT_CLIENT_ID, MQTT_USER, MQTT_PASS)) {
      // In caso, subscribe di tutti i cassetti
      for (int i = 0; i < NUM_DRAWERS; i++) {
        char topic[80];
        sprintf(topic, "securelock/%s/drawer/%d/open", BUILDING_ID, i + 1);
        mqttClient.subscribe(topic);
      }
    } else {
      delay(5000);
    }
  }
}

// Setup
void setup() {
  Serial.begin(115200);

  wifiClient.setInsecure(); // TLS senza certificati (HotSpot)

  // modalità pin e servomotori
  for (int i = 0; i < NUM_DRAWERS; i++) {
    pinMode(LED_R[i], OUTPUT);
    pinMode(LED_G[i], OUTPUT);
    pinMode(LED_B[i], OUTPUT);
    pinMode(IR_PINS[i], INPUT);

    servos[i].attach(SERVO_PINS[i]);
    servos[i].write(SERVO_LOCKED);
    setLedRed(i);
  }

  // Connessioni: WiFi, MQTT
  connectWiFi();
  // Preparazione server/callback MQTT
  mqttClient.setServer(MQTT_BROKER, MQTT_PORT);
  mqttClient.setCallback(callback);
  connectMQTT();
}

// Loop
void loop() {
  // ESP deve SEMPRE essere connesso
  if (WiFi.status() != WL_CONNECTED) 
    connectWiFi();
  
  if (!mqttClient.connected()) 
    connectMQTT();
  
  /* Gestione mqtt interna
   * Ricezione messaggi
   * Connessione
   * Rete e sincronizzazione
  */
  mqttClient.loop();
}