/// <reference types="cypress" />

import {
  credentials,
  loginResponse,
  sessionFixture,
} from './test-data';

export {};

type LoginOptions = {
  email?: string;
  password?: string;
  admin?: boolean;
  userId?: number;
};

declare global {
  namespace Cypress {
    interface Chainable {
      /** Connexion UI avec API interceptée. */
      login(options?: LoginOptions): Chainable<void>;
    }
  }
}

Cypress.Commands.add('login', (options: LoginOptions = {}) => {
  const {
    admin = true,
    email = admin ? credentials.admin.email : credentials.user.email,
    password = admin ? credentials.admin.password : credentials.user.password,
    userId = admin ? 1 : 2,
  } = options;

  cy.intercept('POST', '**/api/auth/login', {
    statusCode: 200,
    body: {
      ...loginResponse(admin, userId),
      username: email,
    },
  }).as('login');

  cy.intercept('GET', '**/api/session', [sessionFixture]).as('sessions');

  cy.visit('/login');
  cy.get('input[formControlName="email"]').clear().type(email);
  cy.get('input[formControlName="password"]').clear().type(password);
  cy.get('button[type="submit"]').click();

  cy.wait('@login');
  cy.wait('@sessions');
  cy.url().should('include', '/sessions');
});
