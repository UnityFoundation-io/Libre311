/** @type {import('@playwright/test').PlaywrightTestConfig} */
import dotenv from 'dotenv';

dotenv.config();
const config = {
	webServer: {
		command: 'npm run start:frontend',
		url: 'http://127.0.0.1:3000',
    reuseExistingServer: true
	},
	testDir: './tests',
	reporter: [ ['html', { open: 'never' }] ],
  use: {
    video: {
      mode: 'retain-on-failure',
      size: { width: 640, height: 480 },
      dir: "frontend/playwright-report"
    }
  }
};

export default config;
