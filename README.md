# Yoga App — Projet OCR P4 (Full-Stack Testing)

Application de gestion de sessions de yoga :
- **Front** : Angular 19 + Jest (unitaires/intégration) + Cypress (e2e)
- **Back** : Spring Boot + JUnit / Mockito

---

## Prérequis

| Outil | Usage |
|---|---|
| Node.js 18+ / npm | Front Angular, Jest, Cypress |
| JDK 21 | Backend Java |
| Maven 3.9+ | Build / run Spring Boot |
| Docker Desktop | Base MySQL du back |

---

## Installation

```bash
git clone <url-de-votre-repo>
cd PROJET4_OCR
```

### Front

```bash
cd front
npm install
```

### Back

```bash
cd back
# Docker Desktop doit être démarré
mvn spring-boot:run
```

Au premier lancement, Maven démarre le container MySQL via Docker Compose.

Compte seed (back réel) :
- email : `yoga@studio.com`
- password : `test!1234`

---

## Lancer l’application

### 1. Backend (terminal 1)

```bash
cd back
mvn spring-boot:run
```

→ API sur `http://localhost:8080`

### 2. Frontend (terminal 2)

```bash
cd front
npm start
```

→ App sur `http://localhost:4200`  
(`proxy.conf.json` redirige `/api` vers le back `:8080`)

---

## Tests Front

### Tests unitaires & d’intégration (Jest)

```bash
cd front
npm run test
```

Avec couverture (seuil ≥ 80 % statements / branches / lines / functions) :

```bash
npm run test -- --coverage
```

Rapport HTML :
`front/coverage/jest/lcov-report/index.html`

Mode watch :

```bash
npm run test:watch
```

### Tests end-to-end (Cypress)

Les e2e utilisent `cy.intercept()` : le **back n’est pas obligatoire** pour les lancer.

1. Démarrer le front :
```bash
cd front
npm start
```

2. Ouvrir Cypress (autre terminal) :
```bash
cd front
npm run cypress:open
```

Puis : **E2E Testing** → navigateur → choisir un fichier `*.cy.ts`.

Lancer tous les e2e en headless :

```bash
npm run cypress:run
```

Via Angular (serveur de couverture) :

```bash
npm run e2e
# ou CI
npm run e2e:ci
npm run e2e:coverage
```

Rapport couverture e2e (après run coverage) :
`front/coverage/lcov-report/index.html`

#### Fichiers e2e (plan de testing)

| Fichier | Features couvertes |
|---|---|
| `cypress/e2e/login.cy.ts` | Login OK / KO |
| `cypress/e2e/register.cy.ts` | Register OK / KO / champs requis |
| `cypress/e2e/sessions.cy.ts` | Liste + boutons admin |
| `cypress/e2e/session-detail.cy.ts` | Infos session, Delete admin, Participate |
| `cypress/e2e/session-form.cy.ts` | Création / modification / champs requis |
| `cypress/e2e/account.cy.ts` | Account + Logout |

---

## Tests Back (Java)

```bash
cd back
mvn test
```

(Ne pas tester le package DTO — exigence OCR.)

Rapports de couverture Maven (JaCoCo, selon config du projet) dans `back/target/site/jacoco` après les tests.

---

## Structure du repo

```text
PROJET4_OCR/
├── front/          # Angular + Jest + Cypress
│   ├── src/
│   ├── cypress/
│   └── README.md
├── back/           # Spring Boot + JUnit/Mockito
│   └── README.md
└── README.md       # ce fichier
```

---

## Récap commandes utiles

```bash
# Front
cd front && npm install
cd front && npm start
cd front && npm run test -- --coverage
cd front && npm run cypress:open

# Back
cd back && mvn spring-boot:run
cd back && mvn test
```
