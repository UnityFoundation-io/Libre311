import { test } from '@playwright/test';

test('report new issue', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
  
  // Enter issue location
  await page.getByPlaceholder('Enter the address').click();
  await page.getByPlaceholder('Enter the address').fill('12140 Woodcrest Executive Drive');
  await page.getByPlaceholder('Enter the address').press('Enter');
  await page.getByText('12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA').click();
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter issue details
  await page.getByRole('combobox').selectOption('001');
  await page.getByRole('combobox').nth(1).selectOption('CRACKED');
  await page.getByRole('combobox').nth(1).selectOption('NARROW');
  await page.getByPlaceholder('Additional Description Details').click();
  await page.getByPlaceholder('Additional Description Details').fill('Additional description details');
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Upload picture (skipped because of photo upload bug; actual test to follow)
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter contact information
  await page.getByPlaceholder('ex: John Doe').click();
  await page.getByPlaceholder('ex: John Doe').fill('John Doe');
  await page.getByPlaceholder('ex: johndoe@gmail.com').click();
  await page.getByPlaceholder('ex: johndoe@gmail.com').fill('johndoe@gmail.com');
  await page.getByRole('button', { name: 'Review & Submit submit issue' }).click();

  // Review information
  await page.getByText('Issue Location: 12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA').click();
  await page.getByText('Issue Type: Sidewalk').click();
  await page.getByText('Issue Details: 1-Cracked2-Too narrow').click();
  await page.getByText('Name of Submitter: John Doe').click();
  await page.getByText('Contact Info: johndoe@gmail.com').click();
  await page.getByRole('button', { name: 'Submit submit issue' }).click();
});

/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
// });