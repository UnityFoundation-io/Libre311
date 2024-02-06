<script lang="ts">
	import type { CreateServiceRequestParams } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher } from 'svelte';
	import Breakpoint from '../Breakpoint.svelte';
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
	import { FilePicker } from 'stwui';
	import { uploadIcon } from '$lib/components/Svg/outline/upload-icon.js';
	import { useLibre311Context, useLibre311Service } from '$lib/context/Libre311Context';
	import type { DropResult } from 'stwui/types';
	import { page } from '$app/stores';
	import { stageImage } from '$lib/stores/serviceRequestImageUpload';

	let input: HTMLInputElement;

	export let params: Readonly<Partial<CreateServiceRequestParams>>;

	const dispatch = createEventDispatcher();

	const linkResolver = useLibre311Context().linkResolver;

	async function onChange() {
		if (input.files && input.files.length < 2) await uploadImage(input.files[0]);
		else throw new Error('Can only upload a single image.');
	}

	async function desktopDropFiles(dropFiles: DropResult) {
		for (const file of dropFiles.accepted) {
			await uploadImage(file);
			return; // Only upload single file
		}
	}

	function uploadImage(file: File) {
		if (file) {
			const reader = new FileReader();
			reader.addEventListener('load', async function () {
				if (reader.result) {
					const result: String = new String(reader.result);
					stageImage(result.toString());
					dispatch('stepChange');
				}
			});
			reader.readAsDataURL(file);

			return;
		}
	}
</script>

<Breakpoint>
	<div slot="is-desktop" class="flex h-full w-full items-center justify-center">
		<div class="flex-col">
			<div class="mb-4">
				<FilePicker onDrop={desktopDropFiles}>
					<FilePicker.Icon slot="icon" data={uploadIcon} />
					<FilePicker.Title slot="title">{messages['photo']['upload']}</FilePicker.Title>
					<FilePicker.Description slot="description">Drag & Drop your file</FilePicker.Description>
				</FilePicker>
			</div>

			<div class="grid grid-rows-2 gap-3">
				<Button
					type="primary"
					on:click={() => {
						dispatch('stepChange');
					}}
				>
					{messages['photo']['no_upload']}
				</Button>

				<Button type="link" href={linkResolver.createIssuePagePrevious($page.url)}>
					{messages['photo']['back']}
				</Button>
			</div>
		</div>
	</div>

	<div slot="is-mobile-or-tablet" class="flex h-full w-full items-center justify-center">
		<div class="flex-col">
			<div class="grid grid-rows-1 gap-3">
				<input
					type="file"
					id="actual-btn"
					accept="image/*"
					capture="environment"
					hidden
					bind:this={input}
					on:change={onChange}
				/>
				<label for="actual-btn">{messages['photo']['take_photo']}</label>

				<input
					type="file"
					name="photo"
					id="camera-roll-btn"
					accept="image/*"
					hidden
					bind:this={input}
					on:change={onChange}
				/>
				<label for="camera-roll-btn">{messages['photo']['camera_roll']}</label>

				<Button
					type="link"
					on:click={() => {
						dispatch('stepChange');
					}}
				>
					{messages['photo']['no_upload']}
				</Button>

				<Button type="link" href={linkResolver.createIssuePagePrevious($page.url)}>
					{messages['photo']['back']}
				</Button>
			</div>
		</div>
	</div>
</Breakpoint>

<style>
	label {
		background-color: hsl(var(--primary));
		color: hsl(var(--primary-content));
		padding: 0.5rem;
		font-family: sans-serif;
		text-align: center;
		border-radius: 0.3rem;
		cursor: pointer;
	}
</style>
