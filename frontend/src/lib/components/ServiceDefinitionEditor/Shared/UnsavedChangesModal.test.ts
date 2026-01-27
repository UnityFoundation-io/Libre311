import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/svelte';
import UnsavedChangesModal from './UnsavedChangesModal.svelte';

describe('UnsavedChangesModal', () => {
	beforeEach(() => {
		vi.clearAllMocks();
	});

	it('does not render when open is false', () => {
		render(UnsavedChangesModal, {
			props: { open: false }
		});

		expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
	});

	it('renders modal when open is true', () => {
		render(UnsavedChangesModal, {
			props: { open: true }
		});

		expect(screen.getByRole('alertdialog')).toBeInTheDocument();
		expect(screen.getByText('Unsaved Changes')).toBeInTheDocument();
		expect(
			screen.getByText('You have unsaved changes. Do you want to save them before leaving?')
		).toBeInTheDocument();
	});

	it('displays Cancel, Discard, and Save buttons', () => {
		render(UnsavedChangesModal, {
			props: { open: true }
		});

		expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /discard/i })).toBeInTheDocument();
		expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument();
	});

	it('dispatches cancel event when Cancel button is clicked', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true }
		});

		let cancelCalled = false;
		component.$on('cancel', () => {
			cancelCalled = true;
		});

		const cancelButton = screen.getByRole('button', { name: /cancel/i });
		await fireEvent.click(cancelButton);

		expect(cancelCalled).toBe(true);
	});

	it('dispatches discard event when Discard button is clicked', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true }
		});

		let discardCalled = false;
		component.$on('discard', () => {
			discardCalled = true;
		});

		const discardButton = screen.getByRole('button', { name: /discard/i });
		await fireEvent.click(discardButton);

		expect(discardCalled).toBe(true);
	});

	it('dispatches save event when Save button is clicked', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true }
		});

		let saveCalled = false;
		component.$on('save', () => {
			saveCalled = true;
		});

		const saveButton = screen.getByRole('button', { name: /save/i });
		await fireEvent.click(saveButton);

		expect(saveCalled).toBe(true);
	});

	it('dispatches cancel event when Escape key is pressed', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true }
		});

		let cancelCalled = false;
		component.$on('cancel', () => {
			cancelCalled = true;
		});

		await fireEvent.keyDown(window, { key: 'Escape' });

		expect(cancelCalled).toBe(true);
	});

	it('does not dispatch cancel on Escape when isSaving is true', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true, isSaving: true }
		});

		let cancelCalled = false;
		component.$on('cancel', () => {
			cancelCalled = true;
		});

		await fireEvent.keyDown(window, { key: 'Escape' });

		expect(cancelCalled).toBe(false);
	});

	it('dispatches cancel event when backdrop is clicked', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true }
		});

		let cancelCalled = false;
		component.$on('cancel', () => {
			cancelCalled = true;
		});

		// Find the backdrop (the element with bg-black/50)
		const backdrop = screen.getByRole('presentation');
		await fireEvent.click(backdrop);

		expect(cancelCalled).toBe(true);
	});

	it('disables all buttons when isSaving is true', () => {
		render(UnsavedChangesModal, {
			props: { open: true, isSaving: true }
		});

		expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
		expect(screen.getByRole('button', { name: /discard/i })).toBeDisabled();
		expect(screen.getByRole('button', { name: /saving/i })).toBeDisabled();
	});

	it('shows spinner and "Saving..." text when isSaving is true', () => {
		render(UnsavedChangesModal, {
			props: { open: true, isSaving: true }
		});

		expect(screen.getByText('Saving...')).toBeInTheDocument();
		// Check for spinner (SVG with animate-spin class)
		const saveButton = screen.getByRole('button', { name: /saving/i });
		const spinner = saveButton.querySelector('svg.animate-spin');
		expect(spinner).toBeInTheDocument();
	});

	it('has proper accessibility attributes', () => {
		render(UnsavedChangesModal, {
			props: { open: true }
		});

		const dialog = screen.getByRole('alertdialog');
		expect(dialog).toHaveAttribute('aria-modal', 'true');
		expect(dialog).toHaveAttribute('aria-labelledby', 'unsaved-modal-title');
		expect(dialog).toHaveAttribute('aria-describedby', 'unsaved-modal-description');

		// Check title and description IDs match
		expect(screen.getByText('Unsaved Changes')).toHaveAttribute('id', 'unsaved-modal-title');
		expect(
			screen.getByText('You have unsaved changes. Do you want to save them before leaving?')
		).toHaveAttribute('id', 'unsaved-modal-description');
	});

	it('does not close on backdrop click when isSaving is true', async () => {
		const { component } = render(UnsavedChangesModal, {
			props: { open: true, isSaving: true }
		});

		let cancelCalled = false;
		component.$on('cancel', () => {
			cancelCalled = true;
		});

		const backdrop = screen.getByRole('presentation');
		await fireEvent.click(backdrop);

		expect(cancelCalled).toBe(false);
	});
});
