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

test('footer displays logo', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.waitForTimeout(1000);
  const img = page.getByRole('img', { name: 'footer logo' });
  await img.waitFor({ state: 'visible'})
});

test('footer displays major headings', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'Admin Login' }).click();
  await expect(page).toHaveURL(/.*accounts.google.com\/.*/);

  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'About Us' }).click();
});

test('footer displays about sub links', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'GitHub Repo' }).click();
  await expect(page).toHaveURL('https://github.com/UnityFoundation-io/Libre311');
});
