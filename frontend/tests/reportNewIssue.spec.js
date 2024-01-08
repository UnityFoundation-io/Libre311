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

import { expect, test } from "@playwright/test";

test("report new issue with image", async ({ page }) => {
  await page.goto("http://localhost:3000/");
  await page.waitForTimeout(5000);
  const issuesButton = await page.waitForSelector("#button-report-issue");
  await issuesButton.click();

  const locationInputLocator = await page.getByPlaceholder("Enter the address");
  await locationInputLocator.click();
  await locationInputLocator.fill(
    "12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA"
  );
  await locationInputLocator.press("Enter");

  await page.getByRole("button", { name: "Next" }).click();

  // Enter issue details
  await expect(page.getByRole("combobox")).toBeVisible();
  await page.getByRole("combobox").selectOption("002");
  await page.getByPlaceholder("Bus Stop Issues").click();
  await page.getByText("Unsafe location").click();
  await page.getByText("No sidewalk").click();
  await page.locator("#issue-details").press("Tab");
  await page.getByPlaceholder("Additional Description Details").click();
  await page
    .getByPlaceholder("Additional Description Details")
    .fill("Additional description details");
  await page.getByRole("button", { name: "Next" }).click();

  // Upload picture
  // ~ These tests are commented out because the CI environment currently does not support
  // ~ authentication with GCP storage

  // const fileChooserPromise = page.waitForEvent('filechooser');
  // await page.getByRole('img', { name: 'take photo' }).click();
  // const fileChooser = await fileChooserPromise;
  // await fileChooser.setFiles('./tests/fixtures/logo.png');
  // await page.getByRole('button', { name: 'Upload' }).click();
  // await expect(page.getByText('The uploaded image has been saved.')).toBeVisible();
  await page.getByRole("button", { name: "Next" }).click();

  // Enter contact information
  await page.getByPlaceholder("ex: John Doe").click();
  await page.getByPlaceholder("ex: John Doe").fill("John Doe");
  await page.getByPlaceholder("ex: johndoe@gmail.com").click();
  await page
    .getByPlaceholder("ex: johndoe@gmail.com")
    .fill("johndoe@gmail.com");
  await page.getByRole("button", { name: "Review & Submit" }).click();

  // Review information
  await expect(
    page.getByText(
      "Issue Location: 12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA"
    )
  ).toBeVisible();
  await expect(page.getByText("Issue Type: Bus Stop")).toBeVisible();
  await expect(
    page.getByText("Issue Details: 1-Unsafe location2-No sidewalk")
  ).toBeVisible();
  // await expect(page.getByRole('img', { name: 'uploaded image' })).toBeVisible();
  await expect(
    page.getByText("Description: Additional description details")
  ).toBeVisible();
  await expect(page.getByText("Name of Submitter: John Doe")).toBeVisible();
  await expect(page.getByText("Contact Info: johndoe@gmail.com")).toBeVisible();
  await page.getByRole("button", { name: "Submit" }).click();
  await page.route("**/api/requests.json", (route) =>
    route.fulfill({
      status: 200,
    })
  );

  // Confirms the submission went through
  // await page.getByText(/Thank You!.*/).click({ timeout: 25000 });
});

test("report new issue with other issue type", async ({ page }) => {
  await page.goto("http://localhost:3000/");
  await page.waitForTimeout(5000);
  const issuesButton = await page.waitForSelector("#button-report-issue");
  await issuesButton.click();

  // Enter issue location
  await page.getByPlaceholder("Enter the address").click();
  await page
    .getByPlaceholder("Enter the address")
    .fill("12140 Woodcrest Executive Drive");
  await page.getByPlaceholder("Enter the address").press("Enter");
  await expect(
    page.getByText("12140 Woodcrest Executive Dr, Creve Coeur, MO 63141, USA")
  ).toBeVisible();
  await page.getByRole("button", { name: "Next" }).click();

  // Issue type "other" selected
  await expect(page.getByRole("combobox")).toBeVisible();
  await page.getByRole("combobox").selectOption({ index: 6 });
  await page
    .getByPlaceholder("Additional Description Details*")
    .fill("Other issue description");
  await page.getByRole("button", { name: "Next" }).click();

  // Proceeds as normal, confirming the rest of the issue information
  // Skipping image upload and contact information for the purpose of this test
  await page.getByRole("button", { name: "Next" }).click();
  await page.getByRole("button", { name: "Review & Submit" }).click();
  await expect(page.getByText("Issue Type: Other")).toBeVisible();
  await expect(
    page.getByText("Description: Other issue description")
  ).toBeVisible();
  await page.getByRole("button", { name: "Submit" }).click();
  await page.route("**/api/requests.json", (route) =>
    route.fulfill({
      status: 200,
    })
  );
});

/* Template for possible future tests */
// test('test name', async ({ page }) => {
//   await page.goto('http://localhost:3000/');
//   await page.getByRole('button', { name: 'report a new issue Report a New Issue' }).click();
// });
