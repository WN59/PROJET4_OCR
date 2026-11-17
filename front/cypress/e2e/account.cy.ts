/// <reference types="cypress" />

import { credentials, userFixture } from '../support/test-data';

describe('Account & Logout (e2e)', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/api/user/1', userFixture).as('userDetail');
    cy.login({ admin: true });
  });

  it('should display user information on Account page', () => {
    cy.contains('.link', 'Account').click();
    cy.wait('@userDetail');

    cy.url().should('include', '/me');
    cy.contains('User information').should('be.visible');
    cy.contains(userFixture.firstName).should('be.visible');
    cy.contains(credentials.admin.email).should('be.visible');
    cy.contains('You are admin').should('be.visible');
  });

  it('should logout and show Login/Register links', () => {
    cy.contains('.link', 'Logout').click();

    cy.url().should('include', '/login');
    cy.get('a.link').contains('Login').should('be.visible');
    cy.get('a.link').contains('Register').should('be.visible');
    cy.contains('.link', 'Logout').should('not.exist');
  });
});
