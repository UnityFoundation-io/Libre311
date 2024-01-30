<script lang="ts">
	import { useLibre311Service } from '$lib/context/Libre311Context';
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
	import { createEventDispatcher } from 'svelte';

	export let params: Readonly<Partial<CreateServiceRequestParams>>;

	const dispatch = createEventDispatcher();

	const libre311Service = useLibre311Service();

	let files: FileList;

	$: if (files) {
		console.log(files);

		for (const file of files) {
			console.log(`${file.name}: ${file.size} bytes`);
			libre311Service.uploadImage(file);
		}

		dispatch('stepChange');
	}
</script>

<div class="flex h-full w-full items-center justify-center">
	<div class="flex-col">
		<div class="grid grid-rows-2 gap-3">
			<input type="file" id="actual-btn" accept="image/*" capture="environment" hidden bind:files />
			<label for="actual-btn">{messages['photo']['take_photo']}</label>

			<input type="file" name="photo" id="camera-roll-btn" accept="image/*" hidden bind:files />
			<label for="camera-roll-btn">{messages['photo']['camera_roll']}</label>

			<Button
				type="link"
				on:click={() => {
					dispatch('stepChange');
				}}
			>
				{messages['photo']['no_upload']}
			</Button>

			<Button type="link" on:click={() => {}}>
				{messages['photo']['back']}
			</Button>
		</div>
	</div>
</div>

<style>
	label {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		padding: 0.5rem;
		font-family: sans-serif;
		text-align: center;
		border-radius: 0.3rem;
		cursor: pointer;
		margin-top: 1rem;
	}
</style>
