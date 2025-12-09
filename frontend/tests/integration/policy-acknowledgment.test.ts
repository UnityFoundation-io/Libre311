import { test, expect } from '@playwright/test';

test.describe('Policy Acknowledgment', () => {
	test.skip('displays acknowledgment on submission review page', async () => {
		// Note: This test requires navigating through the service request flow
		// to reach the review page where the acknowledgment is displayed.
		// For a full E2E test, we would need to:
		// 1. Navigate to /issue/create
		// 2. Select location
		// 3. Choose service
		// 4. Fill details
		// 5. Fill contact info
		// 6. Reach review page
		// Skipped for now as it requires full navigation flow
	});

	test.skip('Terms of Use link has correct attributes', async () => {
		// This would require navigating to the review page
		// Skipping for now until full flow is implemented in tests
	});

	test.skip('Privacy Policy link has correct attributes', async () => {
		// This would require navigating to the review page
		// Skipping for now until full flow is implemented in tests
	});
});

// Note: These integration tests are placeholders.
// Full integration testing would require:
// 1. Mock backend services
// 2. Complete navigation flow through service request creation
// 3. Authentication setup
// The acknowledgment functionality can be manually tested by:
// - Starting the dev server
// - Creating a service request
// - Verifying the acknowledgment appears on the review page

test.describe('Terms of Use Page', () => {
	test('displays default content with template notice', async ({ page }) => {
		await page.goto('/policies/terms');

		// Check for default template notice
		const defaultNotice = page.locator('text=Default Template');
		await expect(defaultNotice).toBeVisible();

		// Check for placeholder text
		const placeholder = page.locator('text=[JURISDICTION_NAME]');
		await expect(placeholder).toBeVisible();
	});

	test('renders markdown correctly with headings and lists', async ({ page }) => {
		await page.goto('/policies/terms');

		// Check for h1 heading
		const h1 = page.locator('h1').first();
		await expect(h1).toBeVisible();

		// Check for lists (ul or ol)
		const list = page.locator('ul, ol').first();
		await expect(list).toBeVisible();
	});

	test('page has correct title', async ({ page }) => {
		await page.goto('/policies/terms');

		// Check page title
		await expect(page).toHaveTitle(/Terms of Use/);
	});
});

test.describe('Privacy Policy Page', () => {
	test('displays default content with template notice', async ({ page }) => {
		await page.goto('/policies/privacy');

		// Check for default template notice
		const defaultNotice = page.locator('text=Default Template');
		await expect(defaultNotice).toBeVisible();

		// Check for placeholder text
		const placeholder = page.locator('text=[JURISDICTION_NAME]');
		await expect(placeholder).toBeVisible();
	});

	test('renders markdown correctly with headings and lists', async ({ page }) => {
		await page.goto('/policies/privacy');

		// Check for h1 heading
		const h1 = page.locator('h1').first();
		await expect(h1).toBeVisible();

		// Check for lists (ul or ol)
		const list = page.locator('ul, ol').first();
		await expect(list).toBeVisible();
	});

	test('page has correct title', async ({ page }) => {
		await page.goto('/policies/privacy');

		// Check page title
		await expect(page).toHaveTitle(/Privacy Policy/);
	});
});
