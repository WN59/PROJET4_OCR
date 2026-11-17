/// <reference types="cypress" />

import { credentials, loginResponse } from '../support/test-data';

describe('Login (e2e)', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('should login successfully and redirect to sessions', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: loginResponse(true),
    }).as('login');

    cy.intercept('GET', '**/api/session', []).as('sessions');

    cy.get('input[formControlName="email"]').type(credentials.admin.email);
    cy.get('input[formControlName="password"]').type(credentials.admin.password);
    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.url().should('include', '/sessions');
    cy.contains('Logout').should('be.visible');
  });

  it('should display an error when login fails', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 401,
      body: { message: 'Unauthorized' },
    }).as('loginError');

    cy.get('input[formControlName="email"]').type('wrong@studio.com');
    cy.get('input[formControlName="password"]').type('bad-password');
    cy.get('button[type="submit"]').click();

    cy.wait('@loginError');
    cy.url().should('include', '/login');
    cy.get('.error').should('be.visible').and('contain', 'An error occurred');
  });

  it('should keep submit disabled when required fields are missing', () => {
    cy.get('button[type="submit"]').should('be.disabled');
  });
});
