/** @type {import('@playwright/test').PlaywrightTestConfig} */
const config = {
  webServer: {
    command: 'npm run build && npm run preview',
    port: 4173
  },
  testDir: './tests',
  reporter: [ ['html', { open: 'never', outputFolder: './reports' }] ],
  use: {
    video: 'on'
  }
};
 
 export default config;

// use: {
//   video: 'on'
// }