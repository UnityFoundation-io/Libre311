<script lang="ts">
	import { Button, Modal, Portal } from 'stwui';
	import { tick } from 'svelte';

	export let open = false;
	export let title = 'Confirm Action';
	export let message = 'Are you sure you want to proceed?';
	export let handleClose: () => void;
	export let handleConfirm: () => void;
	export let loading = false;

	$: if (open) {
		tick().then(() => {
			const cancelButton = document.getElementById("cancel_dismiss");
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
					<p class="p-4">{message}</p>
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex w-full justify-end gap-2">
						<Button id="cancel_dismiss" on:click={handleClose} type="ghost">Cancel</Button>
						<Button on:click={handleConfirm} type="primary" {loading}>Confirm</Button>
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	</Portal>
{/if}
