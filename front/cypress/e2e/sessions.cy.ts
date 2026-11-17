/// <reference types="cypress" />

import { sessionFixture } from '../support/test-data';

describe('Sessions list (e2e)', () => {
  it('should display sessions and admin actions when user is admin', () => {
    cy.login({ admin: true });

    cy.contains('Sessions available').should('be.visible');
    cy.contains(sessionFixture.name).should('be.visible');
    cy.contains(sessionFixture.description).should('be.visible');
    cy.contains('button', 'Create').should('be.visible');
    cy.contains('button', 'Detail').should('be.visible');
    cy.contains('button', 'Edit').should('be.visible');
  });

  it('should hide Create/Edit when user is not admin', () => {
    cy.login({ admin: false });

    cy.contains(sessionFixture.name).should('be.visible');
    cy.contains('button', 'Detail').should('be.visible');
    cy.contains('button', 'Create').should('not.exist');
    cy.contains('button', 'Edit').should('not.exist');
  });
});
