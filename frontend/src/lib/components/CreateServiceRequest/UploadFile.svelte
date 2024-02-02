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

	export let params: Readonly<Partial<CreateServiceRequestParams>>;

	const dispatch = createEventDispatcher();

	const libre311Service = useLibre311Service();
	const linkResolver = useLibre311Context().linkResolver;

	let files: FileList;

	$: if (files) {
		uploadFiles(files);
	}

	function desktopDropFiles(dropFiles: DropResult) {
		uploadFiles(dropFiles.accepted);
	}

	function uploadFiles(files: FileList | File[]) {
		for (const file of files) {
			console.log(`${file.name}: ${file.size} bytes`);
			libre311Service.uploadImage(file);
		}

		dispatch('stepChange');
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
			<div class="grid grid-rows-4 gap-3">
				<input
					type="file"
					id="actual-btn"
					accept="image/*"
					capture="environment"
					hidden
					bind:files
				/>
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
