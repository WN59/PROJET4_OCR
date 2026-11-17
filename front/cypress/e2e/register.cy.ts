/// <reference types="cypress" />

describe('Register (e2e)', () => {
  beforeEach(() => {
    cy.visit('/register');
  });

  it('should keep submit disabled when required fields are missing', () => {
    cy.get('button[type="submit"]').should('be.disabled');
  });

  it('should register successfully and redirect to login', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 200,
      body: { message: 'User registered successfully!' },
    }).as('register');

    cy.get('input[formControlName="firstName"]').type('Yoga');
    cy.get('input[formControlName="lastName"]').type('Studio');
    cy.get('input[formControlName="email"]').type('new.user@studio.com');
    cy.get('input[formControlName="password"]').type('test!1234');
    cy.get('button[type="submit"]').should('not.be.disabled').click();

    cy.wait('@register');
    cy.url().should('include', '/login');
    cy.get('mat-card-title').should('contain', 'Login');
  });

  it('should display an error when register fails', () => {
    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 400,
      body: { message: 'Error: Email is already taken!' },
    }).as('registerError');

    cy.get('input[formControlName="firstName"]').type('Yoga');
    cy.get('input[formControlName="lastName"]').type('Studio');
    cy.get('input[formControlName="email"]').type('yoga@studio.com');
    cy.get('input[formControlName="password"]').type('test!1234');
    cy.get('button[type="submit"]').click();

    cy.wait('@registerError');
    cy.url().should('include', '/register');
    cy.get('.error').should('be.visible').and('contain', 'An error occurred');
  });
});
