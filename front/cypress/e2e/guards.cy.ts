/// <reference types="cypress" />

/**
 * Navigation SPA sans reload : SessionService est en mémoire,
 * un cy.visit() plein page perdrait la session.
 */
const navigateSpa = (path: string) => {
  cy.window().then((win) => {
    win.history.pushState({}, '', path);
    win.dispatchEvent(new PopStateEvent('popstate'));
  });
};

describe('Guards (e2e)', () => {
  it('should redirect unauthenticated user from /sessions to /login (authGuard)', () => {
    cy.visit('/sessions');
    cy.url().should('include', '/login');
  });

  it('should redirect authenticated user from /login to /sessions (unauthGuard)', () => {
    cy.login({ admin: true });
    navigateSpa('/login');
    cy.url().should('include', '/sessions');
  });
});
