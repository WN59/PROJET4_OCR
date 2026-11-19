/// <reference types="cypress" />

import {
  credentials,
  regularUserFixture,
  userFixture,
} from '../support/test-data';

describe('Account & Logout (e2e)', () => {
  describe('as admin', () => {
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
      // Admin : pas de bouton Delete
      cy.contains('button', 'Delete').should('not.exist');
    });

    it('should logout and show Login/Register links', () => {
      cy.contains('.link', 'Logout').click();

      cy.url().should('include', '/login');
      cy.get('a.link').contains('Login').should('be.visible');
      cy.get('a.link').contains('Register').should('be.visible');
      cy.contains('.link', 'Logout').should('not.exist');
    });
  });

  describe('as regular user', () => {
    beforeEach(() => {
      cy.intercept('GET', '**/api/user/2', regularUserFixture).as('userDetail');
      cy.intercept('DELETE', '**/api/user/2', { statusCode: 200, body: null }).as(
        'deleteAccount'
      );
      cy.login({ admin: false });
    });

    it('should delete account, show snackbar and redirect to login', () => {
      cy.contains('.link', 'Account').click();
      cy.wait('@userDetail');

      cy.contains('Delete my account:').should('be.visible');
      cy.contains('button', 'Delete').click();
      cy.wait('@deleteAccount');

      // MatSnackBar (même pattern que session-detail)
      cy.contains('Your account has been deleted !').should('be.visible');

      // logOut + navigate(['/']) → redirect login
      cy.url().should('include', '/login');
      cy.get('a.link').contains('Login').should('be.visible');
    });
  });
});
