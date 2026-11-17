import { defineConfig } from 'cypress';

export default defineConfig({
  videosFolder: 'cypress/videos',
  screenshotsFolder: 'cypress/screenshots',
  // Données e2e centralisées dans support/test-data.ts (pas de cy.fixture)
  fixturesFolder: false,
  video: false,
  screenshotOnRunFailure: true,
  e2e: {
    baseUrl: 'http://localhost:4200',
    supportFile: 'cypress/support/e2e.ts',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    setupNodeEvents(on, config) {
      // Package encore en CommonJS
      // eslint-disable-next-line @typescript-eslint/no-var-requires
      const registerCodeCoverageTasks = require('@cypress/code-coverage/task');
      return registerCodeCoverageTasks(on, config);
    },
  },
});
