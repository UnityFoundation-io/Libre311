import { expect, test } from '@playwright/test';

test('footer displays Local Motion logo', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await expect(page.getByRole('img', { name: 'local motion logo' })).toBeVisible();
});

test('footer displays major headings', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'Admin Login' }).click();
  await expect(page).toHaveURL(/.*accounts.google.com\/.*/);

  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'Contact Us' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/contact-us/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'Advocacy' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/?ct_template=advocacy');

  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'Get Involved' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/get-involved/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('heading', { name: 'About' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/');
});

test('footer displays advocacy sub links', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Livable Streets' }).first().click();
  await expect(page).toHaveURL('https://lomocomo.org/advocacy/livable-streets/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Transportation Planning' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/advocacy/transportation-planning/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Grassroots Organizing' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/advocacy/grassroots-organizing/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'State-Level Advocacy' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/advocacy/missourians-for-responsible-transportation/');
});

test('footer displays get involved sub links', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Membership' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/get-involved/membership/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Volunteer' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/get-involved/volunteer/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Sign up for our newsletter' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/get-involved/sign-up-for-our-newsletter/');
});

test('footer displays about sub links', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Staff' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/staff/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Board of Directors' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/board-of-directors/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Annual Reports' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/annual-reports/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Legal and Financial' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/legal-and-financial/');

  await page.goto('http://localhost:3000/');
  await page.getByRole('link', { name: 'Our Rebrand Story' }).click();
  await expect(page).toHaveURL('https://lomocomo.org/about/the-rebrand-story/');
});

test('footer has facebook icon', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('img', { name: 'facebook' }).click();
  await expect(page).toHaveURL('https://www.facebook.com/LocalMotionCoMo/');
});

test('footer has instagram icon', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('img', { name: 'instagram' }).click();
  await expect(page).toHaveURL(/https:\/\/www.instagram.com\/.*localmotioncomo.*/);
});

test('footer has twitter icon', async ({ page }) => {
  await page.goto('http://localhost:3000/');
  await page.getByRole('img', { name: 'twitter' }).click();
  await expect(page).toHaveURL(/https:\/\/twitter\.com\/.*localmotioncomo/);
});