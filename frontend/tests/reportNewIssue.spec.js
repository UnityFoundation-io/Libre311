import { expect, test } from '@playwright/test';

test('report new issue with image', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
  
  // Enter issue location
  await page.getByPlaceholder('Enter the address').click();
  await page.getByPlaceholder('Enter the address').fill('12140 Woodcrest Executive Drive');
  await page.getByPlaceholder('Enter the address').press('Enter');
  await expect(page.getByText('12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA')).toBeVisible();
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter issue details
  await expect(page.getByRole('combobox')).toBeVisible();
  await page.getByRole('combobox').selectOption('001');
  await page.getByRole('combobox').nth(1).selectOption('CRACKED');
  await page.getByRole('combobox').nth(1).selectOption('NARROW');
  await page.getByPlaceholder('Additional Description Details').click();
  await page.getByPlaceholder('Additional Description Details').fill('Additional description details');
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Upload picture
  // ~ These tests are commented out because the CI environment currently does not support
  // ~ authentication with GCP storage

  // const fileChooserPromise = page.waitForEvent('filechooser');
  // await page.getByRole('img', { name: 'take photo' }).click();
  // const fileChooser = await fileChooserPromise;
  // await fileChooser.setFiles('./tests/fixtures/logo.png');
  // await page.getByRole('button', { name: 'Upload' }).click();
  // await expect(page.getByText('The uploaded image has been saved.')).toBeVisible();
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter contact information
  await page.getByPlaceholder('ex: John Doe').click();
  await page.getByPlaceholder('ex: John Doe').fill('John Doe');
  await page.getByPlaceholder('ex: johndoe@gmail.com').click();
  await page.getByPlaceholder('ex: johndoe@gmail.com').fill('johndoe@gmail.com');
  await page.getByRole('button', { name: 'Review & Submit submit issue' }).click();

  // Review information
  await expect(page.getByText('Issue Location: 12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA')).toBeVisible();
  await expect(page.getByText('Issue Type: Sidewalk')).toBeVisible();
  await expect(page.getByText('Issue Details: 1-Cracked2-Too narrow')).toBeVisible();
  // await expect(page.getByRole('img', { name: 'uploaded image' })).toBeVisible();
  await expect(page.getByText('Description: Additional description details')).toBeVisible();
  await expect(page.getByText('Name of Submitter: John Doe')).toBeVisible();
  await expect(page.getByText('Contact Info: johndoe@gmail.com')).toBeVisible();
  await page.getByRole('button', { name: 'Submit submit issue' }).click();

  // Confirms the submission went through
  await expect(page.getByText('Thank You! The issue has been reported.')).toBeVisible();
});

test('report new issue with other issue type', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();

  // Enter issue location
  await page.getByPlaceholder('Enter the address').click();
  await page.getByPlaceholder('Enter the address').fill('12140 Woodcrest Executive Drive');
  await page.getByPlaceholder('Enter the address').press('Enter');
  await expect(page.getByText('12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA')).toBeVisible();
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Issue type "other" selected
  await expect(page.getByRole('combobox')).toBeVisible();
  await page.getByRole('combobox').selectOption({ index: 6 });
  await page.getByPlaceholder('Additional Description Details*').fill('Other issue description');
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Proceeds as normal, confirming the rest of the issue information
  // Skipping image upload and contact information for the purpose of this test
  await page.getByRole('button', { name: 'Next next step' }).click();
  await page.getByRole('button', { name: 'Review & Submit submit issue' }).click();
  await expect(page.getByText('Description: Other issue description')).toBeVisible();
  await expect(page.getByText('Issue Type: Other')).toBeVisible();
  await page.getByRole('button', { name: 'Submit submit issue' }).click();
  await expect(page.getByText('Thank You! The issue has been reported.')).toBeVisible();
});

/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
// });