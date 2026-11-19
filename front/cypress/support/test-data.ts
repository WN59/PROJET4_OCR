/**
 * Données de test partagées pour les e2e.
 * Source unique (remplace cypress/fixtures/*.json + cy.fixture).
 * Import synchrone, typé, réutilisable dans les specs et cy.login.
 */

export const credentials = {
  admin: {
    email: 'yoga@studio.com',
    password: 'test!1234',
  },
  user: {
    email: 'user@studio.com',
    password: 'test!1234',
  },
} as const;

export const sessionFixture = {
  id: 1,
  name: 'Yoga débutant',
  description: 'Séance pour débutants',
  date: '2026-01-15T10:00:00.000+00:00',
  teacher_id: 1,
  users: [] as number[],
  createdAt: '2026-01-01T10:00:00.000+00:00',
  updatedAt: '2026-01-02T10:00:00.000+00:00',
};

export const teacherFixture = {
  id: 1,
  firstName: 'Margot',
  lastName: 'DELAHAYE',
  createdAt: '2026-01-01T10:00:00.000+00:00',
  updatedAt: '2026-01-01T10:00:00.000+00:00',
};

export const userFixture = {
  id: 1,
  email: credentials.admin.email,
  firstName: 'Admin',
  lastName: 'Admin',
  admin: true,
  createdAt: '2026-01-01T10:00:00.000+00:00',
  updatedAt: '2026-01-02T10:00:00.000+00:00',
};

/** Compte non-admin (le bouton Delete n’apparaît que si admin === false). */
export const regularUserFixture = {
  id: 2,
  email: credentials.user.email,
  firstName: 'User',
  lastName: 'Yoga',
  admin: false,
  createdAt: '2026-01-01T10:00:00.000+00:00',
  updatedAt: '2026-01-02T10:00:00.000+00:00',
};

export const loginResponse = (admin = true, id = 1) => ({
  token: 'fake-jwt-token',
  type: 'Bearer',
  id,
  username: admin ? credentials.admin.email : credentials.user.email,
  firstName: admin ? 'Admin' : 'User',
  lastName: admin ? 'Admin' : 'Yoga',
  admin,
});
