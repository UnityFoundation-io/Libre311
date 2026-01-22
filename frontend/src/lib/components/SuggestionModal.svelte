<script lang="ts">
	import { Button, Modal, Input, TextArea, Portal } from 'stwui';
	import messages from '$media/messages.json';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import {
		createInput,
		optionalCoalescePhoneNumberValidator,
		type FormInputValue
	} from '$lib/utils/validation';

	export let open = false;
	export let serviceRequestId: number;
	export let handleClose: () => void;

	const libre311Service = useLibre311Service();

	let email = '';
	let name = '';
	let phone: FormInputValue<string | undefined> = createInput('');
	let reason = '';
	let loading = false;
	let error = '';
	let success = false;

	async function submit() {
		if (!email || !reason) {
			error = 'Email and Reason are required.';
			return;
		}

		phone = optionalCoalescePhoneNumberValidator(phone);
		if (phone.type === 'invalid') return;

		loading = true;
		error = '';
		try {
			await libre311Service.createRemovalSuggestion({
				service_request_id: serviceRequestId,
				email,
				name,
				phone: phone.value,
				reason
			});
			success = true;
			setTimeout(() => {
				handleClose();
				setTimeout(() => {
					reset();
				}, 500);
			}, 1500);
		} catch (e) {
			console.error(e);
			error = 'Failed to submit suggestion.';
		} finally {
			loading = false;
		}
	}

	function formatPhoneNumber() {
		if (!phone.value) return;
		phone.value = phone.value
			.replace(/\D/g, '') // Remove non-digit characters
			.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3'); // Format with hyphens
		phone = phone;
	}

	function reset() {
		email = '';
		name = '';
		phone = createInput('');
		reason = '';
		error = '';
		success = false;
	}
</script>

{#if open}
	<Portal>
		<Modal {handleClose}>
			<Modal.Content slot="content">
				<Modal.Content.Header slot="header">Suggest Removal</Modal.Content.Header>
				<Modal.Content.Body slot="body">
					{#if success}
						<div class="p-4 text-green-600">Suggestion submitted successfully!</div>
					{:else}
						<div class="flex flex-col gap-4 p-2">
							{#if error}
								<p class="mb-2 text-red-500">{error}</p>
							{/if}
							<div class="mb-2">
								<Input bind:value={email} type="email" placeholder="your@email.com">
									<Input.Label slot="label">Email *</Input.Label>
								</Input>
							</div>
							<div class="mb-2">
								<Input bind:value={name} placeholder="Your Name">
									<Input.Label slot="label">Name</Input.Label>
								</Input>
							</div>
							<div class="mb-2">
								<Input
									bind:value={phone.value}
									placeholder={messages.contact.phone.placeholder}
									error={phone.error}
									on:input={formatPhoneNumber}
								>
									<Input.Label slot="label">Phone</Input.Label>
								</Input>
							</div>
							<div class="mb-2">
								<TextArea
									bind:value={reason}
									name="reason"
									placeholder="Why should this be removed?"
								>
									<TextArea.Label slot="label">Reason *</TextArea.Label>
								</TextArea>
							</div>
						</div>
					{/if}
				</Modal.Content.Body>
				<Modal.Content.Footer slot="footer">
					<div class="flex w-full justify-end gap-2">
						{#if !success}
							<Button on:click={handleClose} type="ghost">Cancel</Button>
							<Button on:click={submit} type="primary" {loading}>Submit</Button>
						{:else}
							<Button on:click={handleClose} type="ghost">Close</Button>
						{/if}
					</div>
				</Modal.Content.Footer>
			</Modal.Content>
		</Modal>
	</Portal>
{/if}
