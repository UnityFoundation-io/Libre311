import { expect, test } from '@playwright/test';


test('report new issue in offline mode', async ({ page, context }) => {
  await page.goto('http://localhost:3000/');
  await expect(page.getByRole('button', { name: 'report a new issue Report a New Issue' })).toBeVisible();
  await context.setOffline(true);
  await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
  
  // Enter issue location
  await page.getByPlaceholder('Enter the address').click();
  await page.getByPlaceholder('Enter the address').fill('12140 Woodcrest Executive Drive');
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter issue details
  await page.getByRole('combobox').selectOption({ index: 6 });
  await page.getByPlaceholder('Additional Description Details').click();
  await page.getByPlaceholder('Additional Description Details').fill('Additional description details');
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Upload picture
  await page.getByRole('button', { name: 'Next next step' }).click();

  // Enter contact information
  await page.getByPlaceholder('ex: John Doe').click();
  await page.getByPlaceholder('ex: John Doe').fill('John Doe');
  await page.getByPlaceholder('ex: johndoe@gmail.com').click();
  await page.getByPlaceholder('ex: johndoe@gmail.com').fill('johndoe@gmail.com');
  await page.getByRole('button', { name: 'Review & Submit submit issue' }).click();

  // Review information
  await expect(page.getByText('Issue Location: 12140 Woodcrest Executive Drive')).toBeVisible();
  await expect(page.getByText('Issue Type: Other')).toBeVisible();
  await expect(page.getByText('Description: Additional description details')).toBeVisible();
  await expect(page.getByText('Name of Submitter: John Doe')).toBeVisible();
  await expect(page.getByText('Contact Info: johndoe@gmail.com')).toBeVisible();
  await page.getByRole('button', { name: 'Submit submit issue' }).click();

  // Confirms the submission went through
  await expect(page.getByText('Thank You! The issue will be reported when you go online.')).toBeVisible();

  await page.goto('http://localhost:3000/');
  await context.setOffline(false);

  await page.getByRole('button', { name: 'Close' }).click({ timeout: 500000 });

  await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();
  await page.getByRole('cell', { name: 'Additional description details' }).click();
  await page.getByRole('img', { name: 'detail view' }).click();
  await page.getByText('Type: Other').click();
  await page.getByText('Description:Additional description details').click();
  await page.getByText('Location:12140 Woodcrest Executive Drive').click();
});

// The issue will be reported when you go online.

// Issue Reported
// Your pending offline issue was successfully reported.

// check local storage. if blank, submitted successfully. if completed, issue has not fully submitted