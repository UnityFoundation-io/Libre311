<script lang="ts">
	import { Button, Modal, Portal } from 'stwui';
	import { tick } from 'svelte';

	export let open = false;
	export let title = 'Confirm Action';
	export let message = 'Are you sure you want to proceed?';
	export let handleClose: () => void;
	export let handleConfirm: () => void;
	export let loading = false;
	export let confirmationLabel: string | undefined = undefined;

	let confirmed = !confirmationLabel;

	$: if (open) {
		confirmed = !confirmationLabel;
		tick().then(() => {
			const cancelButton = document.getElementById('cancel_dismiss');
			cancelButton?.focus();
		});
	}
</script>

{#if open}
	<Portal>
		<Modal {handleClose}>
			<Modal.Content slot="content">
				<Modal.Content.Header slot="header">{title}</Modal.Content.Header>
				<Modal.Content.Body slot="body">
					<p class="p-4 text-sm">{message}</p>
					{#if confirmationLabel}
						<div class="px-4 pb-4">
							<label class="flex cursor-pointer items-center gap-2">
								<input
									type="checkbox"
									bind:checked={confirmed}
									class="h-4 w-4 rounded border-gray-300 text-primary focus:ring-primary"
								/>
								<span class="text-sm text-gray-700">{confirmationLabel}</span>
							</label>
						</div>
					{/if}
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex w-full justify-end gap-2">
						<Button id="cancel_dismiss" on:click={handleClose} type="ghost">Cancel</Button>
						<Button
							on:click={handleConfirm}
							variant="primary"
							{loading}
							disabled={!confirmed || loading}>Confirm</Button
						>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	</Portal>
{/if}
