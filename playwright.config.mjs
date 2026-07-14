import { defineConfig, devices } from '@playwright/test';

const PORT = 8099;

export default defineConfig({
  testDir: './test/browser',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: 0,
  use: {
    baseURL: `http://localhost:${PORT}`,
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
  // serve resources/public statically; the scittle build lands under js/
  webServer: {
    command: `python3 -m http.server ${PORT} --directory resources/public`,
    url: `http://localhost:${PORT}/test/smoke.html`,
    reuseExistingServer: !process.env.CI,
  },
});
