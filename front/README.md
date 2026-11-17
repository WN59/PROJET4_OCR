# Yoga App — Frontend (Angular)

Front de l’application Yoga App (Angular 19).

## Prérequis

- Node.js 18+
- npm

## Installation

```bash
cd front
npm install
```

## Lancer le front

```bash
npm start
```

Application : [http://localhost:4200](http://localhost:4200)

> Le proxy `proxy.conf.json` redirige `/api` vers le backend `http://localhost:8080`.  
> Pour un usage complet avec données réelles, démarrez aussi le back (voir README racine / `back/README.md`).

Compte seed (si back démarré) :
- email : `yoga@studio.com`
- password : `test!1234`

---

## Tests

### Jest — unitaires & intégration

```bash
npm run test
npm run test -- --coverage
npm run test:watch
```

Rapport de couverture :
`coverage/jest/lcov-report/index.html`

Seuil configuré dans `jest.config.js` : **80 %** minimum (statements, branches, lines, functions).

### Cypress — e2e

Avec le front déjà lancé (`npm start`) :

```bash
npm run cypress:open
```

Headless :

```bash
npm run cypress:run
```

Via Angular CLI (coverage) :

```bash
npm run e2e
npm run e2e:ci
npm run e2e:coverage
```

Scénarios couverts (plan de testing) :
- Login / Register
- Liste des sessions (admin / non-admin)
- Détail + suppression session
- Création / modification session
- Account + Logout

Les e2e mockent l’API avec `cy.intercept()` (backend optionnel).  
Données partagées : `cypress/support/test-data.ts` (+ commande `cy.login`).

---

## Scripts npm

| Script | Description |
|---|---|
| `npm start` | Lance `ng serve` |
| `npm run test` | Tests Jest |
| `npm run test:watch` | Jest en mode watch |
| `npm run cypress:open` | UI Cypress |
| `npm run cypress:run` | Cypress headless |
| `npm run e2e` | E2E via Angular + coverage server |
| `npm run e2e:ci` | E2E CI headless |
| `npm run e2e:coverage` | Rapport couverture e2e |
