/// <reference types="cypress" />

import { sessionFixture, teacherFixture } from '../support/test-data';

describe('Session detail (e2e)', () => {
  beforeEach(() => {
    cy.intercept('GET', '**/api/session/1', sessionFixture).as('sessionDetail');
    cy.intercept('GET', '**/api/teacher/1', teacherFixture).as('teacherDetail');
  });

  it('should display session information and Delete for admin', () => {
    cy.login({ admin: true });

    cy.contains('button', 'Detail').click();
    cy.wait(['@sessionDetail', '@teacherDetail']);

    cy.url().should('include', '/sessions/detail/1');
    cy.contains('Yoga Débutant').should('be.visible');
    cy.contains(sessionFixture.description).should('be.visible');
    cy.contains(teacherFixture.firstName).should('be.visible');
    cy.contains(teacherFixture.lastName).should('be.visible');
    cy.contains('button', 'Delete').should('be.visible');
  });

  it('should show Participate instead of Delete for non-admin', () => {
    cy.login({ admin: false });

    cy.contains('button', 'Detail').click();
    cy.wait(['@sessionDetail', '@teacherDetail']);

    cy.contains('button', 'Delete').should('not.exist');
    cy.contains('button', 'Participate').should('be.visible');
  });

  it('should delete a session as admin', () => {
    cy.intercept('DELETE', '**/api/session/1', { statusCode: 200 }).as('deleteSession');
    cy.intercept('GET', '**/api/session', []).as('sessionsAfterDelete');

    cy.login({ admin: true });
    cy.contains('button', 'Detail').click();
    cy.wait('@sessionDetail');

    cy.contains('button', 'Delete').click();
    cy.wait('@deleteSession');

    cy.url().should('include', '/sessions');
    cy.contains('Session deleted !').should('be.visible');
  });

  it('should participate then unParticipate as regular user', () => {
    const userId = 2;
    let participants: number[] = [];

    cy.intercept('GET', '**/api/session/1', (req) => {
      req.reply({ ...sessionFixture, users: participants });
    }).as('sessionDetail');

    cy.intercept('POST', `**/api/session/1/participate/${userId}`, (req) => {
      participants = [userId];
      req.reply({ statusCode: 200, body: null });
    }).as('participate');

    cy.intercept('DELETE', `**/api/session/1/participate/${userId}`, (req) => {
      participants = [];
      req.reply({ statusCode: 200, body: null });
    }).as('unParticipate');

    cy.login({ admin: false, userId });
    cy.contains('button', 'Detail').click();
    cy.wait(['@sessionDetail', '@teacherDetail']);

    cy.contains('button', 'Participate').click();
    cy.wait('@participate');
    cy.wait('@sessionDetail'); // fetchSession() après participate
    cy.contains('button', 'Do not participate').should('be.visible').click();
    cy.wait('@unParticipate');
    cy.wait('@sessionDetail'); // fetchSession() après unParticipate
    cy.contains('button', 'Participate').should('be.visible');
  });
});
