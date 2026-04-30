# DonorConnect — Blood Bank Microservices Architecture

A Spring Boot + Kafka + Eureka microservices system for blood bank inventory and donor management.

---

## Architecture Overview

```
                        ┌─────────────────────────┐
                        │   API Gateway (:8080)    │
                        │  Spring Cloud Gateway    │
                        └────────────┬────────────┘
                                     │ routes via Eureka
          ┌──────────────────────────┼──────────────────────────┐
          │                          │                          │
  ┌───────▼──────┐          ┌────────▼───────┐        ┌────────▼──────────┐
  │ Auth :8001   │          │ Donor :8002    │        │ BloodSupply :8003  │
  │ User,AuditLog│          │ Donor,Screening│        │ Donation,Component │
  └──────────────┘          │ Deferral,Appt  │        │ Inventory,Recall   │
                            └───────┬────────┘        └──────┬─────────────┘
                                    │ Kafka                   │ Kafka
                                    ▼                         ▼
  ┌──────────────┐          ┌───────────────┐        ┌───────────────────┐
  │Safety :8005  │◄─────────│  Kafka Bus    │────────►Transfusion :8004  │
  │Reaction,     │          │               │        │ Crossmatch,Issue  │
  │LookbackTrace │          └───────┬───────┘        └───────────────────┘
  └──────────────┘                  │
          ┌───────────────┬─────────┴──────┬──────────────┐
          ▼               ▼                ▼              ▼
  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
  │Billing :8006 │ │Reporting:8007│ │Notif. :8008  │ │Config :8009  │
  │  BillingRef  │ │ LabReportPack│ │ Notification │ │ SystemConfig │
  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘
          │                                 ▲
          └─────────────────────────────────┘
                   Eureka Server (:8761)
```

---

## Kafka Topics

| Topic | Producer | Consumers | Trigger |
|-------|----------|-----------|---------|
| `blood.test.reactive` | blood-supply-service | safety-service, blood-supply-service (quarantine), notification-service, reporting-service | TestResult marked REACTIVE |
| `transfusion.component.issued` | transfusion-service | blood-supply-service (inventory decrement), billing-service (create BillingRef), notification-service, reporting-service | Component issued to patient |
| `donor.flagged` | donor-service | notification-service | Donor marked ineligible |
| `safety.alert` | safety-service | notification-service, reporting-service | Adverse reaction logged |

---

## Scheduled Jobs (Cron)

| Service | Job Class | Schedule | Action |
|---------|-----------|----------|--------|
| blood-supply-service | `ComponentExpiryScheduler` | Every hour | AVAILABLE → EXPIRED when ExpiryDate passed |
| donor-service | `DeferralScheduler` | Daily midnight | ACTIVE deferral → EXPIRED; Donor → ACTIVE when temporary deferral ends |
| reporting-service | `ReportingScheduler` | Daily 1 AM | Generate daily snapshot LabReportPack |

---

## Feign Clients (Synchronous)

| Service | Client | Calls | Purpose |
|---------|--------|-------|---------|
| transfusion-service | `BloodSupplyFeignClient` | `GET /blood/components/{id}` | Verify component is AVAILABLE before confirming crossmatch |
| transfusion-service | `BloodSupplyFeignClient` | `GET /blood/components/available` | List available stock for crossmatch selection |

---

## Services & Ports

| Service | Port | Database |
|---------|------|----------|
| eureka-server | 8761 | — |
| api-gateway | 8080 | — |
| auth-service | 8001 | auth_db |
| donor-service | 8002 | donor_db |
| blood-supply-service | 8003 | blood_db |
| transfusion-service | 8004 | transfusion_db |
| safety-service | 8005 | safety_db |
| billing-service | 8006 | billing_db |
| reporting-service | 8007 | reporting_db |
| notification-service | 8008 | notification_db |
| config-service | 8009 | config_db |
| kafka-ui | 8090 | — |

---

## Quick Start

### Option A: Docker Compose (recommended)

```bash
# 1. Build all services
docker-compose build

# 2. Start infrastructure first, then services
docker-compose up -d mysql zookeeper kafka
sleep 30
docker-compose up -d eureka-server
sleep 15
docker-compose up -d

# 3. Verify
open http://localhost:8761         # Eureka dashboard
open http://localhost:8090         # Kafka UI
curl http://localhost:8080/actuator/gateway/routes  # Gateway routes
```

### Option B: Local (IntelliJ / Maven)

**Prerequisites:** Java 17+, MySQL 8+, Apache Kafka 3+

```bash
# 1. Start MySQL and Kafka locally

# 2. Create databases
mysql -u root -ppassword < scripts/init-databases.sql

# 3. Start services IN ORDER:
# Terminal 1:
cd eureka-server && mvn spring-boot:run

# Terminal 2:
cd api-gateway && mvn spring-boot:run

# Terminals 3-11 (any order after Eureka):
cd auth-service && mvn spring-boot:run
cd config-service && mvn spring-boot:run
cd donor-service && mvn spring-boot:run
cd blood-supply-service && mvn spring-boot:run
cd transfusion-service && mvn spring-boot:run
cd safety-service && mvn spring-boot:run
cd billing-service && mvn spring-boot:run
cd reporting-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
```

---

## Sample API Flows

### 1. Register a Donor
```http
POST http://localhost:8080/api/donors/donors
Content-Type: application/json

{
  "name": "John Doe",
  "dob": "1990-05-15",
  "gender": "Male",
  "bloodGroup": "O",
  "rhFactor": "POSITIVE",
  "donorType": "VOLUNTARY",
  "contactInfo": "9876543210"
}
```

### 2. Record a Reactive Test Result (triggers Kafka cascade)
```http
POST http://localhost:8080/api/blood/blood/test-results
Content-Type: application/json

{
  "donationId": 1,
  "testType": "HIV",
  "result": "REACTIVE",
  "status": "REACTIVE",
  "enteredBy": 1
}
```
*→ Kafka fires `blood.test.reactive`*
*→ blood-supply-service quarantines all components from donation 1*
*→ safety-service creates a LookbackTrace*
*→ notification-service creates an alert for admin*

### 3. Issue a Component (triggers billing + inventory decrement)
```http
POST http://localhost:8080/api/transfusion/transfusion/issues
Content-Type: application/json

{
  "componentId": 1,
  "patientId": 42,
  "issuedBy": 5,
  "indication": "Surgical replacement"
}
```
*→ Kafka fires `transfusion.component.issued`*
*→ blood-supply-service decrements InventoryBalance*
*→ billing-service auto-creates BillingRef*

---

## Configuration

All services read DB and Kafka config from `application.yml`. To change defaults:

| Property | Default |
|----------|---------|
| MySQL host | localhost:3306 |
| MySQL root password | password |
| Kafka bootstrap | localhost:9092 |
| Eureka URL | http://localhost:8761/eureka/ |

Override at runtime with environment variables:
```bash
SPRING_DATASOURCE_PASSWORD=mypass mvn spring-boot:run
```

---

## Technology Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Cloud 2023.0** (Eureka, Gateway, OpenFeign)
- **Apache Kafka** (via Spring Kafka)
- **MySQL 8** (separate schema per service)
- **Lombok** (boilerplate reduction)
- **Docker Compose** (full stack orchestration)

--currently in building phase