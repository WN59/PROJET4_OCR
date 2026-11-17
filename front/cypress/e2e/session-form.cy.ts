/// <reference types="cypress" />

import { sessionFixture, teacherFixture } from '../support/test-data';

describe('Session form (e2e)', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/api/teacher', [teacherFixture]).as('teachers');
    cy.login({ admin: true });
  });

  it('should keep Save disabled when required fields are missing', () => {
    cy.contains('button', 'Create').click();
    cy.url().should('include', '/sessions/create');
    cy.contains('Create session').should('be.visible');
    cy.get('button[type="submit"]').should('be.disabled');
  });

  it('should create a session successfully', () => {
    cy.intercept('POST', '**/api/session', {
      statusCode: 200,
      body: { ...sessionFixture, name: 'Yoga avancé' },
    }).as('createSession');

    cy.intercept('GET', '**/api/session', [
      { ...sessionFixture, name: 'Yoga avancé' },
    ]).as('sessionsAfterCreate');

    cy.contains('button', 'Create').click();
    cy.wait('@teachers');

    cy.get('input[formControlName="name"]').type('Yoga avancé');
    cy.get('input[formControlName="date"]').type('2026-02-20');
    cy.get('mat-select[formControlName="teacher_id"]').click();
    cy.get('mat-option').contains(teacherFixture.firstName).click();
    cy.get('textarea[formControlName="description"]').type('Séance avancée');

    cy.get('button[type="submit"]').should('not.be.disabled').click();
    cy.wait('@createSession');

    cy.url().should('include', '/sessions');
    cy.contains('Session created !').should('be.visible');
  });

  it('should update a session successfully', () => {
    cy.intercept('GET', '**/api/session/1', sessionFixture).as('sessionDetail');
    cy.intercept('PUT', '**/api/session/1', {
      statusCode: 200,
      body: { ...sessionFixture, name: 'Yoga modifié' },
    }).as('updateSession');
    cy.intercept('GET', '**/api/session', [
      { ...sessionFixture, name: 'Yoga modifié' },
    ]).as('sessionsAfterUpdate');

    cy.contains('button', 'Edit').click();
    cy.wait(['@sessionDetail', '@teachers']);

    cy.url().should('include', '/sessions/update/1');
    cy.contains('Update session').should('be.visible');
    cy.get('input[formControlName="name"]').should('have.value', sessionFixture.name);

    cy.get('input[formControlName="name"]').clear().type('Yoga modifié');
    cy.get('button[type="submit"]').should('not.be.disabled').click();
    cy.wait('@updateSession');

    cy.url().should('include', '/sessions');
    cy.contains('Session updated !').should('be.visible');
  });
});
