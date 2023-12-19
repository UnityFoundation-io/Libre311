// Copyright 2023 Libre311 Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { expect, test } from '@playwright/test';


test('report new issue in offline mode', async ({ page, context }) => {
  await page.goto('http://localhost:3000/');
  // Makes sure the page is fully loaded before toggling offline
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

  // Picture upload disabled while offline
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

  // Refreshes page
  await page.goto('http://localhost:3000/');
  await context.setOffline(false);

  // Awaits notification that the offline issue has been uploaded
  // await expect(page.getByText('Your pending offline issue was successfully reported.')).toBeVisible({ timeout: 15000 });
  await page.route('**/api/requests.json', route => route.fulfill({
    status: 200
  }));
  await page.getByRole('button', { name: 'Close'}).click();
});
