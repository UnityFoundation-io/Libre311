<script lang="ts">
	import { Libre311ServiceImpl } from '$lib/services/Libre311/Libre311';
	import { createEventDispatcher } from 'svelte';
	import Breakpoint from '../Breakpoint.svelte';
	import messages from '$media/messages.json';
	import { Button } from 'stwui';
	import { FilePicker } from 'stwui';
	import { uploadIcon } from '$lib/components/Svg/outline/upload-icon.js';
	import { useLibre311Context } from '$lib/context/Libre311Context';
	import type { DropResult } from 'stwui/types';
	import { page } from '$app/stores';
	import type { CreateServiceRequestUIParams } from './shared';

	let input: HTMLInputElement;

	export let params: Readonly<Partial<CreateServiceRequestUIParams>>;

	const dispatch = createEventDispatcher();

	const linkResolver = useLibre311Context().linkResolver;

	const allowedExtensions = Libre311ServiceImpl.supportedImageTypes.map((s) => {
		return s.slice(s.indexOf('/') + 1);
	});

	async function onChange() {
		if (input.files && input.files.length < 2) dispatchFile(input.files[0]);
	}

	function desktopDropFiles(dropFiles: DropResult) {
		if (dropFiles.rejected[0]) {
			console.log('Unsupported file type');
		}
		for (const file of dropFiles.accepted) {
			dispatchFile(file);
			return; // Only upload single file
		}
	}

	function dispatchFile(file: File) {
		dispatch('stepChange', { file });
	}
</script>

<Breakpoint>
	<div slot="is-desktop" class="flex h-full w-full items-center justify-center">
		<div class="flex-col">
			<div class="mb-4">
				<FilePicker onDrop={desktopDropFiles} {allowedExtensions}>
					<FilePicker.Icon slot="icon" data={uploadIcon} />
					<FilePicker.Title slot="title">{messages['photo']['upload']}</FilePicker.Title>
					<FilePicker.Description slot="description">Drag & Drop your file</FilePicker.Description>
				</FilePicker>
			</div>

			<div class="grid grid-rows-2 gap-3">
				<Button
					type="primary"
					on:click={() => {
						dispatch('stepChange', { file: undefined });
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
					name="photo"
					id="camera-roll-btn"
					accept="image/*"
					hidden
					bind:this={input}
					on:change={onChange}
				/>
				<label for="camera-roll-btn">{messages['photo']['upload']}</label>

				<Button
					type="link"
					on:click={() => {
						dispatch('stepChange', { file: undefined });
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
