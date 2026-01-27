<script lang="ts">
	import { Button, Modal, Input, TextArea, Portal } from 'stwui';
	import messages from '$media/messages.json';
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import {
		createInput,
		optionalCoalescePhoneNumberValidator,
		type FormInputValue,
		emailValidator,
		type InputValidator,
		inputValidatorFactory
	} from '$lib/utils/validation';
	import { setUpAlertRole } from '$lib/utils/functions';
	import { z } from 'zod';

	export let open = false;
	export let serviceRequestId: number;
	export let handleClose: () => void;

	const reasonValidator: InputValidator<string> = inputValidatorFactory(
		z.string().trim().min(1, 'Reason must be non-empty')
	);

	const libre311Service = useLibre311Service();

	let email: FormInputValue<string> = createInput('');
	let name: FormInputValue<string | undefined> = createInput('');
	let phone: FormInputValue<string | undefined> = createInput('');
	let reason: FormInputValue<string> = createInput('');
	let submitError = '';

	let loading = false;
	let success = false;

	async function submit() {
		submitError = '';

		email = emailValidator(email);
		reason = reasonValidator(reason);
		phone = optionalCoalescePhoneNumberValidator(phone);

		if (email.type === 'invalid' || reason.type === 'invalid' || phone.type === 'invalid') return;

		loading = true;
		try {
			await libre311Service.createRemovalSuggestion({
				service_request_id: serviceRequestId,
				email: email.value,
				name: name.value,
				phone: phone.value,
				reason: reason.value
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
			submitError = 'Failed to submit suggestion.';
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
		email.value = '';
		email.error = '';
		name.value = '';
		phone.value = '';
		phone.error = '';
		reason.value = '';
		reason.error = '';
		submitError = '';
		success = false;
	}

	let emailRoot: HTMLElement;
	let reasonRoot: HTMLElement;
	let phoneRoot: HTMLElement;

	$: if (open) {
		// when opened clear out input
		reset();
	}

	$: setUpAlertRole(email, emailRoot, 'input#suggest-email', 'suggest-email-error');
	$: setUpAlertRole(reason, reasonRoot, 'textarea#suggest-reason', 'suggest-reason-error');
	$: setUpAlertRole(phone, phoneRoot, 'input#suggest-phone', 'suggest-phone-error');
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
							{#if submitError}
								<p class="mb-2 text-red-500" role="alert">{submitError}</p>
							{/if}
							<div bind:this={emailRoot} class="mb-2">
								<Input
									name="suggest-email"
									error={email.error}
									bind:value={email.value}
									type="email"
									placeholder="your@email.com"
								>
									<Input.Label slot="label">Email *</Input.Label>
								</Input>
							</div>
							<div class="mb-2">
								<Input bind:value={name.value} placeholder="Your Name">
									<Input.Label slot="label">Name</Input.Label>
								</Input>
							</div>
							<div bind:this={phoneRoot} class="mb-2">
								<Input
									name="suggest-phone"
									inputmode="tel"
									autocomplete="on"
									bind:value={phone.value}
									placeholder={messages.contact.phone.placeholder}
									error={phone.error}
									on:input={formatPhoneNumber}
								>
									<Input.Label slot="label">Phone</Input.Label>
								</Input>
							</div>
							<div bind:this={reasonRoot} class="mb-2">
								<TextArea
									name="suggest-reason"
									bind:value={reason.value}
									error={reason.error}
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
