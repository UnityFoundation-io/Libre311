/** @type {import('@playwright/test').PlaywrightTestConfig} */
const config = {
	webServer: {
		command: 'npm run start:frontend',
		url: 'http://127.0.0.1:3000'
	},
	testDir: './tests',
	reporter: [ ['html', { open: 'never' }] ]
};

export default config;

