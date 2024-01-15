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

test('open reported issues screen', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  const issuesButton = await page.waitForSelector('#button-find-issues');
  await issuesButton.click();

  await page.locator('#map').isVisible();
  await page.getByRole('table').isVisible();
});

test('apply filters to reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.waitForTimeout(1000);
  const issuesButton = await page.waitForSelector('#button-find-issues');
  await issuesButton.click();

  // Opens filters
  await page.locator('div').filter({ hasText: /\+$/ }).locator('span').click();
  // Filters to only display Bus Stop issues 
  await page.getByRole('combobox').first().selectOption({ index: 2 });
});

test('view expanded issue details from reported issues table', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.waitForTimeout(1000);
  const issuesButton = await page.waitForSelector('#button-find-issues');
  await issuesButton.click();

  await page.getByRole('cell', { name: /Two city buses parked obstructing .*/ }).click();
  await page.getByRole('img', { name: 'detail view' }).click();
  await expect(page.getByText('Type: Bike Lane')).toBeVisible();
  await expect(page.getByText('Description:Two city buses parked obstructing the bike lane rather than using th')).toBeVisible();
  await expect(page.getByText('Location:270 Temple St New Haven, CT, 06511, USA')).toBeVisible();
});


/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'search for reported issues Find a Reported Issue' }).click();

// });
