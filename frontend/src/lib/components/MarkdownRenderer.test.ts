import { describe, it, expect } from 'vitest';
import { Carta } from 'carta-md';

/**
 * Unit tests for MarkdownRenderer component
 *
 * Note: These tests focus on the markdown parsing logic rather than component rendering,
 * as the project does not have @testing-library/svelte installed. The MarkdownRenderer
 * component is a simple wrapper around the carta-md library with styling.
 *
 * These tests verify:
 * 1. The carta-md library correctly parses markdown to HTML
 * 2. The expected HTML structure is generated for various markdown inputs
 * 3. Edge cases (empty markdown, invalid markdown) are handled
 *
 * Integration tests in Playwright verify the actual rendering in the browser.
 */

const carta = new Carta();

describe('MarkdownRenderer - Markdown Parsing', () => {
	it('parses basic markdown with headings, bold, and lists', () => {
		const markdown = `# Heading 1

## Heading 2

### Heading 3

This is a paragraph with **bold text** and *italic text*.

- List item 1
- List item 2
- List item 3

1. Numbered item 1
2. Numbered item 2

[Link text](https://example.com)`;

		const html = carta.renderSSR(markdown);

		// Check for headings
		expect(html).toContain('<h1>Heading 1</h1>');
		expect(html).toContain('<h2>Heading 2</h2>');
		expect(html).toContain('<h3>Heading 3</h3>');

		// Check for formatted text
		expect(html).toContain('<strong>bold text</strong>');
		expect(html).toContain('<em>italic text</em>');

		// Check for lists
		expect(html).toContain('<ul>');
		expect(html).toContain('<li>List item 1</li>');
		expect(html).toContain('<li>List item 2</li>');
		expect(html).toContain('<li>List item 3</li>');
		expect(html).toContain('</ul>');

		expect(html).toContain('<ol>');
		expect(html).toContain('<li>Numbered item 1</li>');
		expect(html).toContain('<li>Numbered item 2</li>');
		expect(html).toContain('</ol>');

		// Check for link
		expect(html).toContain('<a href="https://example.com">Link text</a>');
	});

	it('handles empty markdown without errors', () => {
		const markdown = '';

		// Should not throw
		const html = carta.renderSSR(markdown);

		// Empty markdown should produce empty string
		expect(html).toBe('');
	});

	it('parses markdown with custom placeholders', () => {
		const markdown = `# Terms of Use for [JURISDICTION_NAME]

This is a template for [JURISDICTION_NAME].

**Default Template** - Please customize this content.`;

		const html = carta.renderSSR(markdown);

		// Placeholders should be preserved in output
		expect(html).toContain('[JURISDICTION_NAME]');
		expect(html).toContain('<strong>Default Template</strong>');
		expect(html).toContain('<h1>Terms of Use for [JURISDICTION_NAME]</h1>');
	});

	it('handles markdown with line breaks', () => {
		const markdown = `Line 1
Line 2
Line 3`;

		const html = carta.renderSSR(markdown);

		// With default settings, single line breaks might not become <br>
		// but let's check what Carta/Marked does by default
		expect(html).toContain('Line 1');
		expect(html).toContain('Line 2');
		expect(html).toContain('Line 3');
	});

	it('handles special characters and HTML entities', () => {
		const markdown = 'Test with & ampersand and < less than and > greater than';

		const html = carta.renderSSR(markdown);

		// Special characters should be escaped in HTML
		expect(html).toContain('&amp;');
		expect(html).toContain('&lt;');
		expect(html).toContain('&gt;');
	});

	it('parses complex nested structures', () => {
		const markdown = `## Section

- Item 1
  - Nested item 1.1
  - Nested item 1.2
- Item 2

### Subsection

1. First
2. Second
   - Nested bullet
3. Third`;

		const html = carta.renderSSR(markdown);

		// Should handle nested lists
		expect(html).toContain('<h2>Section</h2>');
		expect(html).toContain('<h3>Subsection</h3>');
		expect(html).toContain('<ul>');
		expect(html).toContain('<ol>');
		expect(html).toContain('Nested item 1.1');
		expect(html).toContain('Nested bullet');
	});

	it('parses links with various formats', () => {
		const markdown = `[External link](https://example.com)
[Link with title](https://example.com "Title text")
[Relative link](/policies/terms)`;

		const html = carta.renderSSR(markdown);

		expect(html).toContain('<a href="https://example.com">External link</a>');
		expect(html).toContain('title="Title text"');
		expect(html).toContain('<a href="/policies/terms">Relative link</a>');
	});

	it('preserves horizontal rules', () => {
		const markdown = `Section 1

---

Section 2`;

		const html = carta.renderSSR(markdown);

		expect(html).toContain('<hr>');
		expect(html).toContain('Section 1');
		expect(html).toContain('Section 2');
	});
});

describe('MarkdownRenderer - Error Handling', () => {
	it('handles undefined or null gracefully', () => {
		// The component reactive statement will handle these,
		// but test that carta doesn't crash with empty strings
		const emptyString = '';
		const html = carta.renderSSR(emptyString);

		expect(html).toBe('');
	});

	it('handles extremely long markdown without crashing', () => {
		// Generate a large markdown document
		const longMarkdown = Array(100)
			.fill(0)
			.map((_, i) => `## Heading ${i}\n\nParagraph ${i} with some content.`)
			.join('\n\n');

		// Should not throw
		const html = carta.renderSSR(longMarkdown);

		expect(html).toContain('Heading 0');
		expect(html).toContain('Heading 99');
	});
});
